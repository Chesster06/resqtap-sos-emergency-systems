package com.example.resqtap.notification;
import com.example.resqtap.R;

import com.example.resqtap.home.MainActivity;
import com.example.resqtap.room.RoomMapActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


/**
 * NotificationHelper
 * Setup notification channels Android (SOS, Call, Room, Notice).
 */
public final class NotificationHelper {
    private NotificationHelper() {}

    public static final String CHANNEL_ID_SOS = "sos_emergency_v3";
    public static final String CHANNEL_ID_LOCAL = "local_notifications_v1";
    private static final String SOS_TAG = "sos_alert";

    /** Fungsi untuk createNotificationChannel. */
    public static void createNotificationChannel(Context context) {
        if (context == null) return;
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        if (nm.getNotificationChannel(CHANNEL_ID_LOCAL) != null) return;

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID_LOCAL,
                "Local Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription("Notifications shown locally by the app.");
        nm.createNotificationChannel(channel);
    }

    /** Paparkan LocalNotification. */
    public static void showLocalNotification(Context context, String title, String text) {
        if (context == null) return;
        createNotificationChannel(context);

        String safeTitle = title == null || title.trim().isEmpty() ? "ResQTap" : title.trim();
        String safeText = text == null ? "" : text.trim();

        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openPendingIntent = PendingIntent.getActivity(
                context,
                400,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_LOCAL)
                .setSmallIcon(R.drawable.ic_bell)
                .setContentTitle(safeTitle)
                .setContentText(safeText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(safeText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(openPendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (!notificationManager.areNotificationsEnabled()) return;
        notificationManager.notify((int) (System.currentTimeMillis() % Integer.MAX_VALUE), builder.build());
    }

    /** Fungsi untuk ensureSosChannel. */
    public static void ensureSosChannel(Context context) {
        if (context == null) return;
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        NotificationChannel existing = nm.getNotificationChannel(CHANNEL_ID_SOS);
        if (existing != null) {

            try {
                existing.enableVibration(false);
                existing.setVibrationPattern(null);
                nm.createNotificationChannel(existing);
            } catch (Exception ignored) {
            }
            return;
        }

        NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID_SOS,
                "SOS Emergency Alerts",
                NotificationManager.IMPORTANCE_HIGH
        );
        ch.setDescription("High priority SOS emergency alerts.");

        ch.enableVibration(false);
        ch.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);

        ch.setSound(null, null);

        nm.createNotificationChannel(ch);
    }

    /** Paparkan SosAlert. */
    public static void showSosAlert(Context context, String roomId, String senderName, String senderUid, String alertId) {
        if (context == null) return;
        ensureSosChannel(context);

        String safeRoom = String.valueOf(roomId == null ? "" : roomId).trim();
        String safeName = String.valueOf(senderName == null ? "" : senderName).trim();
        if ("null".equalsIgnoreCase(safeName)) safeName = "";

        String title = "SOS ALERT";
        String text = safeName.isEmpty()
                ? "Emergency SOS in your room."
                : (safeName + " triggered SOS in your room.");

        Intent open = new Intent(context, RoomMapActivity.class);
        open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (!safeRoom.isEmpty()) open.putExtra(RoomMapActivity.EXTRA_ROOM_CODE, safeRoom);
        String focusUid = String.valueOf(senderUid == null ? "" : senderUid).trim();
        String aId = String.valueOf(alertId == null ? "" : alertId).trim();
        if (!focusUid.isEmpty()) open.putExtra(RoomMapActivity.EXTRA_FOCUS_UID, focusUid);
        if (!aId.isEmpty()) open.putExtra(RoomMapActivity.EXTRA_SOS_ALERT_ID, aId);
        PendingIntent openPi = PendingIntent.getActivity(
                context,
                200,
                open,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_ID_SOS)
                .setSmallIcon(R.drawable.ic_error_24)
                .setContentTitle(title)
                .setContentText(text)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(openPi)

                .setSilent(true)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
                .setVibrate(new long[]{0});

        try {
            NotificationManagerCompat nm = NotificationManagerCompat.from(context);
            if (!nm.areNotificationsEnabled()) {
                android.util.Log.d("SOS_DEBUG", "Notifications disabled at OS level; cannot post SOS notification");
                return;
            }
            nm.notify(SOS_TAG, notificationIdForAlert(aId), b.build());
        } catch (Exception e) {
            try {
                android.util.Log.d("SOS_DEBUG", "notify failed: " + e.getMessage());
            } catch (Exception ignored) {
            }
        }
    }

    /** Fungsi untuk cancelSos. */
    public static void cancelSos(Context context, String alertId) {
        if (context == null) return;
        String aId = String.valueOf(alertId == null ? "" : alertId).trim();
        NotificationManagerCompat.from(context).cancel(SOS_TAG, notificationIdForAlert(aId));
    }

    /** Fungsi untuk notificationIdForAlert. */
    private static int notificationIdForAlert(String alertId) {
        String aId = String.valueOf(alertId == null ? "" : alertId).trim();
        if (aId.isEmpty()) {

            return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        }

        return 10000 + (aId.hashCode() & 0x7fffffff);
    }
}
