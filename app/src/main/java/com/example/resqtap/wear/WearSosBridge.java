package com.example.resqtap.wear;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * WearSosBridge
 * Bridge sambungan ke Wear OS watch guna Google Play Services Wearable API.
 */
public final class WearSosBridge {
    public static final String SOS_MESSAGE_PATH = "/resqtap/sos";
    public static final String SOS_DATA_PATH = "/resqtap/sos_data";

    private static final String PREFS = "wear_sos_bridge";
    private static final String KEY_PENDING_AT = "pending_at";
    private static final String EXTRA_TRIGGER_WATCH_SOS = "com.example.resqtap.EXTRA_TRIGGER_WATCH_SOS";

    private WearSosBridge() {
    }

    /** Fungsi untuk triggerExtra. */
    public static String triggerExtra() {
        return EXTRA_TRIGGER_WATCH_SOS;
    }

    /** Fungsi untuk markPending. */
    public static void markPending(Context context) {
        if (context == null) return;
        prefs(context).edit().putLong(KEY_PENDING_AT, System.currentTimeMillis()).apply();
    }

    /** Fungsi untuk consumePending. */
    public static boolean consumePending(Context context) {
        if (context == null) return false;
        SharedPreferences p = prefs(context);
        long pendingAt = p.getLong(KEY_PENDING_AT, 0L);
        if (pendingAt <= 0L) return false;
        p.edit().remove(KEY_PENDING_AT).apply();
        return true;
    }

    /** Fungsi untuk prefs. */
    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
