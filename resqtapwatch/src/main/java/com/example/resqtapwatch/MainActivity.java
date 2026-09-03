package com.example.resqtapwatch;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.example.resqtap.R;


/**
 * MainActivity
 * Main Dashboard: Butang OneTap SOS, grid menu pantas, status bateri, dan sambungan ke smartwatch.
 */
public class MainActivity extends Activity {
    private static final String SOS_PATH = "/resqtap/sos";
    private static final String SOS_DATA_PATH = "/resqtap/sos_data";
    private static final String PHONE_SOS_CAPABILITY = "resqtap_phone_sos";

    // =========================================================================
    // SEKSYEN: ONCREATE
    // =========================================================================
    /** Inisialisasi aktiviti, bind view UI, dan setup listener. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ResQTapWatchView view = new ResQTapWatchView(this);
        view.setContentDescription("ResQTap Watch SOS emergency button");
        view.setSosListener(this::sendSosToPhone);
        setContentView(view);
    }

    /** Simpan atau hantar data SosToPhone. */
    private void sendSosToPhone() {
        Wearable.getCapabilityClient(this)
                .getCapability(PHONE_SOS_CAPABILITY, CapabilityClient.FILTER_REACHABLE)
                .addOnSuccessListener(capabilityInfo -> {
                    java.util.Set<com.google.android.gms.wearable.Node> nodes = capabilityInfo.getNodes();
                    if (nodes == null || nodes.isEmpty()) {
                        sendSosToConnectedNodes();
                        return;
                    }

                    sendSosToNodes(nodes, "SOS sent");
                    sendSosDataItem();
                })
                .addOnFailureListener(error -> sendSosToConnectedNodes());
    }

    /** Simpan atau hantar data SosToConnectedNodes. */
    private void sendSosToConnectedNodes() {
        Wearable.getNodeClient(this).getConnectedNodes()
                .addOnSuccessListener(nodes -> {
                    if (nodes == null || nodes.isEmpty()) {
                        showWatchToast("Pair phone first");
                        return;
                    }
                    sendSosToNodes(nodes, "SOS sent");
                    sendSosDataItem();
                })
                .addOnFailureListener(error -> showWatchToast("Connection failed"));
    }

    /** Simpan atau hantar data SosToNodes. */
    private void sendSosToNodes(java.util.Collection<com.google.android.gms.wearable.Node> nodes, String successMessage) {
        for (com.google.android.gms.wearable.Node node : nodes) {
            Wearable.getMessageClient(this).sendMessage(
                    node.getId(),
                    SOS_PATH,
                    "SOS_TRIGGERED".getBytes()
            )
                    .addOnSuccessListener(id -> showWatchToast(successMessage))
                    .addOnFailureListener(error -> showWatchToast("Send failed"));
        }
    }

    /** Simpan atau hantar data SosDataItem. */
    private void sendSosDataItem() {
        PutDataMapRequest dataMap = PutDataMapRequest.create(SOS_DATA_PATH);
        dataMap.getDataMap().putLong("triggeredAt", System.currentTimeMillis());
        dataMap.getDataMap().putString("source", "watch");
        PutDataRequest request = dataMap.asPutDataRequest();
        request.setUrgent();
        Wearable.getDataClient(this).putDataItem(request)
                .addOnFailureListener(error -> showWatchToast("Sync failed"));
    }

    /** Paparkan WatchToast. */
    private void showWatchToast(String message) {
        TextView toastText = new TextView(this);
        toastText.setText(message);
        toastText.setTextColor(Color.WHITE);
        toastText.setTextSize(12f);
        toastText.setGravity(Gravity.CENTER);
        toastText.setSingleLine(true);
        toastText.setPadding(dp(14), dp(7), dp(14), dp(7));

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.argb(225, 33, 29, 48));
        background.setCornerRadius(dp(18));
        background.setStroke(dp(1), Color.argb(70, 248, 244, 255));
        toastText.setBackground(background);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(toastText);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /** Fungsi untuk dp. */
    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static final class ResQTapWatchView extends android.view.View {
        private static final int SURFACE_TOP = Color.rgb(27, 15, 43);
        private static final int SURFACE_MID = Color.rgb(18, 17, 22);
        private static final int SURFACE_BOTTOM = Color.rgb(18, 17, 22);
        private static final int CARD = Color.rgb(56, 50, 75);
        private static final int CARD_DARK = Color.rgb(44, 38, 61);
        private static final int TEXT_PRIMARY = Color.WHITE;
        private static final int TEXT_MUTED = Color.rgb(207, 198, 221);
        private static final int BRAND_DARK = Color.rgb(33, 29, 48);
        private static final int BRAND_PRIMARY = Color.rgb(56, 50, 75);
        private static final int BRAND_SOFT = Color.rgb(248, 244, 255);

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF sosRect = new RectF();
        private final RectF textScrimRect = new RectF();

        private boolean pressed;
        private float pulse;
        private ValueAnimator pulseAnimator;
        private SosListener sosListener;

        ResQTapWatchView(android.content.Context context) {
            super(context);
            setClickable(true);
            setFocusable(true);
            startPulse();
        }

        void setSosListener(SosListener sosListener) {
            this.sosListener = sosListener;
        }

        /** Fungsi untuk onDraw. */
    @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float width = getWidth();
            float height = getHeight();
            float min = Math.min(width, height);
            float cx = width / 2f;
            float cy = height / 2f;

            drawSurface(canvas, width, height, cx, cy, min);
            drawSosArea(canvas, cx, height, min);
            drawTextScrim(canvas, cx, height, min);
            drawTitle(canvas, cx, height, min);
            drawHint(canvas, cx, height, min);
        }

        /** Fungsi untuk onTouchEvent. */
    @Override
        public boolean onTouchEvent(MotionEvent event) {
            boolean hit = sosRect.contains(event.getX(), event.getY());
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (hit) {
                        pressed = true;
                        invalidate();
                        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    }
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (pressed != hit) {
                        pressed = hit;
                        invalidate();
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    boolean shouldClick = pressed && hit;
                    pressed = false;
                    invalidate();
                    if (shouldClick) performClick();
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    pressed = false;
                    invalidate();
                    return true;
                default:
                    return super.onTouchEvent(event);
            }
        }

        /** Fungsi untuk performClick. */
    @Override
        public boolean performClick() {
            super.performClick();
            performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            if (sosListener != null) sosListener.onSos();
            return true;
        }

        /** Fungsi untuk drawSurface. */
        private void drawSurface(Canvas canvas, float width, float height, float cx, float cy, float min) {
            canvas.drawColor(SURFACE_MID);

            paint.setShader(new LinearGradient(
                    0f, 0f, width, height,
                    new int[]{SURFACE_TOP, SURFACE_MID, SURFACE_BOTTOM},
                    new float[]{0f, 0.46f, 1f},
                    Shader.TileMode.CLAMP
            ));
            canvas.drawRect(0f, 0f, width, height, paint);
            paint.setShader(null);

            paint.setShader(new RadialGradient(
                    cx - min * 0.28f, cy - min * 0.33f, min * 0.72f,
                    new int[]{
                            Color.argb(82, 56, 50, 75),
                            Color.argb(34, 56, 50, 75),
                            Color.TRANSPARENT
                    },
                    new float[]{0f, 0.50f, 1f},
                    Shader.TileMode.CLAMP
            ));
            canvas.drawCircle(cx - min * 0.28f, cy - min * 0.33f, min * 0.72f, paint);
            paint.setShader(null);

            paint.setShader(new RadialGradient(
                    cx, cy + min * 0.04f, min * 0.58f,
                    new int[]{
                            Color.argb(72, 56, 50, 75),
                            Color.argb(28, 33, 29, 48),
                            Color.TRANSPARENT
                    },
                    new float[]{0f, 0.58f, 1f},
                    Shader.TileMode.CLAMP
            ));
            canvas.drawCircle(cx, cy + min * 0.04f, min * 0.58f, paint);
            paint.setShader(null);
        }

        /** Fungsi untuk drawSosArea. */
        private void drawSosArea(Canvas canvas, float cx, float height, float min) {
            float centerY = height * 0.465f + (pressed ? min * 0.006f : 0f);
            float outerR = min * (0.32f + pulse * 0.035f);
            float midR = min * (0.265f + pulse * 0.022f);
            float buttonR = min * (pressed ? 0.205f : 0.218f);
            sosRect.set(cx - buttonR, centerY - buttonR, cx + buttonR, centerY + buttonR);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(Math.round(138 - pulse * 34), 35, 18, 72));
            canvas.drawCircle(cx, centerY, outerR, paint);

            paint.setColor(Color.argb(Math.round(170 - pulse * 40), 58, 27, 116));
            canvas.drawCircle(cx, centerY, midR, paint);

            paint.setColor(Color.WHITE);
            canvas.drawCircle(cx, centerY, buttonR, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(minStroke(min, 1.45f));
            paint.setColor(Color.argb(210, 248, 244, 255));
            canvas.drawCircle(cx, centerY, buttonR - min * 0.006f, paint);

            paint.setStyle(Paint.Style.FILL);

            textPaint.setColor(BRAND_DARK);
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(min * 0.095f);
            textPaint.setLetterSpacing(0.05f);
            drawCenteredText(canvas, "SOS", cx, centerY, textPaint);
        }

        /** Fungsi untuk drawTitle. */
        private void drawTitle(Canvas canvas, float cx, float height, float min) {
            textPaint.setColor(Color.rgb(248, 244, 255));
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(min * 0.036f);
            textPaint.setLetterSpacing(0.08f);
            drawCenteredText(canvas, "WELCOME TO RESQTAP", cx, height * 0.775f, textPaint);
        }

        /** Fungsi untuk drawTextScrim. */
        private void drawTextScrim(Canvas canvas, float cx, float height, float min) {
            float scrimW = min * 0.58f;
            float scrimH = min * 0.13f;
            float scrimTop = height * 0.735f;
            textScrimRect.set(cx - scrimW / 2f, scrimTop, cx + scrimW / 2f, scrimTop + scrimH);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(185, 18, 17, 22));
            canvas.drawRoundRect(textScrimRect, scrimH / 2f, scrimH / 2f, paint);
        }

        /** Fungsi untuk drawHint. */
        private void drawHint(Canvas canvas, float cx, float height, float min) {
            textPaint.setColor(Color.rgb(248, 244, 255));
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(min * 0.032f);
            textPaint.setLetterSpacing(0f);
            drawCenteredText(canvas, "Press SOS for urgent help", cx, height * 0.825f, textPaint);
        }

        /** Fungsi untuk startPulse. */
        private void startPulse() {
            pulseAnimator = ValueAnimator.ofFloat(0f, 1f);
            pulseAnimator.setDuration(1500);
            pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
            pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
            pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            pulseAnimator.addUpdateListener(animator -> {
                pulse = (float) animator.getAnimatedValue();
                invalidate();
            });
            pulseAnimator.start();
        }

        /** Fungsi untuk onDetachedFromWindow. */
    @Override
        protected void onDetachedFromWindow() {
            if (pulseAnimator != null) {
                pulseAnimator.cancel();
                pulseAnimator = null;
            }
            super.onDetachedFromWindow();
        }

        /** Fungsi untuk drawCenteredText. */
        private void drawCenteredText(Canvas canvas, String text, float x, float centerY, Paint p) {
            Paint.FontMetrics fm = p.getFontMetrics();
            float baseline = centerY - (fm.ascent + fm.descent) / 2f;
            canvas.drawText(text, x, baseline, p);
        }

        /** Fungsi untuk minStroke. */
        private float minStroke(float base, float multiplier) {
            return Math.max(1f, base * 0.005f * multiplier);
        }

        interface SosListener {
            void onSos();
        }
    }
}
