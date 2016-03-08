package com.example.loh.gridview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by User on 3/8/2016.
 */
public class WheelOfLuck_WinningSector extends View {
    private Paint mArcPaint;
    private RectF rect;
    private Canvas mCanvas;
    private int itemCount;
    private int radius;
    private float startAngle, sweepAngle;

    public WheelOfLuck_WinningSector(Context context, AttributeSet attrs, float startAngle, float sweepAngle) {
        super(context, attrs);
        this.startAngle = startAngle;
        this.sweepAngle = sweepAngle;
        mCanvas = new Canvas();
        mArcPaint = new Paint();
        draw(mCanvas);
    }

    @Override
    protected void onDraw(Canvas mCanvas) {
        int screenWidth=getMeasuredWidth();
        int screenHeight=getMeasuredHeight();
        int width = Math.min(screenWidth, screenHeight);
        radius=width/2-80;

        rect = new RectF();
        rect.set(getWidth() / 2 - radius, getHeight() / 2 - radius, getWidth() / 2 + radius, getHeight() / 2 + radius);
        mArcPaint.setColor(Color.YELLOW);
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(15);
        mArcPaint.setDither(true);
        mArcPaint.setStrokeCap(Paint.Cap.BUTT);
        mCanvas.drawArc(rect, startAngle, sweepAngle, true, mArcPaint);
    }
}
