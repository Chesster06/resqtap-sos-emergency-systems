package com.example.resqtap.room;
import com.example.resqtap.R;

import com.example.resqtap.call.CallSignalingClient;
import com.example.resqtap.call.IncomingCallActivity;
import com.example.resqtap.home.MainActivity;
import com.example.resqtap.map.GpsUtils;
import com.example.resqtap.notification.NotificationHelper;
import com.example.resqtap.notification.NotificationUtils;
import com.example.resqtap.sos.SosAudioManager;
import com.example.resqtap.sos.SosPrefs;
import com.example.resqtap.sos.VibrateManager;
import com.example.resqtap.utils.BatteryUtils;
import com.example.resqtap.utils.PermissionUtils;
import com.example.resqtap.utils.UserPrefs;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.ValueEventListener;


/**
 * LiveRoomTrackingService
 * Foreground Service untuk live location: stream GPS lat/lng ke RTDB tanpa kena kill oleh battery saver.
 */
public class LiveRoomTrackingService extends Service {
    /** Paparkan LowBatteryNotification pada skrin. */
    private void showLowBatteryNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm == null) return;
        String channelId = "battery_alert_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(channelId, "Battery Alerts", NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(chan);
        }
        Intent i = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_battery)
                .setContentTitle(getString(R.string.low_battery_alert))
                .setContentText(getString(R.string.low_battery_alert_desc))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        nm.notify(2102, builder.build());
    }
    public static final String ACTION_START = "com.example.resqtap.action.TRACK_START";
    public static final String ACTION_STOP = "com.example.resqtap.action.TRACK_STOP";
    public static final String EXTRA_ROOM_CODE = "room_code";
    public static final String EXTRA_UID = "uid";
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_PHOTO_URL = "photo_url";
    public static final String EXTRA_PHOTO_B64 = "photo_b64";

    private static final String CHANNEL_ID = "live_room_tracking";
    private static final int NOTIF_ID = 2101;

    private FusedLocationProviderClient fused;
    private LocationCallback callback;
    private boolean running = false;
    private android.content.BroadcastReceiver batteryReceiver;
    private boolean hasNotifiedLowBattery = false;
    private ChildEventListener bellListener;
    private ChildEventListener roomSosListener;
    private ValueEventListener forceLeaveListener;
    private ValueEventListener roomDeletedListener;
    private final android.os.Handler sosHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private volatile Runnable sosStopWatchdog;
    private final android.os.Handler fgHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private volatile Runnable fgEnsureRunnable;

    private String roomCode = "";
    private String uid = "";
    private String name = "";
    private String photoUrl = "";
    private String photoB64 = "";
    private String deviceId = "";

    // =========================================================================
    // SEKSYEN: ONCREATE
    // =========================================================================
    /** Inisialisasi aktiviti, bind view UI, dan setup listener. */
    @Override
    public void onCreate() {
        super.onCreate();
        ensureChannel();
        fused = LocationServices.getFusedLocationProviderClient(this);
        batteryReceiver = new android.content.BroadcastReceiver() {
            /** Fungsi untuk onReceive. */
    @Override
    public void onReceive(android.content.Context context, Intent intent) {
                if (Intent.ACTION_BATTERY_LOW.equals(intent.getAction()) || Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                    if (UserPrefs.isLowBatteryAlert(context)) {
                        int pct = BatteryUtils.getBatteryPct(context);
                        if (pct >= 0 && pct <= 15 && !hasNotifiedLowBattery) {
                            showLowBatteryNotification();
                            hasNotifiedLowBattery = true;
                        } else if (pct > 20) {
                            hasNotifiedLowBattery = false;
                        }
                    }
                }
            }
        };
        android.content.IntentFilter filter = new android.content.IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        try {
            registerReceiver(batteryReceiver, filter);
        } catch (Exception ignored) {}
    }

    /** Fungsi untuk onStartCommand. */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ensureChannel();
        String action = intent == null ? "" : String.valueOf(intent.getAction());
        if (ACTION_STOP.equals(action)) {
            stopTracking();
            stopForegroundNotificationWatchdog();
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }

        if (intent != null) {
            roomCode = String.valueOf(intent.getStringExtra(EXTRA_ROOM_CODE) == null ? "" : intent.getStringExtra(EXTRA_ROOM_CODE)).trim();
            uid = String.valueOf(intent.getStringExtra(EXTRA_UID) == null ? "" : intent.getStringExtra(EXTRA_UID)).trim();
            name = String.valueOf(intent.getStringExtra(EXTRA_NAME) == null ? "" : intent.getStringExtra(EXTRA_NAME)).trim();
            photoUrl = String.valueOf(intent.getStringExtra(EXTRA_PHOTO_URL) == null ? "" : intent.getStringExtra(EXTRA_PHOTO_URL)).trim();
            photoB64 = String.valueOf(intent.getStringExtra(EXTRA_PHOTO_B64) == null ? "" : intent.getStringExtra(EXTRA_PHOTO_B64)).trim();
        }
        deviceId = UserPrefs.getOrCreateDeviceId(this);

        Notification n = buildNotification(roomCode);
        try {
            if (Build.VERSION.SDK_INT >= 34) {
                if (PermissionUtils.hasAnyLocation(this)) {
                    startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
                } else {
                    startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
                }
            } else if (Build.VERSION.SDK_INT >= 29) {
                startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
            } else {
                startForeground(NOTIF_ID, n);
            }
        } catch (Exception e) {
            try {
                if (Build.VERSION.SDK_INT >= 34) {
                    startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
                } else {
                    startForeground(NOTIF_ID, n);
                }
            } catch (Exception ignored) {}
        }
        startForegroundNotificationWatchdog();
        try {
            android.util.Log.d("SOS_DEBUG", "LiveRoomTrackingService started");
        } catch (Exception ignored) {
        }
        logServiceEnvOnce("onStartCommand");

        startBellListener();
        startForceLeaveListener();
        startRoomDeletedListener();
        startRoomSosListener();
        startIncomingCallListener();
        startTracking();
        return START_STICKY;
    }

    // =========================================================================
    // SEKSYEN: ONDESTROY
    // =========================================================================
    /** Pembersihan memori, buang listener, dan tutup sambungan. */
    @Override
    public void onDestroy() {
        stopForegroundNotificationWatchdog();
        try {
            if (batteryReceiver != null) unregisterReceiver(batteryReceiver);
        } catch (Exception ignored) {}
        try {
            if (fused != null && callback != null) {
                fused.removeLocationUpdates(callback);
            }
        } catch (Exception ignored) {
        }
        stopBellListener();
        stopRoomSosListener();
        super.onDestroy();
    }

    /** Fungsi untuk startRoomDeletedListener. */
    private void startRoomDeletedListener() {
        if (roomDeletedListener != null) return;
        roomDeletedListener = FirebaseRoomClient.listenRoomDeleted(roomCode, () -> {
            try {
                android.util.Log.d("SOS_DEBUG", "Room deleted detected roomId=" + roomCode + " uid=" + uid);
            } catch (Exception ignored) {
            }
            new Thread(() -> {
                try {
                    FirebaseRoomClient.deleteUserRoom(uid, roomCode);
                } catch (Exception ignored) {
                }
                try {
                    UserPrefs.setActiveRoomCode(LiveRoomTrackingService.this, "");
                } catch (Exception ignored) {
                }
                try { stopTracking(); } catch (Exception ignored) {}
                try { stopBellListener(); } catch (Exception ignored) {}
                try { stopForceLeaveListener(); } catch (Exception ignored) {}
                try { stopRoomSosListener(); } catch (Exception ignored) {}
                try { stopRoomDeletedListener(); } catch (Exception ignored) {}
                try { stopIncomingCallListener(); } catch (Exception ignored) {}
                try { stopForegroundNotificationWatchdog(); } catch (Exception ignored) {}
                try { stopForeground(true); } catch (Exception ignored) {}
                try { stopSelf(); } catch (Exception ignored) {}
            }).start();
        });
    }

    /** Fungsi untuk stopRoomDeletedListener. */
    private void stopRoomDeletedListener() {
        if (roomDeletedListener == null) return;
        try {
            FirebaseRoomClient.removeRoomDeletedListener(roomCode, roomDeletedListener);
        } catch (Exception ignored) {
        }
        roomDeletedListener = null;
    }

    /** Fungsi untuk startForceLeaveListener. */
    private void startForceLeaveListener() {
        if (forceLeaveListener != null) return;
        forceLeaveListener = FirebaseRoomClient.listenForceLeave(roomCode, uid, (byUid, atMillis) -> {
            try {
                android.util.Log.d("SOS_DEBUG", "ForceLeave received roomId=" + roomCode + " uid=" + uid);
            } catch (Exception ignored) {
            }
            new Thread(() -> {
                try {
                    FirebaseRoomClient.deleteUserRoom(uid, roomCode);
                } catch (Exception ignored) {
                }
                FirebaseRoomClient.clearForceLeaveRequestQueued(roomCode, uid);
                try {
                    UserPrefs.setActiveRoomCode(LiveRoomTrackingService.this, "");
                } catch (Exception ignored) {
                }
                try {
                    stopTracking();
                } catch (Exception ignored) {
                }
                try {
                    stopBellListener();
                } catch (Exception ignored) {
                }
                try {
                    stopRoomSosListener();
                } catch (Exception ignored) {
                }
                try {
                    stopForegroundNotificationWatchdog();
                } catch (Exception ignored) {
                }
                try {
                    stopForeground(true);
                } catch (Exception ignored) {
                }
                try {
                    stopIncomingCallListener();
                } catch (Exception ignored) {
                }
                try {
                    stopSelf();
                } catch (Exception ignored) {
                }
            }).start();
        });
    }

    /** Fungsi untuk stopForceLeaveListener. */
    private void stopForceLeaveListener() {
        if (forceLeaveListener == null) return;
        try {
            FirebaseRoomClient.removeForceLeaveListener(roomCode, uid, forceLeaveListener);
        } catch (Exception ignored) {
        }
        forceLeaveListener = null;
    }

    /** Fungsi untuk startForegroundNotificationWatchdog. */
    private void startForegroundNotificationWatchdog() {

        stopForegroundNotificationWatchdog();
        fgEnsureRunnable = new Runnable() {
            /** Fungsi untuk run. */
    @Override
    public void run() {
                try {
                    Notification n = buildNotification(roomCode);
                    if (Build.VERSION.SDK_INT >= 29) {
                        startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
                    } else {
                        startForeground(NOTIF_ID, n);
                    }
                } catch (Exception ignored) {
                }
                fgHandler.postDelayed(this, 5000L);
            }
        };
        fgHandler.postDelayed(fgEnsureRunnable, 5000L);
    }

    /** Fungsi untuk stopForegroundNotificationWatchdog. */
    private void stopForegroundNotificationWatchdog() {
        try {
            Runnable r = fgEnsureRunnable;
            if (r != null) fgHandler.removeCallbacks(r);
        } catch (Exception ignored) {
        }
        fgEnsureRunnable = null;
    }

    /** Fungsi untuk onBind. */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /** Fungsi untuk startTracking. */
    private void startTracking() {

        startBellListener();
        startRoomSosListener();

        if (running) return;
        if (!PermissionUtils.hasAnyLocation(this)) return;
        if (!GpsUtils.isGpsEnabled(this)) return;

        FirebaseRoomClient.upsertMemberPresenceQueued(roomCode, uid, name, photoUrl, photoB64, BatteryUtils.getBatteryPct(this));

        boolean saver = UserPrefs.isBatterySaverMode(this);
        long interval = saver ? 30000L : 5000L;
        long minInterval = saver ? 15000L : 2500L;

        LocationRequest req = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval)
                .setMinUpdateIntervalMillis(minInterval)
                .setWaitForAccurateLocation(false)
                .build();

        if (callback == null) {
            callback = new LocationCallback() {
                /** Fungsi untuk onLocationResult. */
    @Override
    public void onLocationResult(LocationResult result) {
                    if (result == null) return;
                    android.location.Location loc = result.getLastLocation();
                    if (loc == null) return;
                    LatLng me = new LatLng(loc.getLatitude(), loc.getLongitude());
                    FirebaseRoomClient.publishLocationQueued(roomCode, uid, name, photoUrl, photoB64, me.latitude, me.longitude, BatteryUtils.getBatteryPct(LiveRoomTrackingService.this));
                }
            };
        }

        try {
            fused.requestLocationUpdates(req, callback, getMainLooper());
            running = true;
        } catch (SecurityException ignored) {
        }
    }

    /** Fungsi untuk stopTracking. */
    private void stopTracking() {
        if (!running) return;
        if (callback == null) return;
        try {
            fused.removeLocationUpdates(callback);
        } catch (Exception ignored) {
        }
        running = false;
    }

    /** Fungsi untuk startBellListener. */
    private void startBellListener() {
        if (bellListener != null) return;
        try {
            bellListener = FirebaseRoomClient.listenBells(roomCode, uid, deviceId, (fromUid, fromDeviceId, fromName, atMillis, id) -> {
                String from = String.valueOf(fromUid == null ? "" : fromUid).trim();
                String fromDev = String.valueOf(fromDeviceId == null ? "" : fromDeviceId).trim();
                String name = String.valueOf(fromName == null ? "" : fromName).trim();
                boolean sameAccount = uid != null && uid.trim().equals(from);
                boolean sameDevice = deviceId != null && !deviceId.trim().isEmpty() && deviceId.trim().equals(fromDev);
                if (sameAccount && sameDevice) {
                    FirebaseRoomClient.ackBell(roomCode, uid, id, deviceId);
                    return;
                }
                triggerVibrate();
                NotificationUtils.notifyBell(this, roomCode, name);
                FirebaseRoomClient.ackBell(roomCode, uid, id, deviceId);
            });
        } catch (Exception ignored) {
        }
    }

    /** Fungsi untuk stopBellListener. */
    private void stopBellListener() {
        if (bellListener == null) return;
        FirebaseRoomClient.removeBellListener(roomCode, uid, bellListener);
        bellListener = null;
    }

    /** Fungsi untuk startRoomSosListener. */
    private void startRoomSosListener() {
        if (roomSosListener != null) return;
        try {
            long localLastSeen = SosPrefs.getLastSeenAlertTime(this, roomCode);
            FirebaseRoomClient.fetchServerNowQueued(serverNow -> {
                long baseline = Math.max(serverNow, localLastSeen);

                SosPrefs.bumpBaselineAtListenerStart(LiveRoomTrackingService.this, roomCode, baseline);
                try {
                    android.util.Log.d("SOS_DEBUG", "Listener attached to roomId: " + roomCode + " (service)");
                } catch (Exception ignored) {
                }
                logServiceEnvOnce("listenerAttach");
                roomSosListener = FirebaseRoomClient.listenRoomSosSince(roomCode, deviceId, baseline, new FirebaseRoomClient.RoomSosHandler() {
                    /** Fungsi untuk onSos. */
    @Override
    public void onSos(String senderUid, String senderName, String roomId, long createdAtMs, String alertId, String status) {
                        String from = String.valueOf(senderUid == null ? "" : senderUid).trim();
                        String nameSafe = String.valueOf(senderName == null ? "" : senderName).trim();
                        String sosId = String.valueOf(alertId == null ? "" : alertId).trim();
                        long createdAt = Math.max(0L, createdAtMs);

                        if (uid != null && !uid.trim().isEmpty() && uid.trim().equals(from)) return;

                        if (!SosPrefs.tryMarkHandled(LiveRoomTrackingService.this, roomCode, sosId, createdAt)) return;

                        try {
                            android.util.Log.d("SOS_DEBUG", "SOS alert received in service: " + sosId + " createdAt=" + createdAt + " status=" + status);
                        } catch (Exception ignored) {
                        }
                        logServiceEnvOnce("handleSos");
                        try { NotificationHelper.ensureSosChannel(LiveRoomTrackingService.this); } catch (Exception ignored) {}

                        try {
                            UserPrefs.setPendingSosFocus(LiveRoomTrackingService.this, roomCode, from, sosId, createdAt);
                        } catch (Exception ignored) {
                        }

                        try {
                            long until = (createdAt > 0L ? createdAt : System.currentTimeMillis()) + 20_000L;
                            UserPrefs.setSosUiBlink(LiveRoomTrackingService.this, roomCode, from, sosId, until);
                        } catch (Exception ignored) {
                        }
                        NotificationHelper.showSosAlert(LiveRoomTrackingService.this, roomCode, nameSafe, from, sosId);
                        VibrateManager.startEmergency10s(LiveRoomTrackingService.this, sosId);
                        SosAudioManager.start10s(LiveRoomTrackingService.this, sosId);

                        try {
                            Runnable prev = sosStopWatchdog;
                            if (prev != null) sosHandler.removeCallbacks(prev);
                        } catch (Exception ignored) {
                        }
                        sosStopWatchdog = () -> {
                            try {
                                android.util.Log.d("SOS_DEBUG", "Service watchdog stopping vibration/sound after 10s. alertId=" + sosId);
                            } catch (Exception ignored) {
                            }
                            VibrateManager.stopAll(LiveRoomTrackingService.this);
                            SosAudioManager.stopAll();
                        };
                        sosHandler.postDelayed(sosStopWatchdog, 10_000L);
                    }

                    /** Fungsi untuk onSosCancelled. */
    @Override
    public void onSosCancelled(String senderUid, String roomId, String alertId, long cancelledAtMs) {
                        String sosId = String.valueOf(alertId == null ? "" : alertId).trim();
                        try {
                            android.util.Log.d("SOS_DEBUG", "SOS cancelled received: " + sosId);
                        } catch (Exception ignored) {
                        }
                        logServiceEnvOnce("handleCancel");

                        VibrateManager.stopAll(LiveRoomTrackingService.this);
                        SosAudioManager.stopAll();
                        try {
                            UserPrefs.clearSosUiBlinkIfAlert(LiveRoomTrackingService.this, roomCode, sosId);
                        } catch (Exception ignored) {
                        }
                        try {
                            Runnable prev = sosStopWatchdog;
                            if (prev != null) sosHandler.removeCallbacks(prev);
                        } catch (Exception ignored) {
                        }
                        sosStopWatchdog = null;
                        try {
                            android.util.Log.d("SOS_DEBUG", "Vibration stopped due to cancel. activeAlertId(now)=" + VibrateManager.getActiveAlertId());
                        } catch (Exception ignored) {
                        }

                    }
                });
            });
        } catch (Exception ignored) {
        }
    }

    private static volatile boolean loggedEnv = false;

    /** Fungsi untuk logServiceEnvOnce. */
    private void logServiceEnvOnce(String reason) {
        if (loggedEnv) return;
        loggedEnv = true;
        try {
            boolean notifEnabled = true;
            try {
                notifEnabled = androidx.core.app.NotificationManagerCompat.from(this).areNotificationsEnabled();
            } catch (Exception ignored) {
            }

            boolean postNotifGranted = true;
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                postNotifGranted = androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED;
            }

            android.os.Vibrator vib = (android.os.Build.VERSION.SDK_INT >= 31)
                    ? ((android.os.VibratorManager) getSystemService(VIBRATOR_MANAGER_SERVICE)).getDefaultVibrator()
                    : (android.os.Vibrator) getSystemService(VIBRATOR_SERVICE);
            boolean hasVibrator = vib != null && vib.hasVibrator();

            android.util.Log.d("SOS_DEBUG", "ENV reason=" + reason
                    + " roomId=" + roomCode
                    + " uid=" + uid
                    + " postNotifGranted=" + postNotifGranted
                    + " notifEnabled=" + notifEnabled
                    + " hasVibrator=" + hasVibrator
                    + " sdk=" + android.os.Build.VERSION.SDK_INT);
        } catch (Exception ignored) {
        }
    }

    /** Fungsi untuk stopRoomSosListener. */
    private void stopRoomSosListener() {
        if (roomSosListener == null) return;
        try {
            FirebaseRoomClient.removeRoomSosListener(roomCode, roomSosListener);
        } catch (Exception ignored) {
        }
        roomSosListener = null;
    }

    /** Fungsi untuk startIncomingCallListener. */
    private void startIncomingCallListener() {
        if (uid == null || uid.trim().isEmpty()) return;
        if (com.example.resqtap.call.IncomingCallManager.getInstance() != null) {
            com.example.resqtap.call.IncomingCallManager.getInstance().startListening(uid);
        }
    }

    /** Fungsi untuk stopIncomingCallListener. */
    private void stopIncomingCallListener() {
        // Do not stop globally if user is still logged in, IncomingCallManager handles lifecycle
    }

    /** Fungsi untuk triggerVibrate. */
    private void triggerVibrate() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 31) {
                android.os.VibratorManager vm = (android.os.VibratorManager) getSystemService(VIBRATOR_MANAGER_SERVICE);
                android.os.Vibrator v = vm == null ? null : vm.getDefaultVibrator();
                if (v == null) return;
                v.vibrate(android.os.VibrationEffect.createOneShot(300, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                android.os.Vibrator v = (android.os.Vibrator) getSystemService(VIBRATOR_SERVICE);
                if (v == null) return;
                if (android.os.Build.VERSION.SDK_INT >= 26) {
                    v.vibrate(android.os.VibrationEffect.createOneShot(300, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    v.vibrate(300);
                }
            }
        } catch (Exception ignored) {
        }
    }

    /** Fungsi untuk ensureChannel. */
    private void ensureChannel() {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm == null) return;
        NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID,
                "ResQTap Active",
                NotificationManager.IMPORTANCE_LOW
        );
        ch.setDescription("Keeps SOS listener active in the background.");
        nm.createNotificationChannel(ch);
    }

    /** Fungsi untuk buildNotification. */
    private Notification buildNotification(String room) {
        Intent open = new Intent(this, RoomMapActivity.class);
        open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        open.putExtra(RoomMapActivity.EXTRA_ROOM_CODE, room == null ? "" : room);
        PendingIntent openPi = PendingIntent.getActivity(
                this,
                0,
                open,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        Intent stop = new Intent(this, LiveRoomTrackingService.class);
        stop.setAction(ACTION_STOP);
        PendingIntent stopPi = PendingIntent.getService(
                this,
                1,
                stop,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        String title = "ResQTap active";
        String text = (room == null || room.trim().isEmpty()) ? "SOS listener active" : ("SOS listener active • Room: " + room.trim());
        Notification n = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_menu_location)
                .setContentTitle(title)
                .setContentText(text)
                .setOngoing(true)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .setSilent(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(openPi)
                .addAction(new NotificationCompat.Action(0, "Stop", stopPi))
                .build();
        try {

            n.flags |= Notification.FLAG_NO_CLEAR;
            n.flags |= Notification.FLAG_ONGOING_EVENT;
        } catch (Exception ignored) {
        }
        return n;
    }
}
