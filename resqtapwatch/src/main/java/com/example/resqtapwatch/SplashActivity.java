package com.example.resqtapwatch;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.example.resqtap.R;


/**
 * SplashActivity
 * Splash screen: check status login Firebase Auth sebelum masuk ke Main atau Login.
 */
public class SplashActivity extends Activity {
    private static final long SPLASH_MS = 5000L;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable openMain = () -> {
        startActivity(new Intent(this, MainActivity.class));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    };

    // =========================================================================
    // SEKSYEN: ONCREATE
    // =========================================================================
    /** Inisialisasi aktiviti, bind view UI, dan setup listener. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SplashView(this));
        handler.postDelayed(openMain, SPLASH_MS);
    }

    // =========================================================================
    // SEKSYEN: ONDESTROY
    // =========================================================================
    /** Pembersihan memori, buang listener, dan tutup sambungan. */
    @Override
    protected void onDestroy() {
        handler.removeCallbacks(openMain);
        super.onDestroy();
    }

    private static final class SplashView extends android.view.View {
        private static final int PURPLE_TOP = Color.rgb(94, 103, 255);
        private static final int PURPLE_MID = Color.rgb(124, 84, 246);
        private static final int PURPLE_BOTTOM = Color.rgb(157, 80, 239);
        private static final int WHITE = Color.rgb(255, 255, 255);

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Bitmap logoBitmap;

        SplashView(android.content.Context context) {
            super(context);
            logoBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.resqtap_launcher);
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

            drawBackground(canvas, width, height, min);
            drawLogo(canvas, cx, cy - min * 0.1f, min);
            drawBrandText(canvas, cx, cy + min * 0.19f, min);
        }

        /** Fungsi untuk drawBackground. */
        private void drawBackground(Canvas canvas, float width, float height, float min) {
            paint.setShader(new LinearGradient(
                    0f, 0f, width, height,
                    new int[]{PURPLE_TOP, PURPLE_MID, PURPLE_BOTTOM},
                    new float[]{0f, 0.48f, 1f},
                    Shader.TileMode.CLAMP
            ));
            canvas.drawRect(0f, 0f, width, height, paint);
            paint.setShader(null);

            paint.setShader(new RadialGradient(
                    width * 0.5f, height * 0.42f, min * 0.62f,
                    new int[]{Color.argb(72, 255, 255, 255), Color.argb(0, 255, 255, 255)},
                    new float[]{0f, 1f},
                    Shader.TileMode.CLAMP
            ));
            canvas.drawCircle(width * 0.5f, height * 0.42f, min * 0.62f, paint);
            paint.setShader(null);
        }

        /** Fungsi untuk drawLogo. */
        private void drawLogo(Canvas canvas, float cx, float cy, float min) {
            float badgeR = min * 0.175f;

            paint.setStyle(Paint.Style.FILL);
            paint.setFilterBitmap(true);
            ColorMatrix logoMatrix = new ColorMatrix(new float[]{
                    1.24f, 0f, 0f, 0f, 14f,
                    0f, 1.24f, 0f, 0f, 14f,
                    0f, 0f, 1.28f, 0f, 18f,
                    0f, 0f, 0f, 1f, 0f
            });
            paint.setColorFilter(new ColorMatrixColorFilter(logoMatrix));
            RectF logoBounds = new RectF(
                    cx - badgeR,
                    cy - badgeR,
                    cx + badgeR,
                    cy + badgeR
            );
            canvas.drawBitmap(logoBitmap, null, logoBounds, paint);
            paint.setColorFilter(null);
        }

        /** Fungsi untuk drawBrandText. */
        private void drawBrandText(Canvas canvas, float cx, float centerY, float min) {
            textPaint.setColor(WHITE);
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(min * 0.078f);
            textPaint.setLetterSpacing(0f);
            drawCenteredText(canvas, "ResQTap", cx, centerY, textPaint);

            textPaint.setColor(Color.argb(230, 255, 255, 255));
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            textPaint.setTextSize(min * 0.037f);
            textPaint.setLetterSpacing(0.03f);
            drawCenteredText(canvas, "Fast help, safer you", cx, centerY + min * 0.1f, textPaint);
        }

        /** Fungsi untuk drawCenteredText. */
        private void drawCenteredText(Canvas canvas, String text, float x, float centerY, Paint p) {
            Paint.FontMetrics fm = p.getFontMetrics();
            float baseline = centerY - (fm.ascent + fm.descent) / 2f;
            canvas.drawText(text, x, baseline, p);
        }
    }
}
