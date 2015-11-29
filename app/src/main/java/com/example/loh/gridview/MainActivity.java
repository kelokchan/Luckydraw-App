package com.example.loh.gridview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import me.iwf.photopicker.PhotoPickerActivity;
import me.iwf.photopicker.utils.PhotoPickerIntent;
import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    GridView myGrid;
    int size;
    Button button1, start;
    ArrayList<String> frontPhotos;
    public static String backPhoto;
    ArrayList<Box> boxList;
    public Adapter_box ab;
    public static int numberOfCards = 9;
    GifImageView gifImageView;
    GifImageView congratsImageView;

    public static boolean clickable = false;
    public static final int NINE = 9;
    public static final int TWELVE = 12;
    public static final int FIFTEEN = 15;
    public static final int COLUMN_SIZE = 3;
    public static int row= 3;
    private DAOdb daOdb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myGrid = (GridView) findViewById(R.id.gridView);
        size = (getScreenWidth() / COLUMN_SIZE) - 50;
        myGrid.setColumnWidth(size);
        myGrid.setNumColumns(COLUMN_SIZE);

        gifImageView = (GifImageView) findViewById(R.id.gifImageView);
        congratsImageView = (GifImageView) findViewById(R.id.congratsImageView);

        boxList = new ArrayList<>();
        initDB();

        if (!boxList.isEmpty()) {
            ab = new Adapter_box(getApplicationContext(), boxList, true);
            ab.notifyDataSetChanged();
            myGrid.setAdapter(ab);
            backPhoto = boxList.get(0).getBack();
        }

        myGrid.setOnItemClickListener(this);

        button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoPickerIntent intent = new PhotoPickerIntent(MainActivity.this);
                intent.setPhotoCount(numberOfCards);
                intent.setShowCamera(false);
                intent.setShowGif(false);
                startActivityForResult(intent, 100);
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoPickerIntent intent = new PhotoPickerIntent(MainActivity.this);
                intent.setPhotoCount(1);
                intent.setShowCamera(false);
                intent.setShowGif(false);
                startActivityForResult(intent, 200);
            }
        });
        start = (Button) findViewById(R.id.start);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                gifImageView.setVisibility(View.GONE);
                congratsImageView.setAnimation(null);
                congratsImageView.setVisibility(View.GONE);

                for (int x = 0; x < myGrid.getChildCount(); x++) {
                    Adapter_box.ViewHolder holder = (Adapter_box.ViewHolder) myGrid.getChildAt(x).getTag();
                    flip(holder.front, holder.back, 1000);
                    moveViewToScreenCenter(holder.back);
                }

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        long seed = System.nanoTime();
                        Collections.shuffle(boxList, new Random(seed));
                        myGrid.setAdapter(new Adapter_box(getApplicationContext(), boxList, false));
                        start.setVisibility(View.GONE);
                        clickable = true;
                        start.setVisibility(View.VISIBLE);
                        start.setText("Ready");
                    }
                }, 2000);

            }
        });


    }

    private void initDB() {
        daOdb = new DAOdb(this);
        //        add images from database to images ArrayList
        for (Box mi : daOdb.getBoxImages()) {
            boxList.add(mi);
        }
    }

    public int getScreenWidth() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.widthPixels;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (clickable == true) {
            Adapter_box.ViewHolder holder = (Adapter_box.ViewHolder) view.getTag();
            flip(holder.back, holder.front, 1000);
            move(holder.front);
            final MediaPlayer c = MediaPlayer.create(this, R.raw.flip_card);
            c.start();
            clickable = false;


            final Handler handler = new Handler();

            handler.postDelayed(new Runnable() {

                @Override
                public void run() {

                    for (int x = 0; x < myGrid.getChildCount(); x++) {
                        Adapter_box.ViewHolder holders = (Adapter_box.ViewHolder) myGrid.getChildAt(x).getTag();
                        if (holders.back.getVisibility() == View.VISIBLE) {
                            flip(holders.back, holders.front, 1000);
                        }
                    }

                    gifImageView.setVisibility(View.VISIBLE);
                    congratsImageView.setVisibility(View.VISIBLE);

                    gifImageView.bringToFront();
                    congratsImageView.bringToFront();

                    Animation fadeAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
                    congratsImageView.setAnimation(fadeAnimation);

                    start.setVisibility(View.VISIBLE);
                    start.setText("Replay");
                }
            }, 2000);

        }
    }

    public void flip(final View front, final View back, final int duration) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {

            AnimatorSet set = new AnimatorSet();
            set.playSequentially(
                    ObjectAnimator.ofFloat(front, "rotationY", 90).setDuration(duration / 2),
                    ObjectAnimator.ofInt(front, "visibility", View.GONE).setDuration(0),
                    ObjectAnimator.ofFloat(back, "rotationY", -90).setDuration(0),
                    ObjectAnimator.ofInt(back, "visibility", View.VISIBLE).setDuration(0),
                    ObjectAnimator.ofFloat(back, "rotationY", 0).setDuration(duration / 2)

            );
            set.start();
        } else {
            front.animate().rotationY(90).setDuration(duration / 2).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    front.setVisibility(View.GONE);
                    back.setRotationY(-90);
                    back.setVisibility(View.VISIBLE);
                    back.animate().rotationY(0).setDuration(duration / 2).setListener(null);
                }
            });
        }
    }

    private void moveViewToScreenCenter(View view) {
        RelativeLayout root = (RelativeLayout) findViewById(R.id.gridView_layout);
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int statusBarOffset = dm.heightPixels - root.getMeasuredHeight();

        int originalPos[] = new int[2];
        view.getLocationOnScreen(originalPos);

        int xDest = dm.widthPixels / 2;
        xDest -= (view.getMeasuredWidth() / 2);
        int yDest = dm.heightPixels / 2 - (view.getMeasuredHeight() / 2) - statusBarOffset;

        TranslateAnimation anim = new TranslateAnimation(0, xDest - originalPos[0], 0, yDest - originalPos[1]);
        anim.setDuration(1000);
        anim.setRepeatCount(1);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setFillAfter(true);
        view.startAnimation(anim);
    }

    public void move(View front) {
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, 25);
        animation.setDuration(100);
        animation.setFillAfter(true);
        animation.setRepeatCount(-1);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setFillAfter(true);
        front.startAnimation(animation);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 100) {
            if (data != null) {
                ArrayList<String> photos =
                        data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS);
                frontPhotos = photos;
                boxList.clear();
                daOdb.deleteAllBoxes();
                for (String photo : frontPhotos) {
                    Box box = new Box();
                    box.setFront(photo);
                    box.setBack(backPhoto);
                    boxList.add(box);
                    daOdb.addBoxImage(box);
                }

                ab = new Adapter_box(getApplicationContext(), boxList, true);
                ab.notifyDataSetChanged();
                myGrid.setAdapter(ab);
            }
        }

        if (resultCode == RESULT_OK && requestCode == 200) {
            if (data != null) {
                ArrayList<String> photos =
                        data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS);
                backPhoto = photos.get(0);
                if (!boxList.isEmpty()) {
                    for (Box box : boxList) {
                        box.setBack(backPhoto);
                    }
                    daOdb.addBoxBack(backPhoto);
                    ab = new Adapter_box(getApplicationContext(), boxList, true);
                    ab.notifyDataSetChanged();
                    myGrid.setAdapter(ab);
                }
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_box, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.x9) {
            numberOfCards = NINE;
            size = (getScreenWidth() / 3) - 50;
            myGrid.setColumnWidth(size);
            myGrid.setNumColumns(3);
            int deleteCount = boxList.size() - NINE;
            for (int i = 0; i < deleteCount; i++) {
                Box box = boxList.get(i);
                daOdb.deleteBoxImage(box);
                boxList.remove(i);
            }
            ab = new Adapter_box(getApplicationContext(), boxList, true);
            myGrid.setAdapter(ab);

            View positiveButton = findViewById(R.id.gridView);
            RelativeLayout.LayoutParams layoutParams =
                    (RelativeLayout.LayoutParams)positiveButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            positiveButton.setLayoutParams(layoutParams);

            return true;
        } else if (id == R.id.x12) {
            numberOfCards = TWELVE;
            size = (getScreenWidth() / 3) - 50;
            myGrid.setColumnWidth(size);
            myGrid.setNumColumns(3);
            int deleteCount = boxList.size() - TWELVE;
            for (int i = 0; i < deleteCount; i++) {
                Box box = boxList.get(i);
                daOdb.deleteBoxImage(box);
                boxList.remove(i);
            }
            row = 4;
            ab = new Adapter_box(getApplicationContext(), boxList, true);
            myGrid.setAdapter(ab);

            View positiveButton = findViewById(R.id.gridView);
            RelativeLayout.LayoutParams layoutParams =
                    (RelativeLayout.LayoutParams)positiveButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            positiveButton.setLayoutParams(layoutParams);

            return true;
        } else if (id == R.id.x15) {
            numberOfCards = FIFTEEN;
            size = (getScreenWidth() / 3) - 50;
            myGrid.setColumnWidth(size);
            myGrid.setNumColumns(3);
            row = 5;
            ab = new Adapter_box(getApplicationContext(), boxList, true);
            myGrid.setAdapter(ab);

            View positiveButton = findViewById(R.id.gridView);
            RelativeLayout.LayoutParams layoutParams =
                    (RelativeLayout.LayoutParams)positiveButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            positiveButton.setLayoutParams(layoutParams);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
