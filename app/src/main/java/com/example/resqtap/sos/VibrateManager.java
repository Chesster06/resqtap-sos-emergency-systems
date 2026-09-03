package com.example.resqtap.sos;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;


/**
 * VibrateManager
 * Controller getaran: handle vibration pattern waktu SOS aktif.
 */
public final class VibrateManager {
    private VibrateManager() {}

    private static final Object LOCK = new Object();
    private static volatile String activeAlertId = "";
    private static final android.os.Handler MAIN = new android.os.Handler(android.os.Looper.getMainLooper());
    private static volatile Runnable stopRunnable;

    /** Ambil atau muat data ActiveAlertId. */
    public static String getActiveAlertId() {
        return activeAlertId == null ? "" : activeAlertId;
    }

    /** Fungsi untuk startEmergency10s. */
    public static void startEmergency10s(Context context, String alertId) {
        if (context == null) return;
        String id = String.valueOf(alertId == null ? "" : alertId).trim();
        if (id.isEmpty()) return;

        synchronized (LOCK) {
            activeAlertId = id;
        }

        try { SosAudioManager.stopAll(); } catch (Exception ignored) {}

        try {
            Vibrator v = getVibrator(context);
            if (v == null) return;
            try {
                if (!v.hasVibrator()) {
                    android.util.Log.d("SOS_DEBUG", "No vibrator available on this device");
                    return;
                }
            } catch (Exception ignored) {
            }

            try { v.cancel(); } catch (Exception ignored) {}

            long[] pattern = new long[]{0, 1000, 250};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(pattern, 0));
            } else {
                v.vibrate(pattern, 0);
            }

            try {
                Runnable prev = stopRunnable;
                if (prev != null) MAIN.removeCallbacks(prev);
            } catch (Exception ignored) {
            }
            stopRunnable = () -> {
                try {
                    android.util.Log.d("SOS_DEBUG", "VibrateManager auto-stop after 10s for alertId=" + id);
                } catch (Exception ignored) {
                }

                stopAll(context.getApplicationContext());
            };
            MAIN.postDelayed(stopRunnable, 10_000L);
        } catch (Exception ignored) {
        }
    }

    /** Fungsi untuk stop. */
    public static void stop(Context context, String alertId) {
        if (context == null) return;
        String id = String.valueOf(alertId == null ? "" : alertId).trim();
        if (id.isEmpty()) return;

        synchronized (LOCK) {
            if (!id.equals(activeAlertId)) return;
            activeAlertId = "";
        }
        try {
            Runnable prev = stopRunnable;
            if (prev != null) MAIN.removeCallbacks(prev);
        } catch (Exception ignored) {
        }
        stopRunnable = null;
        try { SosAudioManager.stopAll(); } catch (Exception ignored) {}
        try {
            Vibrator v = getVibrator(context);
            if (v != null) v.cancel();
        } catch (Exception ignored) {
        }
    }

    /** Fungsi untuk stopAll. */
    public static void stopAll(Context context) {
        if (context == null) return;
        synchronized (LOCK) {
            activeAlertId = "";
        }
        try {
            Runnable prev = stopRunnable;
            if (prev != null) MAIN.removeCallbacks(prev);
        } catch (Exception ignored) {
        }
        stopRunnable = null;
        try { SosAudioManager.stopAll(); } catch (Exception ignored) {}
        try {
            Vibrator v = getVibrator(context);
            if (v != null) v.cancel();
        } catch (Exception ignored) {
        }
        try {
            android.util.Log.d("SOS_DEBUG", "VibrateManager.stopAll() called");
        } catch (Exception ignored) {
        }
    }

    /** Ambil atau muat data Vibrator. */
    private static Vibrator getVibrator(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= 31) {
                VibratorManager vm = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                return vm == null ? null : vm.getDefaultVibrator();
            }
            return (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        } catch (Exception e) {
            return null;
        }
    }
}
