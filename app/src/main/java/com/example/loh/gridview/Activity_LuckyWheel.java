package com.example.loh.gridview;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
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
    private double mCurrAngle = 0;
    private double mPrevAngle = 0;
    private double angularAcceleration;
    private long initTime;
    private long diffTime;
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
    MediaPlayer musicPlayer;

    @Bind(R.id.gifImageView)
    GifImageView gifImageView;
    @Bind(R.id.congratsImageView)
    GifImageView congratsImageView;

    SharedPreferences sharedPreferences;
    private DAOdb daOdb;
    List<DivisionItem> divisionItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lucky_wheel);
        ButterKnife.bind(this);

        daOdb = new DAOdb(this);
        divisionItemList = daOdb.getDivisionItems();
        Bundle bundle = getIntent().getExtras();
/*        divisionItemList = (ArrayList<DivisionItem>)bundle.getSerializable("value");*/
        wheelOfLuck.setDivisionItems(divisionItemList);

        spinner.setOnTouchListener(spinnerOnTouchListener);
        wheelOfLuck.setOnTouchListener(wheelOnTouchListener);

        sharedPreferences = getSharedPreferences(PREFERENCE_NAME, 0);
        backgroundPath = sharedPreferences.getString(BACKGROUND_KEY, null);
        BitmapDrawable background = new BitmapDrawable(BitmapFactory.decodeFile(backgroundPath));
        rl.setBackgroundDrawable(background);

        musicPlayer = MediaPlayer.create(this, R.raw.congrats);
    }

    @OnClick(R.id.spinner)
    public void spinnerOnClick() {
        //spin(currAngle, 12828, 3000, true);
    }

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
                        if (mPrevAngle > mCurrAngle) mStopAngle = -mStopAngle;
                        spin(mCurrAngle, mStopAngle / 10, durationMillis * 8, true);
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
                    Log.e("abs", 10 * ratio_Time + "");
                    spin(currAngle, 12828 * ratio_Time, (long) (ratio_Time * 5000), true);
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
                gifImageView.setVisibility(View.VISIBLE);
                congratsImageView.setVisibility(View.VISIBLE);
                gifImageView.bringToFront();
                congratsImageView.bringToFront();
                Animation fadeAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
                congratsImageView.setAnimation(fadeAnimation);
                musicPlayer.start();
            }
        }, 500);
    }

    public void hideEffects() {
        congratsImageView.setAnimation(null);
        gifImageView.setVisibility(View.GONE);
        congratsImageView.setVisibility(View.GONE);
        if (musicPlayer.isPlaying()) {
            musicPlayer.stop();
        }
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
            rl.setBackgroundDrawable(background);
            cursor.close();
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

    // Back to Main activity
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Activity_Selection.class);
        startActivity(intent);
    }
}