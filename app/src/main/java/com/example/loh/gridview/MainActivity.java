package com.example.loh.gridview;

import android.app.Dialog;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import me.iwf.photopicker.PhotoPickerActivity;
import me.iwf.photopicker.utils.PhotoPickerIntent;
import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    GridView myGrid;
    int size;
    Button frontButton, backButton;
    FloatingActionButton start;
    ArrayList<String> frontPhotos;
    public static String backPhoto;
    ArrayList<Box> boxList= new ArrayList<>();;
    public Adapter_box ab;
    public static final int MAXIMUM_CARDS = 16;
    public static boolean expandable = true;
    GifImageView gifImageView;
    GifImageView congratsImageView;

    public static boolean clickable = false;
    public static final int COLUMN_SIZE = 3;
    public static final int COLUMN_SIZE_COMPACT = 4;
    private DAOdb daOdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myGrid = (GridView) findViewById(R.id.gridView);
        myGrid.setOnItemClickListener(this);
        gifImageView = (GifImageView) findViewById(R.id.gifImageView);
        congratsImageView = (GifImageView) findViewById(R.id.congratsImageView);
        initDB();

        size = getSize(COLUMN_SIZE);
        myGrid.setColumnWidth(size);
        myGrid.setNumColumns(COLUMN_SIZE);
        layoutCenterInParent();

        if (!boxList.isEmpty()) {
            setBoxSize();
            myGrid.setColumnWidth(size);
            ab = new Adapter_box(getApplicationContext(), boxList, true);
            ab.notifyDataSetChanged();
            myGrid.setAdapter(ab);
            backPhoto = boxList.get(0).getBack();
        }

        frontButton = (Button) findViewById(R.id.button1);
        frontButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoPickerIntent intent = new PhotoPickerIntent(MainActivity.this);
                intent.setPhotoCount(MAXIMUM_CARDS);
                intent.setShowCamera(true);
                intent.setShowGif(false);
                startActivityForResult(intent, 100);
            }
        });

        backButton = (Button) findViewById(R.id.button2);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoPickerIntent intent = new PhotoPickerIntent(MainActivity.this);
                intent.setPhotoCount(1);
                intent.setShowCamera(true);
                intent.setShowGif(false);
                startActivityForResult(intent, 200);
            }
        });

        start = (FloatingActionButton) findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                expandable = false;
                start.setVisibility(View.GONE);
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
                        start.setImageResource(R.drawable.ic_replay_white_24dp);
                        start.setVisibility(View.VISIBLE);
                    }
                }, 2000);

            }
        });


    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

        if (clickable) {
            Adapter_box.ViewHolder holder = (Adapter_box.ViewHolder) view.getTag();
            flip(holder.back, holder.front, 1000);
            move(holder.front);
            zoom(holder.front);
            clickable = false;
            expandable = true;
            final Handler handler = new Handler();

            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    final MediaPlayer c = MediaPlayer.create(getApplicationContext(), R.raw.congrats);
                    c.start();
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
                }
            }, 1000);
        }else if (expandable){
            final Dialog dialog = new Dialog(MainActivity.this);
            // Include dialog.xml file
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.activity_dialog);
            ImageView image = (ImageView) dialog.findViewById(R.id.goProDialogImage);
            Picasso.with(dialog.getContext()).load(new File(boxList.get(position).front)).resize(1000,1000).centerCrop()
                    .into(image);
            dialog.show();
            // if decline button is clicked, close the custom dialog

        }
    }

    //TODO set the entire GridView layout to the center of screen
    public void layoutCenterInParent(){
        View view = findViewById(R.id.gridView);
        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams)view.getLayoutParams();
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        view.setLayoutParams(layoutParams);

    }

    private void initDB() {
        daOdb = new DAOdb(this);
        //        add images from database to images ArrayList
        for (Box mi : daOdb.getBoxImages()) {
            boxList.add(mi);
        }
    }

    //TODO get the width of the screen in order to decide the width of column
    public int getSize(int column) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        return (displaymetrics.widthPixels/column)-50;
    }

    //TODO Animation- flip the selected card
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

    //TODO Animation-shuffle card to the center of screen
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

    //TODO Animation-move the selected card
    public void move(View front) {
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, 25);
        animation.setDuration(100);
        animation.setFillAfter(true);
        animation.setRepeatCount(-1);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setFillAfter(true);
        front.startAnimation(animation);
    }

    //TODO Animation-zoom in the selected card
    public void zoom(View front){
        front.bringToFront();
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(front, "scaleX", 1.5f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(front, "scaleY", 1.5f);

        scaleDownX.setDuration(1000);
        scaleDownY.setDuration(1000);

        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.play(scaleDownX).with(scaleDownY);
        scaleDown.start();
    }

    public void setBoxSize(){
        if(boxList.size()>12){
            size = getSize(COLUMN_SIZE_COMPACT);
            myGrid.setColumnWidth(size);
            myGrid.setNumColumns(COLUMN_SIZE_COMPACT);
        }else{
            size = getSize(COLUMN_SIZE);
            myGrid.setColumnWidth(size);
            myGrid.setNumColumns(COLUMN_SIZE);
        }
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
                }
            }
        }

        setBoxSize();
        clickable = false;
        ab = new Adapter_box(getApplicationContext(), boxList, true);
        ab.notifyDataSetChanged();
        myGrid.setAdapter(ab);
        start.setBackgroundResource(R.drawable.ic_play_arrow_white_24dp);
        layoutCenterInParent();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_box, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.normalMode){
            frontButton.setVisibility(View.GONE);
            backButton.setVisibility(View.GONE);
            start.setVisibility(View.VISIBLE);
            return true;
        }else if(id == R.id.editMode){
            frontButton.setVisibility(View.VISIBLE);
            backButton.setVisibility(View.VISIBLE);
            start.setVisibility(View.GONE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
