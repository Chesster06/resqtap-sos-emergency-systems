package com.example.resqtap.sos;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.resqtap.R;
import com.example.resqtap.room.FirebaseRoomClient;
import com.example.resqtap.utils.LocaleUtils;
import com.example.resqtap.utils.PermissionUtils;
import com.example.resqtap.utils.ThemeUtils;
import com.example.resqtap.utils.UserPrefs;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * SendingAlarmActivity
 * Skrin utama apabila penggera kecemasan sedang dihantar (Selepas countdown 10 saat selesai):
 * Menyalakan siren kecemasan & getaran tempatan, memancarkan amaran ke Firebase (Admin & Rooms),
 * menyegerakkan koordinat GPS secara langsung, dan menyediakan "Slide to cancel SOS" slider.
 */
public class SendingAlarmActivity extends AppCompatActivity {

    public static final String EXTRA_TARGET_ROOM = "targetRoom";
    public static final String EXTRA_LAT = "lat";
    public static final String EXTRA_LNG = "lng";

    /** Bendera global untuk menyekat peranti ini daripada memaparkan skrin penerima SosAlarmActivity sendiri */
    public static volatile boolean isSendingAlarm = false;

    private String targetRoomCode = "";
    private final Map<String, String> activeSosIds = new HashMap<>();
    private final Map<DatabaseReference, ValueEventListener> reservationListeners = new HashMap<>();

    private View pulseRing;
    private CircularProgressView progressDial;
    private TextView tvDestinations;
    private TextView tvGpsCoords;
    private TextView tvAdminState;
    private MaterialButton btnOpenChat;
    private SeekBar seekCancel;

    private ObjectAnimator pulseAnimator;
    private boolean isSosCancelled = false;

    private FusedLocationProviderClient fusedLocationClient;
    private double lastKnownLat = 0.0;
    private double lastKnownLng = 0.0;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void attachBaseContext(Context newBase) {
        Context wrapped = LocaleUtils.wrap(newBase);
        super.attachBaseContext(wrapped == null ? newBase : wrapped);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeUtils.applySavedNightMode(this);
        super.onCreate(savedInstanceState);

        // Kunci bendera penyiaran aktif
        isSendingAlarm = true;

        // Pastikan skrin hidup dan dipaparkan di atas lockscreen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (km != null) {
                km.requestDismissKeyguard(this, null);
            }
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            );
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_sending_alarm);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(EXTRA_TARGET_ROOM)) {
                targetRoomCode = String.valueOf(intent.getStringExtra(EXTRA_TARGET_ROOM)).trim();
            }
            if (intent.hasExtra(EXTRA_LAT)) {
                lastKnownLat = intent.getDoubleExtra(EXTRA_LAT, 0.0);
            }
            if (intent.hasExtra(EXTRA_LNG)) {
                lastKnownLng = intent.getDoubleExtra(EXTRA_LNG, 0.0);
            }
        }

        bindViews();
        silenceLocalDevice();
        startDialAnimation();
        initGpsAndDispatchSos();
    }

    private void bindViews() {
        pulseRing = findViewById(R.id.sending_alarm_dial_base);
        progressDial = findViewById(R.id.sending_alarm_progress_dial);
        tvDestinations = findViewById(R.id.tv_sending_alarm_destinations);
        tvGpsCoords = findViewById(R.id.tv_sending_alarm_gps_coords);
        tvAdminState = findViewById(R.id.tv_sending_alarm_admin_state);
        btnOpenChat = findViewById(R.id.btn_sending_alarm_open_chat);
        seekCancel = findViewById(R.id.seek_sending_alarm_cancel);

        if (btnOpenChat != null) {
            btnOpenChat.setOnClickListener(v -> openEmergencyChat());
        }

        setupCancelSlider();
    }

    /** Memastikan peranti penghantar kekal senyap (tiada siren/getaran membingitkan pada telefon mangsa) */
    private void silenceLocalDevice() {
        try {
            SosAudioManager.stopAll();
            VibrateManager.stopAll(this);
        } catch (Exception ignored) {}
    }

    private void startDialAnimation() {
        if (pulseRing != null) {
            pulseAnimator = ObjectAnimator.ofFloat(pulseRing, "scaleX", 1.0f, 1.12f, 1.0f);
            pulseAnimator.setDuration(1200);
            pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
            pulseAnimator.setRepeatMode(ValueAnimator.RESTART);
            pulseAnimator.start();

            ObjectAnimator pulseY = ObjectAnimator.ofFloat(pulseRing, "scaleY", 1.0f, 1.12f, 1.0f);
            pulseY.setDuration(1200);
            pulseY.setRepeatCount(ValueAnimator.INFINITE);
            pulseY.setRepeatMode(ValueAnimator.RESTART);
            pulseY.start();
        }

        if (progressDial != null) {
            progressDial.setProgress(1.0f);
        }
    }

    private void initGpsAndDispatchSos() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Sila log masuk untuk menghantar SOS", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final String uid = user.getUid();
        final String deviceId = UserPrefs.getOrCreateDeviceId(this);
        final String rawName = UserPrefs.getName(this);
        final String name = (rawName == null || rawName.trim().isEmpty()) ? "User" : rawName.trim();

        if (lastKnownLat != 0.0 && lastKnownLng != 0.0) {
            if (tvGpsCoords != null) {
                tvGpsCoords.setText(String.format(Locale.getDefault(), "GPS: %.5f, %.5f", lastKnownLat, lastKnownLng));
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (PermissionUtils.hasAnyLocation(this)) {
            try {
                fusedLocationClient.getLastLocation().addOnSuccessListener(loc -> {
                    if (loc != null) {
                        lastKnownLat = loc.getLatitude();
                        lastKnownLng = loc.getLongitude();
                        if (tvGpsCoords != null) {
                            tvGpsCoords.setText(String.format(Locale.getDefault(), "GPS: %.5f, %.5f", lastKnownLat, lastKnownLng));
                        }
                    }
                    dispatchAlertToFirebase(uid, deviceId, name, lastKnownLat, lastKnownLng);
                }).addOnFailureListener(e -> {
                    dispatchAlertToFirebase(uid, deviceId, name, lastKnownLat, lastKnownLng);
                });
                return;
            } catch (SecurityException ignored) {}
        }

        dispatchAlertToFirebase(uid, deviceId, name, lastKnownLat, lastKnownLng);
    }

    private void dispatchAlertToFirebase(String uid, String dev, String name, double lat, double lng) {
        if (isSosCancelled) return;

        // Sekiranya dipanggil dari bilik spesifik
        if (targetRoomCode != null && !targetRoomCode.trim().isEmpty()) {
            final String code = targetRoomCode.trim().toUpperCase(Locale.ROOT);
            sendSosToRoom(code, uid, dev, name, lat, lng);
            if (tvDestinations != null) {
                tvDestinations.setText(getString(R.string.sos_room_badge_format, code, "Admin"));
            }
            return;
        }

        // Dapatkan semua bilik pengguna; jika tiada, hantar ke DIRECT
        FirebaseDatabase.getInstance(FirebaseRoomClient.DATABASE_URL)
                .getReference("userRooms")
                .child(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (isSosCancelled) return;

                    List<String> roomCodes = new ArrayList<>();
                    if (snapshot != null && snapshot.exists()) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            if (child == null || child.getKey() == null) continue;
                            final String code = child.getKey().trim().toUpperCase(Locale.ROOT);
                            if (!code.isEmpty()) {
                                roomCodes.add(code);
                            }
                        }
                    }

                    if (roomCodes.isEmpty()) {
                        roomCodes.add("DIRECT");
                    }

                    if (tvDestinations != null) {
                        if (roomCodes.contains("DIRECT")) {
                            tvDestinations.setText("Saluran Kecemasan Langsung (DIRECT) & Admin");
                        } else {
                            tvDestinations.setText(roomCodes.size() + " Bilik Keselamatan & Admin Diberitahu");
                        }
                    }

                    for (String code : roomCodes) {
                        if (isSosCancelled) return;
                        sendSosToRoom(code, uid, dev, name, lat, lng);
                    }
                })
                .addOnFailureListener(e -> {
                    if (isSosCancelled) return;
                    sendSosToRoom("DIRECT", uid, dev, name, lat, lng);
                    if (tvDestinations != null) {
                        tvDestinations.setText("Saluran Langsung (DIRECT) & Admin");
                    }
                });
    }

    private void sendSosToRoom(String code, String uid, String dev, String name, double lat, double lng) {
        FirebaseRoomClient.sendRoomSosQueued(code, uid, dev, name, lat, lng, id -> {
            String sosId = String.valueOf(id == null ? "" : id).trim();
            if (sosId.isEmpty()) return;

            if (isSosCancelled) {
                FirebaseRoomClient.cancelRoomSosQueued(code, sosId, dev);
            } else {
                activeSosIds.put(code, sosId);
                // Simpan rekod aktif dalam UserPrefs
                UserPrefs.setActiveSosProgress(SendingAlarmActivity.this, code, sosId);
                // Pantau reservation status dari Admin
                watchSosCaseReservation(code, sosId);
            }
        });
    }

    private void watchSosCaseReservation(String roomCode, String sosAlertId) {
        if (roomCode == null || sosAlertId == null || roomCode.isEmpty() || sosAlertId.isEmpty()) return;
        DatabaseReference ref = FirebaseDatabase.getInstance(FirebaseRoomClient.DATABASE_URL)
                .getReference("rooms")
                .child(roomCode.toUpperCase(Locale.ROOT))
                .child("sosAlerts")
                .child(sosAlertId);

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot == null || !snapshot.exists() || isSosCancelled) return;
                Boolean served = snapshot.child("served").getValue(Boolean.class);
                String servedBy = snapshot.child("servedBy").getValue(String.class);
                String servedByName = snapshot.child("servedByName").getValue(String.class);

                if ((served != null && served) || (servedBy != null && !servedBy.trim().isEmpty())) {
                    stopWatchingReservations();
                    String responder = (servedByName != null && !servedByName.trim().isEmpty()) ? servedByName : "Admin Responder";
                    if (tvAdminState != null) {
                        tvAdminState.setText(getString(R.string.sending_alarm_admin_served, responder));
                        tvAdminState.setTextColor(0xFF10B981);
                    }

                    // Hentikan siren audio seketika responder mengambil kes
                    try {
                        SosAudioManager.stopAll();
                        VibrateManager.stopAll(SendingAlarmActivity.this);
                    } catch (Exception ignored) {}

                    // Buka SosProgressActivity secara automatik
                    handler.postDelayed(() -> {
                        if (!isFinishing() && !isDestroyed()) {
                            isSendingAlarm = false;
                            SosProgressActivity.launch(SendingAlarmActivity.this, roomCode, sosAlertId);
                            finish();
                        }
                    }, 1200);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        };

        synchronized (reservationListeners) {
            reservationListeners.put(ref, listener);
        }
        ref.addValueEventListener(listener);
    }

    private void stopWatchingReservations() {
        synchronized (reservationListeners) {
            for (Map.Entry<DatabaseReference, ValueEventListener> entry : reservationListeners.entrySet()) {
                try {
                    entry.getKey().removeEventListener(entry.getValue());
                } catch (Exception ignored) {}
            }
            reservationListeners.clear();
        }
    }

    private void setupCancelSlider() {
        if (seekCancel == null) return;
        seekCancel.setProgress(0);
        seekCancel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                if (progress >= 95) {
                    cancelSosAndExit();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (seekBar.getProgress() < 95) {
                    seekBar.setProgress(0);
                }
            }
        });
    }

    private void cancelSosAndExit() {
        if (isSosCancelled) return;
        isSosCancelled = true;
        isSendingAlarm = false;

        stopWatchingReservations();

        // 1. Matikan siren & getaran serta-merta
        try {
            SosAudioManager.stopAll();
            VibrateManager.stopAll(this);
        } catch (Exception ignored) {}

        // 2. Batalkan kes di Firebase untuk semua room
        final String dev = UserPrefs.getOrCreateDeviceId(this);
        for (Map.Entry<String, String> entry : new HashMap<>(activeSosIds).entrySet()) {
            if (!entry.getKey().isEmpty() && !entry.getValue().isEmpty()) {
                FirebaseRoomClient.cancelRoomSosQueued(entry.getKey(), entry.getValue(), dev);
            }
        }
        activeSosIds.clear();

        // 3. Batalkan apa-apa alert aktif pengguna
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            final String uid = user.getUid();
            FirebaseRoomClient.cancelAllActiveSosForUser(uid, dev);
            handler.postDelayed(() -> {
                try {
                    FirebaseRoomClient.cancelAllActiveSosForUser(uid, dev);
                } catch (Exception ignored) {}
            }, 1000);
        }

        // 4. Bersihkan UserPrefs
        UserPrefs.clearActiveSosProgress(this);

        Toast.makeText(this, R.string.sending_alarm_cancelled_success, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void openEmergencyChat() {
        String activeRoom = "";
        String activeAlert = "";
        if (!activeSosIds.isEmpty()) {
            Map.Entry<String, String> first = activeSosIds.entrySet().iterator().next();
            activeRoom = first.getKey();
            activeAlert = first.getValue();
        } else {
            activeRoom = UserPrefs.getActiveSosProgressRoom(this);
            activeAlert = UserPrefs.getActiveSosProgressAlert(this);
        }

        SosLivechatActivity.launch(this, activeRoom, activeAlert, "Admin Responder", "");
    }

    @Override
    protected void onDestroy() {
        isSendingAlarm = false;
        stopWatchingReservations();
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
            pulseAnimator = null;
        }
        if (isFinishing() || isSosCancelled) {
            try {
                SosAudioManager.stopAll();
                VibrateManager.stopAll(this);
            } catch (Exception ignored) {}
        }
        super.onDestroy();
    }
}
