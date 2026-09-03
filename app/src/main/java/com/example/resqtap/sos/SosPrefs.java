package com.example.resqtap.sos;

import com.example.resqtap.utils.UserPrefs;

import android.content.Context;


/**
 * SosPrefs
 * Helper SharedPreferences untuk simpan setting SOS.
 */
public final class SosPrefs {
    private SosPrefs() {}

    /** Ambil atau muat data LastSeenAlertTime. */
    public static long getLastSeenAlertTime(Context context, String roomId) {
        return UserPrefs.getSosLastSeenAt(context, roomId);
    }

    /** Fungsi untuk setLastSeenAlertTime. */
    public static void setLastSeenAlertTime(Context context, String roomId, long createdAtMs) {
        UserPrefs.setSosLastSeenAt(context, roomId, createdAtMs);
    }

    /** Ambil atau muat data LastHandledAlertId. */
    public static String getLastHandledAlertId(Context context, String roomId) {
        return UserPrefs.getSosLastHandledId(context, roomId);
    }

    /** Fungsi untuk setLastHandledAlertId. */
    public static void setLastHandledAlertId(Context context, String roomId, String alertId) {
        UserPrefs.setSosLastHandledId(context, roomId, alertId);
    }

    /** Fungsi untuk bumpBaselineAtListenerStart. */
    public static void bumpBaselineAtListenerStart(Context context, String roomId, long serverNowMsOrLocalNow) {
        long now = Math.max(0L, serverNowMsOrLocalNow);
        long lastSeen = getLastSeenAlertTime(context, roomId);

        setLastSeenAlertTime(context, roomId, Math.max(lastSeen, now));
    }

    /** Fungsi untuk tryMarkHandled. */
    public static synchronized boolean tryMarkHandled(Context context, String roomId, String alertId, long createdAtMs) {
        if (context == null) return false;
        String room = String.valueOf(roomId == null ? "" : roomId).trim().toUpperCase(java.util.Locale.ROOT);
        String id = String.valueOf(alertId == null ? "" : alertId).trim();
        long createdAt = Math.max(0L, createdAtMs);
        if (room.isEmpty() || id.isEmpty() || createdAt <= 0L) return false;

        try {
            long now = System.currentTimeMillis();
            if (now > 0L && createdAt > 0L) {
                long age = Math.abs(now - createdAt);
                if (age > 2L * 60L * 1000L) return false;
            }
        } catch (Exception ignored) {
        }

        String lastId = getLastHandledAlertId(context, room);
        if (!lastId.isEmpty() && lastId.equals(id)) return false;

        long lastSeen = getLastSeenAlertTime(context, room);
        if (createdAt <= lastSeen) return false;

        setLastHandledAlertId(context, room, id);
        setLastSeenAlertTime(context, room, createdAt);
        return true;
    }
}
