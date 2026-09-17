package com.example.resqtap.sos;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.resqtap.R;
import com.example.resqtap.app.BaseActivity;
import com.example.resqtap.room.FirebaseRoomClient;
import com.example.resqtap.utils.ThemeUtils;
import com.example.resqtap.utils.UserPrefs;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * SosProgressActivity
 * Paparan Kemajuan SOS Masa Nyata:
 * Menunjukkan status tindakan admin setelah kes SOS di-reserve (Dispatched, En Route, On Scene, Resolved).
 */
public class SosProgressActivity extends BaseActivity {

    public static final String EXTRA_ROOM_CODE = "roomCode";
    public static final String EXTRA_ALERT_ID = "alertId";

    private String roomCode = "";
    private String alertId = "";

    private TextView tvRoomCode;
    private TextView tvStatusMain;
    private TextView tvUpdatedTime;
    private TextView tvResponderName;
    private TextView tvProgressNote;

    private FrameLayout step1Circle;
    private FrameLayout step2Circle;
    private FrameLayout step3Circle;
    private FrameLayout step4Circle;

    private ImageView step1Icon;
    private ImageView step2Icon;
    private ImageView step3Icon;
    private ImageView step4Icon;

    private View stepLine12;
    private View stepLine23;
    private View stepLine34;

    private TextView tvStep1Title;
    private TextView tvStep2Title;
    private TextView tvStep3Title;
    private TextView tvStep4Title;

    private TextView tvStep1Desc;
    private TextView tvStep2Desc;
    private TextView tvStep3Desc;
    private TextView tvStep4Desc;

    private MaterialButton btnCancelSos;

    private DatabaseReference alertRef;
    private ValueEventListener alertListener;

    public static void launch(Context context, String roomCode, String alertId) {
        if (context == null) return;
        Intent intent = new Intent(context, SosProgressActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(EXTRA_ROOM_CODE, roomCode);
        intent.putExtra(EXTRA_ALERT_ID, alertId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeUtils.applySavedNightMode(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos_progress);

        roomCode = getIntent().getStringExtra(EXTRA_ROOM_CODE);
        alertId = getIntent().getStringExtra(EXTRA_ALERT_ID);

        if (roomCode == null) roomCode = "";
        if (alertId == null) alertId = "";

        bindViews();
        setupListeners();
        attachAlertListener();
    }

    private void bindViews() {
        View back = findViewById(R.id.btn_back_sos_progress);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }

        tvRoomCode = findViewById(R.id.tv_sos_room_code);
        tvStatusMain = findViewById(R.id.tv_sos_status_main);
        tvUpdatedTime = findViewById(R.id.tv_sos_updated_time);
        tvResponderName = findViewById(R.id.tv_sos_responder_name);
        tvProgressNote = findViewById(R.id.tv_sos_progress_note);

        step1Circle = findViewById(R.id.sos_step_1_circle);
        step2Circle = findViewById(R.id.sos_step_2_circle);
        step3Circle = findViewById(R.id.sos_step_3_circle);
        step4Circle = findViewById(R.id.sos_step_4_circle);

        step1Icon = findViewById(R.id.sos_step_1_icon);
        step2Icon = findViewById(R.id.sos_step_2_icon);
        step3Icon = findViewById(R.id.sos_step_3_icon);
        step4Icon = findViewById(R.id.sos_step_4_icon);

        stepLine12 = findViewById(R.id.sos_step_line_1_2);
        stepLine23 = findViewById(R.id.sos_step_line_2_3);
        stepLine34 = findViewById(R.id.sos_step_line_3_4);

        tvStep1Title = findViewById(R.id.tv_sos_step_1_title);
        tvStep2Title = findViewById(R.id.tv_sos_step_2_title);
        tvStep3Title = findViewById(R.id.tv_sos_step_3_title);
        tvStep4Title = findViewById(R.id.tv_sos_step_4_title);

        tvStep1Desc = findViewById(R.id.tv_sos_step_1_desc);
        tvStep2Desc = findViewById(R.id.tv_sos_step_2_desc);
        tvStep3Desc = findViewById(R.id.tv_sos_step_3_desc);
        tvStep4Desc = findViewById(R.id.tv_sos_step_4_desc);

        btnCancelSos = findViewById(R.id.btn_cancel_sos_progress);

        if (!roomCode.isEmpty()) {
            tvRoomCode.setText("ROOM: " + roomCode.toUpperCase(Locale.ROOT));
        }
    }

    private void setupListeners() {
        if (btnCancelSos != null) {
            btnCancelSos.setOnClickListener(v -> cancelSosByVictim());
        }
    }

    private void cancelSosByVictim() {
        if (roomCode.isEmpty() || alertId.isEmpty()) {
            finish();
            return;
        }

        String dev = UserPrefs.getOrCreateDeviceId(this);
        FirebaseRoomClient.cancelRoomSosQueued(roomCode, alertId, dev);
        Toast.makeText(this, R.string.sos_progress_cancelled, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void attachAlertListener() {
        if (roomCode.isEmpty() || alertId.isEmpty()) return;

        alertRef = FirebaseDatabase.getInstance(FirebaseRoomClient.DATABASE_URL)
                .getReference("rooms")
                .child(roomCode.toUpperCase(Locale.ROOT))
                .child("sosAlerts")
                .child(alertId);

        alertListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Alert was removed
                    finish();
                    return;
                }

                String status = String.valueOf(snapshot.child("status").getValue() == null ? "active" : snapshot.child("status").getValue());
                boolean isCancelled = "cancelled".equalsIgnoreCase(status) || snapshot.child("cancelledAt").exists();
                boolean isResolved = "resolved".equalsIgnoreCase(status) || snapshot.child("resolvedAt").exists();

                if (isCancelled) {
                    Toast.makeText(SosProgressActivity.this, R.string.sos_progress_cancelled, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                String responderName = String.valueOf(snapshot.child("servedByName").getValue() == null ? "Admin Responder" : snapshot.child("servedByName").getValue());
                String progressStatus = String.valueOf(snapshot.child("progressStatus").getValue() == null ? "Admin Dispatched" : snapshot.child("progressStatus").getValue());
                String notes = String.valueOf(snapshot.child("progressNotes").getValue() == null ? "Emergency assistance is assigned and responders have been notified." : snapshot.child("progressNotes").getValue());

                Long stepVal = snapshot.child("progressStep").getValue(Long.class);
                int step = stepVal != null ? stepVal.intValue() : 1;
                if (isResolved) step = 4;

                Long updatedTimeMs = snapshot.child("updatedAt").getValue(Long.class);
                if (updatedTimeMs == null) updatedTimeMs = snapshot.child("servedAt").getValue(Long.class);
                if (updatedTimeMs == null) updatedTimeMs = snapshot.child("createdAt").getValue(Long.class);

                updateUi(step, progressStatus, responderName, notes, updatedTimeMs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        alertRef.addValueEventListener(alertListener);
    }

    private void updateUi(int step, String statusText, String responder, String notes, Long timeMs) {
        if (tvStatusMain != null) tvStatusMain.setText(statusText);
        if (tvResponderName != null) tvResponderName.setText(responder);
        if (tvProgressNote != null) tvProgressNote.setText(notes);

        if (tvUpdatedTime != null && timeMs != null && timeMs > 0L) {
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            tvUpdatedTime.setText("Updated " + sdf.format(new Date(timeMs)));
        }

        renderStepper(step);
    }

    private void renderStepper(int step) {
        // Colors
        int green = ContextCompat.getColor(this, R.color.brand_primary); // Brand theme color
        int activePink = 0xFFE60067;
        int doneGreen = 0xFF10B981;
        int pendingGray = 0xFFCBD5E1;
        int textDark = ContextCompat.getColor(this, R.color.text_primary);
        int textMuted = ContextCompat.getColor(this, R.color.text_secondary);

        // Step 1
        if (step >= 1) {
            step1Circle.setBackgroundResource(step > 1 ? R.drawable.bg_step_circle_done : R.drawable.bg_step_circle_active);
            step1Icon.setColorFilter(0xFFFFFFFF);
            tvStep1Title.setTextColor(textDark);
            tvStep1Desc.setTextColor(textMuted);
        }

        // Line 1-2
        stepLine12.setBackgroundColor(step > 1 ? doneGreen : pendingGray);

        // Step 2
        if (step >= 2) {
            step2Circle.setBackgroundResource(step > 2 ? R.drawable.bg_step_circle_done : R.drawable.bg_step_circle_active);
            step2Icon.setColorFilter(0xFFFFFFFF);
            tvStep2Title.setTextColor(textDark);
            tvStep2Desc.setTextColor(textMuted);
        } else {
            step2Circle.setBackgroundResource(R.drawable.bg_step_circle_pending);
            step2Icon.setColorFilter(0xFF64748B);
            tvStep2Title.setTextColor(textMuted);
            tvStep2Desc.setTextColor(textMuted);
        }

        // Line 2-3
        stepLine23.setBackgroundColor(step > 2 ? doneGreen : pendingGray);

        // Step 3
        if (step >= 3) {
            step3Circle.setBackgroundResource(step > 3 ? R.drawable.bg_step_circle_done : R.drawable.bg_step_circle_active);
            step3Icon.setColorFilter(0xFFFFFFFF);
            tvStep3Title.setTextColor(textDark);
            tvStep3Desc.setTextColor(textMuted);
        } else {
            step3Circle.setBackgroundResource(R.drawable.bg_step_circle_pending);
            step3Icon.setColorFilter(0xFF64748B);
            tvStep3Title.setTextColor(textMuted);
            tvStep3Desc.setTextColor(textMuted);
        }

        // Line 3-4
        stepLine34.setBackgroundColor(step > 3 ? doneGreen : pendingGray);

        // Step 4
        if (step >= 4) {
            step4Circle.setBackgroundResource(R.drawable.bg_step_circle_done);
            step4Icon.setColorFilter(0xFFFFFFFF);
            tvStep4Title.setTextColor(textDark);
            tvStep4Desc.setTextColor(textMuted);
            if (btnCancelSos != null) btnCancelSos.setVisibility(View.GONE);
        } else {
            step4Circle.setBackgroundResource(R.drawable.bg_step_circle_pending);
            step4Icon.setColorFilter(0xFF64748B);
            tvStep4Title.setTextColor(textMuted);
            tvStep4Desc.setTextColor(textMuted);
            if (btnCancelSos != null) btnCancelSos.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (alertRef != null && alertListener != null) {
            alertRef.removeEventListener(alertListener);
            alertListener = null;
        }
    }
}
