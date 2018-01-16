package com.app.slate.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.FrameLayout;

import com.app.slate.R;


public class AreaOverlayView extends FrameLayout {

    private static final float CENTRAL_POINT_RADIUS = 3;

    @Nullable
    private Bitmap windowFrame;
    private float radius = 0f;
    private int centerX = 0;
    private int centerY = 0;
    private float pointRadius;
    private Paint drawingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public AreaOverlayView(@NonNull Context context) {
        super(context);
        initView(context, null);
    }

    public AreaOverlayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public AreaOverlayView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public AreaOverlayView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context, attrs);
    }

    private void initView(@NonNull Context context, @Nullable AttributeSet attrs) {
        pointRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CENTRAL_POINT_RADIUS, getResources().getDisplayMetrics());
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs, R.styleable.AreaOverlayView, 0, 0);
            try {
                radius = a.getDimension(R.styleable.AreaOverlayView_radius, 0f);
            } finally {
                a.recycle();
            }
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (windowFrame == null) {
            createWindowFrame();
        }
        if (windowFrame != null) {
            canvas.drawBitmap(windowFrame, 0, 0, null);
        }
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public boolean isClickable() {
        return false;
    }

    protected void createWindowFrame() {

        int width = getWidth() > 0 ? getWidth() : getMeasuredWidth();
        int height = getHeight() > 0 ? getHeight() : getMeasuredHeight();
        if (width > 0 && height > 0) {
            windowFrame = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas osCanvas = new Canvas(windowFrame);

            centerX = width / 2;
            centerY = height / 2;

            if (radius > 0) {

                drawingPaint.setStyle(Paint.Style.STROKE);
                drawingPaint.setColor(Color.RED);
                drawingPaint.setAlpha(200);
                drawingPaint.setStrokeWidth(2);
                osCanvas.drawCircle(centerX, centerY, radius, drawingPaint);

                drawingPaint.setStyle(Paint.Style.FILL);
                drawingPaint.setAlpha(100);
                osCanvas.drawCircle(centerX, centerY, radius, drawingPaint);

                drawingPaint.setAlpha(255);
                osCanvas.drawCircle(centerX, centerY, pointRadius, drawingPaint);
            }
        }
    }

    @Override
    public boolean isInEditMode() {
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        windowFrame = null;
        super.onLayout(changed, l, t, r, b);
    }

    public float getRadius() {
        return radius;
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterY() {
        return centerY;
    }
}
