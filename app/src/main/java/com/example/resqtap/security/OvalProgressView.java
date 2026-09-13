package com.example.resqtap.security;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * OvalProgressView
 * Draws a stadium/pill shaped guide frame with progressive green arc completion matching
 * the reference Entrust KYC design. The track hugs the camera preview seamlessly and
 * sweeps vivid green arcs as the user turns their head left and right.
 */
public class OvalProgressView extends View {

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dangerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF bounds = new RectF();
    private float cornerRadius = 0f;
    private float strokeWidthPx = 14f;

    // Full capsule path and left/right half paths
    private final Path fullCapsulePath = new Path();
    private final Path leftHalfPath = new Path();
    private final Path rightHalfPath = new Path();

    private final Path leftSegmentPath = new Path();
    private final Path rightSegmentPath = new Path();

    private PathMeasure leftPathMeasure;
    private PathMeasure rightPathMeasure;
    private float leftTotalLen = 0f;
    private float rightTotalLen = 0f;

    // Progress from 0.0f to 1.0f
    private float leftProgress = 0f;
    private float rightProgress = 0f;
    private boolean isDanger = false;
    private boolean isComplete = false;

    public OvalProgressView(Context context) {
        super(context);
        init();
    }

    public OvalProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OvalProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        strokeWidthPx = getResources().getDisplayMetrics().density * 5.0f;

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(strokeWidthPx);
        trackPaint.setColor(Color.parseColor("#374151")); // Sleek dark track for dark theme
        trackPaint.setStrokeCap(Paint.Cap.ROUND);

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidthPx);
        progressPaint.setColor(Color.parseColor("#00E676")); // Vivid green as reference
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        dangerPaint.setStyle(Paint.Style.STROKE);
        dangerPaint.setStrokeWidth(strokeWidthPx);
        dangerPaint.setColor(Color.parseColor("#FF5252"));
        dangerPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float inset = strokeWidthPx / 2f + 1f;
        bounds.set(inset, inset, w - inset, h - inset);
        cornerRadius = bounds.width() / 2f; // Perfectly matches MaterialCardView cornerRadius="135dp"

        buildPaths();
    }

    private void buildPaths() {
        if (bounds.isEmpty()) return;

        fullCapsulePath.reset();
        fullCapsulePath.addRoundRect(bounds, cornerRadius, cornerRadius, Path.Direction.CW);

        float cx = bounds.centerX();
        float top = bounds.top;
        float bottom = bounds.bottom;
        float r = cornerRadius;

        // Top arc rect: width = 2*r = bounds.width(), height = 2*r.
        // Left half sweeps top arc from 270 deg (top center) counter-clockwise to 180 deg (-90 deg sweep)
        // Then straight line down left side to bottom - r
        // Then bottom arc sweeps from 180 deg to 90 deg (bottom center) (-90 deg sweep)
        leftHalfPath.reset();
        leftHalfPath.moveTo(cx, top);
        leftHalfPath.arcTo(new RectF(bounds.left, bounds.top, bounds.right, bounds.top + 2 * r), 270f, -90f, false);
        leftHalfPath.lineTo(bounds.left, bounds.bottom - r);
        leftHalfPath.arcTo(new RectF(bounds.left, bounds.bottom - 2 * r, bounds.right, bounds.bottom), 180f, -90f, false);
        leftHalfPath.lineTo(cx, bottom);

        leftPathMeasure = new PathMeasure(leftHalfPath, false);
        leftTotalLen = leftPathMeasure.getLength();

        // Right half sweeps top arc from 270 deg (top center) clockwise to 0 deg (+90 deg sweep)
        // Then straight line down right side to bottom - r
        // Then bottom arc sweeps from 0 deg to 90 deg (bottom center) (+90 deg sweep)
        rightHalfPath.reset();
        rightHalfPath.moveTo(cx, top);
        rightHalfPath.arcTo(new RectF(bounds.left, bounds.top, bounds.right, bounds.top + 2 * r), 270f, 90f, false);
        rightHalfPath.lineTo(bounds.right, bounds.bottom - r);
        rightHalfPath.arcTo(new RectF(bounds.left, bounds.bottom - 2 * r, bounds.right, bounds.bottom), 0f, 90f, false);
        rightHalfPath.lineTo(cx, bottom);

        rightPathMeasure = new PathMeasure(rightHalfPath, false);
        rightTotalLen = rightPathMeasure.getLength();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bounds.isEmpty()) return;

        // 1. Draw base track capsule
        canvas.drawPath(fullCapsulePath, isDanger ? dangerPaint : trackPaint);

        if (isDanger) return;

        // 2. If completely verified, draw entire perimeter green
        if (isComplete) {
            canvas.drawPath(fullCapsulePath, progressPaint);
            return;
        }

        // 3. Draw Left Arc Progress
        if (leftProgress > 0f && leftPathMeasure != null && leftTotalLen > 0f) {
            leftSegmentPath.reset();
            float stopD = leftTotalLen * Math.min(1.0f, leftProgress);
            leftPathMeasure.getSegment(0f, stopD, leftSegmentPath, true);
            canvas.drawPath(leftSegmentPath, progressPaint);
        }

        // 4. Draw Right Arc Progress
        if (rightProgress > 0f && rightPathMeasure != null && rightTotalLen > 0f) {
            rightSegmentPath.reset();
            float stopD = rightTotalLen * Math.min(1.0f, rightProgress);
            rightPathMeasure.getSegment(0f, stopD, rightSegmentPath, true);
            canvas.drawPath(rightSegmentPath, progressPaint);
        }
    }

    public void setProgress(float left, float right) {
        this.leftProgress = Math.max(0f, Math.min(1f, left));
        this.rightProgress = Math.max(0f, Math.min(1f, right));
        this.isDanger = false;
        this.isComplete = (leftProgress >= 1f && rightProgress >= 1f);
        invalidate();
    }

    public void setComplete(boolean complete) {
        this.isComplete = complete;
        this.isDanger = false;
        invalidate();
    }

    public void setDanger(boolean danger) {
        this.isDanger = danger;
        invalidate();
    }

    public void reset() {
        this.leftProgress = 0f;
        this.rightProgress = 0f;
        this.isDanger = false;
        this.isComplete = false;
        invalidate();
    }
}
