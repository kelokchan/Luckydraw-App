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
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by User on 11/19/2015.
 */
public class WheelOfLuck extends View {
    private Context context;
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
        draw(mCanvas);
    }

    @Override
    protected void onDraw(Canvas mCanvas) {
        int screenWidth=getMeasuredWidth();
        int screenHeight=getMeasuredHeight();
        int width = Math.min(screenWidth, screenHeight);
        radius=width/2-80;
        centerX=screenWidth/2;
        centerY=screenHeight/2;

        rect = new RectF();
        rect.set(getWidth() / 2 - radius, getHeight() / 2 - radius, getWidth() / 2 + radius, getHeight() / 2 + radius);
        float tmpAngle = 0;
        float sweepAngle = (float)360 / itemCount;
        for (int i = 0; i < itemCount; i++) {
            mArcPaint.setColor(mColors[i]);
            mArcPaint.setAntiAlias(true);
            mArcPaint.setDither(true);
            mArcPaint.setStrokeCap(Paint.Cap.BUTT);
            mCanvas.drawArc(rect, tmpAngle, sweepAngle, true, mArcPaint);
            drawTextOnPath(tmpAngle, sweepAngle, mTitle[i], mCanvas);
            drawIcon(tmpAngle, mImgsBitmaps[i], mCanvas);
            tmpAngle += sweepAngle;
        }
    }

    private void drawIcon(float tmpAngle, Bitmap bitmap, Canvas mCanvas) {
        Bitmap scaledBitmap = null;
        if(bitmap != null) {
            scaledBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
        }
        int imgWidth = radius / 4;
        double angle = ((tmpAngle + 360 / itemCount / 2) * Math.PI / 180);
        int x = (int) (centerX + radius *5/6 * Math.cos(angle));
        int y = (int) (centerY + radius *5/6 * Math.sin(angle));

        if(context!=null){
            CircleImageView circleImageView = new CircleImageView(context);
            circleImageView.setImageBitmap(scaledBitmap);
            //Set position of circle view
            circleImageView.setLeft(x - imgWidth / 2);
            circleImageView.setTop(y - imgWidth / 2);
            circleImageView.setRight(x + imgWidth / 2);
            circleImageView.setBottom(y + imgWidth / 2);

            circleImageView.setDrawingCacheEnabled(true);
            circleImageView.buildDrawingCache();
            //Size of circle to draw
            Rect rect = new Rect(x - imgWidth / 2, y - imgWidth / 2, x + imgWidth / 2, y + imgWidth / 2);
            mCanvas.drawBitmap(circleImageView.getDrawingCache(), null, rect, null);
        }

        //Text out circle in code
        //Rect rect = new Rect(x - imgWidth / 2, y - imgWidth / 2, x + imgWidth / 2, y + imgWidth / 2);
        //mCanvas.drawBitmap(bitmap, null, rect, null);
    }

    private void drawTextOnPath(float tmpAngle, float sweepAngle, String mStr, Canvas mCanvas) {
        float angle = (float)((tmpAngle + 360 / itemCount / 2) * Math.PI / 180);
        float x = (float) (centerX + radius *2/3 * Math.cos(angle));
        float y = (float) (centerY + radius *2/3 * Math.sin(angle));

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(centerX, centerY);
        mCanvas.drawTextOnPath(mStr, path, 0, 20, mTextPaint);

        //Text out circle in code
        /*Path path = new Path();
        path.addArc(rect, tmpAngle, sweepAngle);
        float textLength = mTextPaint.measureText(mStr);
        float hOffset = (float) ((radius*2 * Math.PI / itemCount - textLength) / 2);
        float vOffset = radius / 6;
        mCanvas.drawTextOnPath(mStr, path, hOffset, vOffset, mTextPaint);*/
    }

    public void setDivisionItems(List<DivisionItem> divisionItems){
        itemCount=divisionItems.size();
        mImgsBitmaps = new Bitmap[itemCount];
        mTitle = new String[itemCount];
        mColors = new int[itemCount];
        for(int i=0; i<itemCount;i++){
            mColors[i]=divisionItems.get(i).getColor();
            mTitle[i]=divisionItems.get(i).getTitle();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inDither = true;
            Bitmap bitmap = BitmapFactory.decodeFile(divisionItems.get(i).getPicturePath(),options);
            if (bitmap != null)
                mImgsBitmaps[i]=Bitmap.createScaledBitmap(bitmap, 100, 100, false);
        }
    }

    public void setContext(Context context){
        this.context = context;
    }
}
