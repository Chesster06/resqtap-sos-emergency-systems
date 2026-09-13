package com.example.resqtap.security;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.HapticFeedbackConstants;
import android.view.TextureView;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.resqtap.R;
import com.example.resqtap.utils.LocaleUtils;
import com.example.resqtap.utils.ThemeUtils;
import com.google.android.material.button.MaterialButton;
import com.google.mlkit.vision.face.Face;

/**
 * FaceIdVerificationActivity
 * 3-phase full-screen Face ID verification flow inspired by modern KYC identity standards:
 * Phase 1: Clean onboarding & guidelines ("I'm ready")
 * Phase 2: Immersive dark camera preview with vertical oval glowing progress ring
 * Phase 3: Centered green checkmark completion badge and auto-dismiss
 */
public class FaceIdVerificationActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "extra_mode";
    public static final String MODE_REGISTER = "mode_register";
    public static final String MODE_UNLOCK = "mode_unlock";

    public static final int RESULT_FALLBACK_PIN = 1001;

    private View layoutIntro;
    private View layoutScanContainer;

    private TextureView scanCameraTexture;
    private OvalProgressView viewOvalProgress;
    private View viewDimOverlay;
    private FrameLayout layoutSuccessBadge;
    private TextView tvScanStatus;
    private TextView tvScanSubstatus;
    private View layoutScanActions;
    private MaterialButton btnScanRetry;

    private FaceScannerManager faceScanner;
    private String currentMode = MODE_REGISTER;
    private boolean isVerifyingComplete = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        Context wrapped = LocaleUtils.wrap(newBase);
        super.attachBaseContext(wrapped == null ? newBase : wrapped);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applySavedNightMode(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_face_id_verification);

        View root = findViewById(R.id.main_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (getIntent() != null && getIntent().hasExtra(EXTRA_MODE)) {
            currentMode = getIntent().getStringExtra(EXTRA_MODE);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleCancelOrBack();
            }
        });

        initViews();

        if (MODE_UNLOCK.equals(currentMode)) {
            // For app unlock, go straight into the dark scanning oval for quick authentication
            showScanningPhase(false);
        } else {
            // For registration / settings, start at Phase 1 (Instructions)
            showIntroPhase();
        }
    }

    private void initViews() {
        layoutIntro = findViewById(R.id.layout_intro);
        layoutScanContainer = findViewById(R.id.layout_scan_container);

        View btnIntroBack = findViewById(R.id.btn_intro_back);
        MaterialButton btnIntroReady = findViewById(R.id.btn_intro_ready);

        if (btnIntroBack != null) {
            btnIntroBack.setOnClickListener(v -> handleCancelOrBack());
        }
        if (btnIntroReady != null) {
            btnIntroReady.setOnClickListener(v -> showScanningPhase(true));
        }

        View btnScanBack = findViewById(R.id.btn_scan_back);
        scanCameraTexture = findViewById(R.id.scan_camera_texture);
        viewOvalProgress = findViewById(R.id.view_oval_progress);
        viewDimOverlay = findViewById(R.id.view_dim_overlay);
        layoutSuccessBadge = findViewById(R.id.layout_success_badge);
        tvScanStatus = findViewById(R.id.tv_scan_status);
        tvScanSubstatus = findViewById(R.id.tv_scan_substatus);
        layoutScanActions = findViewById(R.id.layout_scan_actions);
        btnScanRetry = findViewById(R.id.btn_scan_retry);

        if (btnScanBack != null) {
            btnScanBack.setOnClickListener(v -> handleCancelOrBack());
        }

        if (btnScanRetry != null) {
            btnScanRetry.setOnClickListener(v -> restartScanning());
        }

        // Developer quick helper: clicking the oval frame simulates head turns and verifies
        View frameOvalWrapper = findViewById(R.id.frame_oval_wrapper);
        if (frameOvalWrapper != null) {
            frameOvalWrapper.setOnClickListener(v -> {
                if (!isVerifyingComplete) {
                    simulateHeadTurnProgressAndVerify();
                }
            });
        }
    }

    private void simulateHeadTurnProgressAndVerify() {
        if (isVerifyingComplete) return;
        if (tvScanStatus != null) {
            tvScanStatus.setText(R.string.face_verify_instruction_turn_head);
        }
        // Animate left arc first
        ValueAnimator leftAnim = ValueAnimator.ofFloat(0f, 1f);
        leftAnim.setDuration(400);
        leftAnim.addUpdateListener(anim -> {
            if (viewOvalProgress != null) {
                viewOvalProgress.setProgress((float) anim.getAnimatedValue(), 0f);
            }
        });

        // Then animate right arc
        ValueAnimator rightAnim = ValueAnimator.ofFloat(0f, 1f);
        rightAnim.setDuration(400);
        rightAnim.setStartDelay(150);
        rightAnim.addUpdateListener(anim -> {
            if (viewOvalProgress != null) {
                viewOvalProgress.setProgress(1.0f, (float) anim.getAnimatedValue());
            }
        });
        rightAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                handleFaceVerifiedSuccess();
            }
        });

        leftAnim.start();
        rightAnim.start();
    }

    private void showIntroPhase() {
        stopScanning();
        View root = findViewById(R.id.main_root);
        if (root != null) root.setBackgroundColor(android.graphics.Color.parseColor("#FAFAFC"));
        if (layoutIntro != null) layoutIntro.setVisibility(View.VISIBLE);
        if (layoutScanContainer != null) layoutScanContainer.setVisibility(View.GONE);

        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(!ThemeUtils.isNightMode(this));
    }

    private void showScanningPhase(boolean animated) {
        View root = findViewById(R.id.main_root);
        if (root != null) root.setBackgroundColor(android.graphics.Color.parseColor("#16181D"));
        if (layoutIntro != null) layoutIntro.setVisibility(View.GONE);
        if (layoutScanContainer != null) {
            layoutScanContainer.setVisibility(View.VISIBLE);
            if (animated) {
                layoutScanContainer.setAlpha(0f);
                layoutScanContainer.animate().alpha(1f).setDuration(250).start();
            }
        }

        // In the dark scanning screen, ensure status bar icons are crisp white
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(false);

        resetScanState();
        startFaceScanner();
    }

    private void resetScanState() {
        isVerifyingComplete = false;
        if (viewOvalProgress != null) {
            viewOvalProgress.reset();
        }
        if (viewDimOverlay != null) {
            viewDimOverlay.setVisibility(View.GONE);
        }
        if (layoutSuccessBadge != null) {
            layoutSuccessBadge.setVisibility(View.GONE);
        }
        if (tvScanStatus != null) {
            tvScanStatus.setText(R.string.face_verify_instruction_turn_head);
        }
        if (tvScanSubstatus != null) {
            tvScanSubstatus.setVisibility(View.GONE);
        }
        if (layoutScanActions != null) {
            layoutScanActions.setVisibility(View.GONE);
        }
    }

    private void startFaceScanner() {
        if (scanCameraTexture == null) return;
        stopScanning();

        faceScanner = new FaceScannerManager(this, scanCameraTexture, new FaceScannerManager.FaceScanListener() {
            @Override
            public void onCameraReady() {
                runOnUiThread(() -> {
                    if (viewOvalProgress != null) {
                        viewOvalProgress.reset();
                    }
                });
            }

            @Override
            public void onFaceScanning(boolean faceDetected, String statusMessage) {
                runOnUiThread(() -> {
                    if (isVerifyingComplete) return;

                    if (tvScanStatus != null && statusMessage != null) {
                        tvScanStatus.setText(statusMessage);
                    }
                });
            }

            @Override
            public void onHeadTurnProgress(float leftProgress, float rightProgress, String prompt) {
                runOnUiThread(() -> {
                    if (isVerifyingComplete) return;

                    if (viewOvalProgress != null) {
                        viewOvalProgress.setProgress(leftProgress, rightProgress);
                    }
                    if (tvScanStatus != null && prompt != null) {
                        tvScanStatus.setText(prompt);
                    }
                });
            }

            @Override
            public void onFaceVerified(Face face) {
                runOnUiThread(() -> handleFaceVerifiedSuccess());
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    if (isVerifyingComplete) return;
                    if (viewOvalProgress != null) {
                        viewOvalProgress.setDanger(true);
                    }
                    if (tvScanStatus != null) {
                        tvScanStatus.setText(errorMessage != null ? errorMessage : getString(R.string.face_id_camera_error));
                    }
                    if (layoutScanActions != null) {
                        layoutScanActions.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onTimeout() {
                runOnUiThread(() -> {
                    if (isVerifyingComplete) return;
                    if (viewOvalProgress != null) {
                        viewOvalProgress.setDanger(true);
                    }
                    if (tvScanStatus != null) {
                        tvScanStatus.setText(R.string.face_id_timeout);
                    }
                    if (layoutScanActions != null) {
                        layoutScanActions.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        faceScanner.startScanning();
    }

    private void handleFaceVerifiedSuccess() {
        if (isVerifyingComplete) return;
        isVerifyingComplete = true;

        stopScanning();

        // 1. Light up border full green
        if (viewOvalProgress != null) {
            viewOvalProgress.setComplete(true);
        }

        // 2. Dim preview slightly
        if (viewDimOverlay != null) {
            viewDimOverlay.setVisibility(View.VISIBLE);
            viewDimOverlay.setAlpha(0f);
            viewDimOverlay.animate().alpha(0.55f).setDuration(200).start();
        }

        // 3. Pop in green checkmark badge
        if (layoutSuccessBadge != null) {
            layoutSuccessBadge.setVisibility(View.VISIBLE);
            layoutSuccessBadge.setScaleX(0.2f);
            layoutSuccessBadge.setScaleY(0.2f);
            layoutSuccessBadge.setAlpha(0f);
            layoutSuccessBadge.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .alpha(1.0f)
                    .setDuration(350)
                    .setInterpolator(new OvershootInterpolator(1.8f))
                    .start();
        }

        // 4. Update title
        if (tvScanStatus != null) {
            tvScanStatus.setText(R.string.face_verify_complete_title);
        }
        if (tvScanSubstatus != null) {
            tvScanSubstatus.setVisibility(View.GONE);
        }

        try {
            getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        } catch (Exception ignored) {
        }

        // 5. Complete and return
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            setResult(RESULT_OK);
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }, 1100);
    }

    private void restartScanning() {
        resetScanState();
        startFaceScanner();
    }

    private void stopScanning() {
        if (faceScanner != null) {
            faceScanner.stopScanning();
            faceScanner = null;
        }
    }

    private void handleCancelOrBack() {
        stopScanning();
        if (MODE_REGISTER.equals(currentMode) && layoutScanContainer != null && layoutScanContainer.getVisibility() == View.VISIBLE) {
            // If user is in scanning phase during registration, go back to intro phase
            showIntroPhase();
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScanning();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScanning();
    }
}
