package com.example.loh.gridview;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.iwf.photopicker.PhotoPickerActivity;
import me.iwf.photopicker.utils.PhotoPickerIntent;
import pl.droidsonroids.gif.GifImageView;

public class Activity_LuckyWheel extends AppCompatActivity {
    private static final int RESULT_LOAD_BACKGROUND = 1234;
    private static final int REQUEST_CODE = 12345;
    @Bind(R.id.relativeLayout)
    RelativeLayout rl;
    @Bind(R.id.wheelOfLuck)
    WheelOfLuck wheelOfLuck;
    @Bind(R.id.spinner)
    ImageButton spinner;
    @Bind(R.id.imgview_wheel_bg)
    ImageView wheel_bg;
    private double mCurrAngle = 0;
    private double mPrevAngle = 0;
    private double angularAcceleration;
    private long initTime;
    private long diffTime;
    public static boolean toolbarHidden = true;
    private VelocityTracker tracker = VelocityTracker.obtain();
    // Charging View attributes
    @Bind(R.id.dynamicArcView)
    DecoView chargingProgressView;
    private int mSeriesIndex;
    // To hold angle when spin ends
    private double currAngle = 0;
    private static String backgroundPath;
    private static final String PREFERENCE_NAME = "SETTINGS";
    private static final String BACKGROUND_KEY = "WHEEL_BACKGROUND";

    @Bind(R.id.gifImageView)
    GifImageView gifImageView;
    @Bind(R.id.congratsImageView)
    GifImageView congratsImageView;

    SharedPreferences sharedPreferences;
    private DAOdb daOdb;
    List<DivisionItem> divisionItemList;
    private WheelOfLuck_WinningSector sector;
    private ImageButton tempSpinner;
    private ImageView tempPointer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lucky_wheel);
        getSupportActionBar().hide();

        ButterKnife.bind(this);
        wheelOfLuck.setContext(Activity_LuckyWheel.this);


        daOdb = new DAOdb(this);
        divisionItemList = daOdb.getDivisionItems();
        Bundle bundle = getIntent().getExtras();
        //divisionItemList = (ArrayList<DivisionItem>)bundle.getSerializable("value");
        currAngle=0;sweepAngle=(float)360 / divisionItemList.size();
        wheelOfLuck.setDivisionItems(divisionItemList);

        spinner.setOnTouchListener(spinnerOnTouchListener);
        wheelOfLuck.setOnTouchListener(wheelOnTouchListener);

        sharedPreferences = getSharedPreferences(PREFERENCE_NAME, 0);
        backgroundPath = sharedPreferences.getString(BACKGROUND_KEY, null);
        BitmapDrawable background = new BitmapDrawable(BitmapFactory.decodeFile(backgroundPath));
        rl.setBackgroundDrawable(background);

        blinkingAnimation(wheel_bg, 500);
    }

    private float sectorStartAngle;
    private float sweepAngle;
    // Lucky Wheel onTouch
    View.OnTouchListener wheelOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            hideEffects();

            final float xc = wheelOfLuck.getWidth() / 2;
            final float yc = wheelOfLuck.getHeight() / 2;

            final float x = event.getX();
            final float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    mCurrAngle = Math.toDegrees(Math.atan2(x - xc, yc - y));
                    initTime = System.currentTimeMillis();
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    mPrevAngle = mCurrAngle;
                    mCurrAngle = Math.toDegrees(Math.atan2(x - xc, yc - y));
                    spin(mPrevAngle, mCurrAngle, 0, false);
                    tracker.addMovement(event);
                    tracker.computeCurrentVelocity(1000);
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    double initialVelocity = (Math.sqrt(Math.pow(tracker.getXVelocity(), 2) + Math.pow(tracker.getYVelocity(), 2))) / 100;
                    diffTime = System.currentTimeMillis() - initTime;
                    angularAcceleration = initialVelocity / diffTime;
                    currAngle = mCurrAngle;
                    if (initialVelocity > 5) {
                        long durationMillis = (long) (initialVelocity / angularAcceleration);
                        double mStopAngle = initialVelocity * durationMillis + angularAcceleration * Math.pow(durationMillis, 2) / 2;
                        if (mPrevAngle > mCurrAngle) {
                            mStopAngle = -mStopAngle;
                            sectorStartAngle = (float) (((mStopAngle / 10) % 360));
                            Log.e("da", sectorStartAngle + "");
                            while ((sectorStartAngle - sweepAngle) < -90)
                                sectorStartAngle += sweepAngle;
                            sectorStartAngle -= 2 * sweepAngle;
                        } else {
                            sectorStartAngle = (float) (((Math.abs(mStopAngle) / 10) % 360));
                            while (sectorStartAngle > -90) {
                                sectorStartAngle -= sweepAngle;
                            }
                        }
                        double angleDiff = Math.abs(mStopAngle / 10 - mCurrAngle);
                        if (angleDiff > 3000)
                            spin(mCurrAngle, mStopAngle / 10, durationMillis * 32, true);
                        else if (angleDiff > 2500)
                            spin(mCurrAngle, mStopAngle / 10, durationMillis * 28, true);
                        else if (angleDiff > 2000)
                            spin(mCurrAngle, mStopAngle / 10, durationMillis * 18, true);
                        else spin(mCurrAngle, mStopAngle / 10, durationMillis * 8, true);
                        mPrevAngle = mCurrAngle = 0;
                    }
                    break;
                }
            }
            return true;
        }
    };

    // Spin button onTouch
    View.OnTouchListener spinnerOnTouchListener = new View.OnTouchListener() {
        long startTime;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            hideEffects();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {

                    startTime = System.nanoTime();
                    // Spinner onHold to start charging, setup charging circular view
                    chargingProgressView.configureAngles(360, 0);
                    final SeriesItem seriesItem = new SeriesItem.Builder(Color.parseColor("#00FFFF"))
                            .setRange(0, 50f, 0)
                            .setInset(new PointF(12f, 12f)) //20f,20f
                            .setInterpolator(new LinearInterpolator())
                            .setLineWidth(20)
                            .setSpinDuration(5000)
                            .setSpinClockwise(true)
                            .build();
                    mSeriesIndex = chargingProgressView.addSeries(seriesItem);
                    chargingProgressView.setVisibility(View.VISIBLE);
                    chargingProgressView.addEvent(new DecoEvent.Builder(50).setIndex(mSeriesIndex).build());
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    long difference_Time = System.nanoTime() - startTime;
                    double ratio_Time = difference_Time / 5e+9;

                    double mStopAngle = currAngle + 10828 * ratio_Time;
                    sectorStartAngle = (float) (((mStopAngle) % 360));
                    while (sectorStartAngle > -90) sectorStartAngle -= sweepAngle;
                    if (ratio_Time > 0.1)
                        spin(currAngle, mStopAngle, (long) (ratio_Time * 8000), true);
                    else
                        spin(currAngle, currAngle + 10828 * ratio_Time, (long) (ratio_Time * 10000), true);
                    chargingProgressView.deleteAll();
                    chargingProgressView.setVisibility(View.INVISIBLE);
                    break;
                }
            }
            return true;
        }
    };

    // Rotation animation of lucky wheel
    private void spin(double fromDegrees, double toDegrees, long durationMillis, boolean isSpinning) {
        if (durationMillis > 8000) durationMillis = 8000;
        final RotateAnimation rotate = new RotateAnimation((float) fromDegrees, (float) toDegrees,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(durationMillis);
        rotate.setFillEnabled(true);
        rotate.setFillAfter(true);
        rotate.setInterpolator(new DecelerateInterpolator());
        if (isSpinning == true) {
            rotate.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation arg0) {
                    spinner.setOnTouchListener(null);
                    wheelOfLuck.setOnTouchListener(null);
                }

                @Override
                public void onAnimationRepeat(Animation arg0) {
                }

                @Override
                public void onAnimationEnd(Animation arg0) {
                    spinner.setOnTouchListener(spinnerOnTouchListener);
                    wheelOfLuck.setOnTouchListener(wheelOnTouchListener);
                    showEffect();
                }
            });
        }
        wheelOfLuck.startAnimation(rotate);
        currAngle = toDegrees;
    }

    public void showEffect() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                try {
                    // Add winning sector indicator
                    sector = new WheelOfLuck_WinningSector(getApplicationContext(), null, sectorStartAngle, sweepAngle);
                    RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeLayout);
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                            , ViewGroup.LayoutParams.WRAP_CONTENT);
                    if(getResources().getBoolean(R.bool.isTablet)){
                        lp.setMargins((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics()),
                                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics()),
                                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics()),
                                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics()));
                        /*float scale = getResources().getDisplayMetrics().density;
                        lp.setMargins((int) (150*scale + 0.5f),
                            (int) (150*scale + 0.5f),
                            (int) (150*scale + 0.5f),
                            (int) (150*scale + 0.5f));*/
                    }

                    sector.setLayoutParams(lp);
                    rl.addView(sector);
                    blinkingAnimation(sector, 100);

                    tempPointer = new ImageView(getApplicationContext());
                    RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT
                            , ViewGroup.LayoutParams.WRAP_CONTENT);
                    lp1.setMargins(0, 0, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 67, getResources().getDisplayMetrics()));
                    lp1.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
                    lp1.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.spinner);
                    tempPointer.setLayoutParams(lp1);
                    tempPointer.setImageResource(R.mipmap.pointer);
                    rl.addView(tempPointer);

                    tempSpinner = new ImageButton(getApplicationContext());
                    RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics())
                            , (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics()));
                    lp2.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                    tempSpinner.setLayoutParams(lp2);
                    tempSpinner.setBackgroundResource(R.mipmap.spinner);
                    rl.addView(tempSpinner);
                    tempSpinner.setOnTouchListener(spinnerOnTouchListener);

                    MediaPlayer musicPlayer = MediaPlayer.create(getApplicationContext(), R.raw.congrats);
                    gifImageView.setVisibility(View.VISIBLE);
                    congratsImageView.setVisibility(View.VISIBLE);
                    gifImageView.bringToFront();
                    congratsImageView.bringToFront();
                    musicPlayer.start();
                    Animation fadeAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
                    congratsImageView.setAnimation(fadeAnimation);
                    musicPlayer = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 500);
    }

    public void hideEffects() {
        try {
            if (sector != null) ((ViewManager) sector.getParent()).removeView(sector);
            if (tempSpinner != null) ((ViewManager) tempSpinner.getParent()).removeView(tempSpinner);
            if (tempPointer != null) ((ViewManager) tempPointer.getParent()).removeView(tempPointer);
        } catch (Exception e) {
        }

        congratsImageView.setAnimation(null);
        gifImageView.setVisibility(View.GONE);
        congratsImageView.setVisibility(View.GONE);
    }

    //hide the bloody toolbar
    public void hideToolbar(View view) {
        if (toolbarHidden) {
            getSupportActionBar().show();
            toolbarHidden = false;
        } else {
            getSupportActionBar().hide();
            toolbarHidden = true;
        }
    }

    public void blinkingAnimation(View view, int duration) {
        final Animation animation = new AlphaAnimation(1, 0);
        animation.setDuration(duration);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        view.startAnimation(animation);
    }

    // Inflate overflow menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_luckywheel, menu);
        return true;
    }

    // Overflow menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_luckywheel:
                daOdb.deleteAllDivisionItems();
                PhotoPickerIntent intent = new PhotoPickerIntent(Activity_LuckyWheel.this);
                intent.setPhotoCount(16);
                intent.setShowCamera(true);
                intent.setShowGif(true);
                startActivityForResult(intent, REQUEST_CODE);
                break;
            case R.id.edit_background:
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_BACKGROUND);
                break;
            default:
                break;
            case R.id.clear_background:
                rl.setBackgroundDrawable(null);
                sharedPreferences = getSharedPreferences(PREFERENCE_NAME, 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(BACKGROUND_KEY, "");
                editor.apply();
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Change background
        if (requestCode == RESULT_LOAD_BACKGROUND && resultCode == Activity.RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            try {
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);

                sharedPreferences = getSharedPreferences(PREFERENCE_NAME, 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(BACKGROUND_KEY, picturePath);
                editor.apply();
                BitmapDrawable background = new BitmapDrawable(BitmapFactory.decodeFile(picturePath));
                rl.setBackgroundDrawable(null);
                rl.setBackgroundDrawable(background);
                cursor.close();
            } catch (Exception e) {
                try {
                    Bitmap selected_image = getBitmapFromUri(selectedImage);
                    BitmapDrawable ob = new BitmapDrawable(getResources(), selected_image);
                    rl.setBackgroundDrawable(null);
                    rl.setBackgroundDrawable(ob);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        // Open Gallery library to start edit wheel
        else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            if (data != null) {
                ArrayList<String> photos = data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS);
                Intent intent = new Intent(this, Activity_Settings_WheelOfLuck_DivisionContent.class);
                intent.putExtra("NUMBER_OF_DIVISION", photos.size());
                intent.putExtra("SELECTED_PHOTOS", photos);
                startActivity(intent);
            }
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    // Back to Main activity
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Activity_LuckyWheel.this, Activity_Selection.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}