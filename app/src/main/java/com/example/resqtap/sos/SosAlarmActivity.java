package com.example.resqtap.sos;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.resqtap.R;
import com.example.resqtap.home.MainActivity;
import com.example.resqtap.sos.SosAudioManager;
import com.example.resqtap.sos.VibrateManager;
import com.example.resqtap.notification.NotificationHelper;
import com.example.resqtap.room.RoomMapActivity;
import com.example.resqtap.friend.FirebaseFriendClient;
import com.example.resqtap.utils.AvatarUtils;
import com.example.resqtap.utils.LocaleUtils;
import com.google.android.material.button.MaterialButton;

/**
 * SosAlarmActivity
 * Skrin UI Penggera SOS Masuk: dipaparkan di atas skrin kunci apabila penggera dicetuskan.
 * Menyediakan "Slide to snooze alarm" slider untuk mematikan siren dan getaran dengan serta-merta.
 */
public class SosAlarmActivity extends AppCompatActivity {

    public static final String EXTRA_ROOM_CODE = "roomCode";
    public static final String EXTRA_ROOM_NAME = "roomName";
    public static final String EXTRA_SENDER_NAME = "senderName";
    public static final String EXTRA_SENDER_UID = "senderUid";
    public static final String EXTRA_ALERT_ID = "alertId";

    private String roomCode = "";
    private String roomName = "";
    private String senderName = "";
    private String senderUid = "";
    private String alertId = "";

    private View pulseRing;
    private CircularProgressView circularProgressDial;
    private TextView tvSender;
    private TextView tvSubtitle;
    private TextView tvRoom;
    private TextView tvStatus;
    private View cardSlideContainer;
    private SeekBar seekSnooze;
    private View layoutSnoozedActions;
    private MaterialButton btnDismiss;
    private MaterialButton btnViewMap;

    private ObjectAnimator pulseAnimator;
    private ObjectAnimator progressAnimator;
    private boolean isSnoozed = false;

    private final Handler autoSnoozeHandler = new Handler(Looper.getMainLooper());
    private final Runnable autoSnoozeRunnable = () -> {
        if (!isFinishing() && !isDestroyed() && !isSnoozed) {
            autoSnoozeTimerFinished();
        }
    };

    private final BroadcastReceiver sosStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) return;
            String action = intent.getAction();

            if ("com.example.resqtap.SOS_CANCELLED".equals(action)) {
                String cancelledAlertId = intent.getStringExtra("alertId");
                if (alertId.isEmpty() || alertId.equals(cancelledAlertId)) {
                    Log.d("SOS_DEBUG", "SosAlarmActivity auto-dismissing: SOS was cancelled by sender.");
                    finishAndRemoveTask();
                }
            } else if (SosSnoozeReceiver.ACTION_SOS_SNOOZED.equals(action)) {
                String snoozedAlertId = intent.getStringExtra(SosSnoozeReceiver.EXTRA_ALERT_ID);
                if (alertId.isEmpty() || alertId.equals(snoozedAlertId)) {
                    Log.d("SOS_DEBUG", "SosAlarmActivity: Alarm snoozed externally.");
                    onAlarmSnoozedUi();
                }
            }
        }
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        Context wrapped = LocaleUtils.wrap(newBase);
        super.attachBaseContext(wrapped == null ? newBase : wrapped);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Konfigurasi paparan di atas skrin kunci (Show on Lockscreen & Turn Screen On)
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

        setContentView(R.layout.activity_sos_alarm);

        // Ekstrak maklumat dari Intent
        Intent intent = getIntent();
        if (intent != null) {
            roomCode = String.valueOf(intent.getStringExtra(EXTRA_ROOM_CODE) == null ? "" : intent.getStringExtra(EXTRA_ROOM_CODE)).trim();
            roomName = String.valueOf(intent.getStringExtra(EXTRA_ROOM_NAME) == null ? "" : intent.getStringExtra(EXTRA_ROOM_NAME)).trim();
            senderName = String.valueOf(intent.getStringExtra(EXTRA_SENDER_NAME) == null ? "" : intent.getStringExtra(EXTRA_SENDER_NAME)).trim();
            senderUid = String.valueOf(intent.getStringExtra(EXTRA_SENDER_UID) == null ? "" : intent.getStringExtra(EXTRA_SENDER_UID)).trim();
            alertId = String.valueOf(intent.getStringExtra(EXTRA_ALERT_ID) == null ? "" : intent.getStringExtra(EXTRA_ALERT_ID)).trim();
        }

        bindViews();
        setupData();
        setupSlider();

        // Daftarkan listener pembatalan / snooze dari luar
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.resqtap.SOS_CANCELLED");
        filter.addAction(SosSnoozeReceiver.ACTION_SOS_SNOOZED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(sosStatusReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(sosStatusReceiver, filter);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null) {
            roomCode = String.valueOf(intent.getStringExtra(EXTRA_ROOM_CODE) == null ? "" : intent.getStringExtra(EXTRA_ROOM_CODE)).trim();
            roomName = String.valueOf(intent.getStringExtra(EXTRA_ROOM_NAME) == null ? "" : intent.getStringExtra(EXTRA_ROOM_NAME)).trim();
            senderName = String.valueOf(intent.getStringExtra(EXTRA_SENDER_NAME) == null ? "" : intent.getStringExtra(EXTRA_SENDER_NAME)).trim();
            senderUid = String.valueOf(intent.getStringExtra(EXTRA_SENDER_UID) == null ? "" : intent.getStringExtra(EXTRA_SENDER_UID)).trim();
            alertId = String.valueOf(intent.getStringExtra(EXTRA_ALERT_ID) == null ? "" : intent.getStringExtra(EXTRA_ALERT_ID)).trim();
        }
        isSnoozed = false;
        if (cardSlideContainer != null) cardSlideContainer.setVisibility(View.VISIBLE);
        if (layoutSnoozedActions != null) layoutSnoozedActions.setVisibility(View.GONE);
        if (tvStatus != null) tvStatus.setVisibility(View.GONE);
        if (seekSnooze != null) seekSnooze.setProgress(0);
        setupData();
        startDialProgressAnimation();
    }

    private ImageView ivSenderAvatar;
    private TextView tvSenderRoomInfo;

    private void bindViews() {
        pulseRing = findViewById(R.id.sos_alarm_dial_base);
        circularProgressDial = findViewById(R.id.circular_progress_dial);
        tvSender = findViewById(R.id.tv_sos_alarm_sender);
        ivSenderAvatar = findViewById(R.id.iv_sos_sender_avatar);
        tvSenderRoomInfo = findViewById(R.id.tv_sos_sender_room_info);
        tvStatus = findViewById(R.id.tv_sos_alarm_status);
        cardSlideContainer = findViewById(R.id.card_slide_container);
        seekSnooze = findViewById(R.id.sos_alarm_seek);
        layoutSnoozedActions = findViewById(R.id.layout_snoozed_actions);
        btnDismiss = findViewById(R.id.btn_sos_dismiss);
        btnViewMap = findViewById(R.id.btn_sos_view_map);

        startDialProgressAnimation();
    }

    private void setupData() {
        String displayName = senderName.isEmpty() ? getString(R.string.sos_alert_title) : senderName;
        if (tvSender != null) {
            tvSender.setText(displayName);
        }

        if (tvSenderRoomInfo != null) {
            String roomDisplay = !roomName.isEmpty() ? roomName : roomCode;
            if (!roomDisplay.isEmpty()) {
                tvSenderRoomInfo.setText(getString(R.string.sos_alarm_room_format, roomDisplay));
                tvSenderRoomInfo.setVisibility(View.VISIBLE);
            } else {
                tvSenderRoomInfo.setVisibility(View.GONE);
            }
        }

        // Muat turun avatar / info pengguna jika senderUid tersedia
        if (!senderUid.isEmpty() && ivSenderAvatar != null) {
            FirebaseFriendClient.fetchUserDetailQueued(senderUid, detail -> {
                if (isFinishing() || isDestroyed()) return;
                if (detail != null) {
                    if (senderName.isEmpty() && !detail.name.isEmpty() && tvSender != null) {
                        tvSender.setText(detail.name);
                    }
                    AvatarUtils.applyAvatar(ivSenderAvatar, "", "", "", R.drawable.ic_avatar);
                }
            });
        }

        btnDismiss.setOnClickListener(v -> finishAndRemoveTask());

        btnViewMap.setOnClickListener(v -> {
            Intent mapIntent = new Intent(this, RoomMapActivity.class);
            mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if (!roomCode.isEmpty()) mapIntent.putExtra(RoomMapActivity.EXTRA_ROOM_CODE, roomCode);
            if (!senderUid.isEmpty()) mapIntent.putExtra(RoomMapActivity.EXTRA_FOCUS_UID, senderUid);
            if (!alertId.isEmpty()) mapIntent.putExtra(RoomMapActivity.EXTRA_SOS_ALERT_ID, alertId);
            startActivity(mapIntent);
            finish();
        });
    }

    private void setupSlider() {
        seekSnooze.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && progress >= 90 && !isSnoozed) {
                    // Berjaya slide ke hujung -> Snooze penggera!
                    snoozeAlarm();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!isSnoozed && seekBar.getProgress() < 90) {
                    // Kembalikan slider ke posisi awal jika belum capai hujung
                    seekBar.setProgress(0);
                }
            }
        });
    }

    private void snoozeAlarm() {
        isSnoozed = true;
        autoSnoozeHandler.removeCallbacks(autoSnoozeRunnable);
        Log.d("SOS_DEBUG", "SosAlarmActivity: Slide to snooze completed for alertId=" + alertId);

        // 1. Matikan audio dan getaran serta-merta
        try {
            SosAudioManager.stopAll();
        } catch (Exception ignored) {}

        try {
            VibrateManager.stopAll(this);
        } catch (Exception ignored) {}

        // 2. Padamkan notifikasi SOS jika ada
        if (!alertId.isEmpty()) {
            try {
                NotificationHelper.cancelSos(this, alertId);
            } catch (Exception ignored) {}
        }

        // 3. Kemas kini UI kepada mod Snoozed
        onAlarmSnoozedUi();
    }

    private void onAlarmSnoozedUi() {
        isSnoozed = true;
        autoSnoozeHandler.removeCallbacks(autoSnoozeRunnable);
        if (tvStatus != null) {
            tvStatus.setText(R.string.sos_alarm_snoozed_success);
            tvStatus.setTextColor(0xFF10B981); // Emerald Green
            tvStatus.setVisibility(View.VISIBLE);
        }

        if (cardSlideContainer != null) {
            cardSlideContainer.setVisibility(View.GONE);
        }

        if (layoutSnoozedActions != null) {
            layoutSnoozedActions.setVisibility(View.VISIBLE);
        }

        if (pulseAnimator != null) {
            pulseAnimator.cancel();
            pulseAnimator = null;
        }
        if (progressAnimator != null) {
            progressAnimator.cancel();
            progressAnimator = null;
        }
        if (pulseRing != null) {
            pulseRing.setScaleX(1.0f);
            pulseRing.setScaleY(1.0f);
            pulseRing.setAlpha(1.0f);
        }
    }

    private void startDialProgressAnimation() {
        if (circularProgressDial == null) return;
        if (progressAnimator != null) {
            progressAnimator.cancel();
        }
        autoSnoozeHandler.removeCallbacks(autoSnoozeRunnable);

        // Berputar dan memanjang dari 0 ke penuh (100% / 1.0f) mengikut durasi 10 saat (10,000ms)
        progressAnimator = ObjectAnimator.ofFloat(circularProgressDial, "progress", 0f, 1f);
        progressAnimator.setDuration(10000L);
        progressAnimator.setInterpolator(new android.view.animation.LinearInterpolator());
        progressAnimator.addListener(new AnimatorListenerAdapter() {
            private boolean cancelled = false;

            @Override
            public void onAnimationCancel(Animator animation) {
                cancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!cancelled && !isFinishing() && !isDestroyed() && !isSnoozed) {
                    autoSnoozeTimerFinished();
                }
            }
        });
        progressAnimator.start();

        // Fallback jikalau animation listener di-skip oleh sistem
        autoSnoozeHandler.postDelayed(autoSnoozeRunnable, 10200L);
    }

    /**
     * autoSnoozeTimerFinished
     * Apabila penggera tamat berbunyi (10 saat):
     * Mematikan siren & getaran, batalkan notifikasi, dan terus masuk ke mod Snoozed (skrin tindakan Dismiss / View on Map)
     * tanpa kembali ke laman utama (MainActivity).
     */
    private void autoSnoozeTimerFinished() {
        if (isSnoozed) return;
        isSnoozed = true;
        autoSnoozeHandler.removeCallbacks(autoSnoozeRunnable);
        Log.d("SOS_DEBUG", "SosAlarmActivity: 10s alarm duration elapsed. Auto-snoozing & showing action buttons.");

        // 1. Matikan audio dan getaran serta-merta
        try {
            SosAudioManager.stopAll();
        } catch (Exception ignored) {}

        try {
            VibrateManager.stopAll(this);
        } catch (Exception ignored) {}

        // 2. Padamkan notifikasi SOS jika ada
        if (!alertId.isEmpty()) {
            try {
                NotificationHelper.cancelSos(this, alertId);
            } catch (Exception ignored) {}
        }

        // 3. Masuk terus ke page snoozed (page kanan) dengan butang Dismiss dan View on Map - JANGAN masuk main page
        onAlarmSnoozedUi();
    }

    private void startPulseAnimation() {
        // Disabled: dial circle remains static without resizing / pulsing animation
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        autoSnoozeHandler.removeCallbacks(autoSnoozeRunnable);
        try {
            unregisterReceiver(sosStatusReceiver);
        } catch (Exception ignored) {}

        if (pulseAnimator != null) {
            pulseAnimator.cancel();
            pulseAnimator = null;
        }
        if (progressAnimator != null) {
            progressAnimator.cancel();
            progressAnimator = null;
        }
    }
}
