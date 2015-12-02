package com.example.loh.gridview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 11/19/2015.
 */
public class WheelOfLuck extends View {
    private int radius;
    private int centerX, centerY;
    private Paint mTextPaint, mArcPaint;
    private RectF rect;
    private Canvas mCanvas;
    private int itemCount=6;
    private List<DivisionItem> divisionItems = new ArrayList<DivisionItem>();
    private Bitmap[] mImgsBitmaps;
    private String[] mTitle = new String[]{"1", "2", "3", "4", "5", "6"};
    private float mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20f, getResources().getDisplayMetrics());
    private int[] mColors = new int[]{0xFFFFC300, 0xFFF17E01, 0xFFFFC300, 0xFFF17E01, 0xFFFFC300, 0xFFF17E01};

    public WheelOfLuck(Context context, AttributeSet attrs) {
        super(context, attrs);
        Bitmap questionMark = BitmapFactory.decodeResource(getResources(), R.mipmap.question_mark);
        mImgsBitmaps = new Bitmap[itemCount];
        for(int i=0; i <itemCount; i++) mImgsBitmaps[i]=questionMark;
        mCanvas=new Canvas();
        mArcPaint=new Paint();
        mTextPaint = new Paint();
        mTextPaint.setColor(0xffffffff);
        mTextPaint.setTextSize(mTextSize);
        Log.e("constructor", itemCount + "");
        draw(mCanvas);
    }

    @Override
    protected void onDraw(Canvas mCanvas) {
        Log.e("onDraw",itemCount+"");
        int screenWidth=getMeasuredWidth();
        int screenHeight=getMeasuredHeight();
        int width = Math.min(screenWidth, screenHeight);
        radius=width/2;
        centerX=getMeasuredHeight()>getMeasuredWidth()?screenWidth/2:screenHeight/2;
        centerY=getMeasuredHeight()>getMeasuredWidth()?screenHeight/2:screenWidth/2;

        rect = new RectF();
        rect.set(getWidth() / 2 - radius, getHeight() / 2 - radius, getWidth() / 2 + radius, getHeight() / 2 + radius);
        float tmpAngle = 0;
        float sweepAngle = 360 / itemCount;
        for (int i = 0; i < itemCount; i++) {
            mArcPaint.setColor(mColors[i]);
            mArcPaint.setAntiAlias(true);
            mArcPaint.setDither(true);
            mArcPaint.setStrokeCap(Paint.Cap.BUTT);
            mCanvas.drawArc(rect, tmpAngle, sweepAngle, true, mArcPaint);
            drawTextOnPath(tmpAngle, sweepAngle, mTitle[i],mCanvas);
            drawIcon(tmpAngle, mImgsBitmaps[i],mCanvas);
            tmpAngle += sweepAngle;
        }
    }

    private void drawIcon(float tmpAngle, Bitmap bitmap, Canvas mCanvas) {
        int imgWidth = radius / 4;
        double angle = ((tmpAngle + 360 / itemCount / 2) * Math.PI / 180);
        int x = (int) (centerX + radius / 2 * Math.cos(angle));
        int y = (int) (centerY + radius / 2 * Math.sin(angle));
        Rect rect = new Rect(x - imgWidth / 2, y - imgWidth / 2, x + imgWidth / 2, y + imgWidth / 2);
        mCanvas.drawBitmap(bitmap, null, rect, null);
    }

    private void drawTextOnPath(float tmpAngle, float sweepAngle, String mStr, Canvas mCanvas) {
        Path path = new Path();
        path.addArc(rect, tmpAngle, sweepAngle);
        float textLength = mTextPaint.measureText(mStr);
        float hOffset = (float) ((radius*2 * Math.PI / itemCount - textLength) / 2);
        float vOffset = radius / 6;
        mCanvas.drawTextOnPath(mStr, path, hOffset, vOffset, mTextPaint);
    }

    public void setDivisionItems(List<DivisionItem> divisionItems){
        itemCount=divisionItems.size();
        mImgsBitmaps = new Bitmap[itemCount];
        mTitle = new String[itemCount];
        mColors = new int[itemCount];
        for(int i=0; i<itemCount;i++){
            mColors[i]=divisionItems.get(i).getColor();
            mTitle[i]=divisionItems.get(i).getTitle();
            mImgsBitmaps[i]=BitmapFactory.decodeFile(divisionItems.get(i).getPicturePath());
        }
    }
}
