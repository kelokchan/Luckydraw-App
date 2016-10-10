package com.example.loh.gridview;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.victor.ringbutton.RingButton;

import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.PhotoPickerActivity;
import me.iwf.photopicker.utils.PhotoPickerIntent;

public class Activity_Selection extends AppCompatActivity {
    private static final int REQUEST_CODE = 12345;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        getSupportActionBar().hide();

        if (Build.VERSION.SDK_INT >= 23) {
            //do your check here
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(Activity_Selection.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }


        RingButton ringButton = (RingButton) findViewById(R.id.ringButton);
        ringButton.setOnClickListener(new RingButton.OnClickListener() {
            @Override
            public void clickUp() {

                Intent intent = new Intent(Activity_Selection.this, Activity_Box.class);
                startActivity(intent);
            }

            @Override
            public void clickDown() {
                try {
                    DAOdb daOdb = new DAOdb(Activity_Selection.this);
                    List<DivisionItem> divisionItemList = daOdb.getDivisionItems();
                    if (divisionItemList.size() < 1) {
                        Toast.makeText(Activity_Selection.this, "Previous settings not found", Toast.LENGTH_SHORT).show();

                        PhotoPickerIntent intent = new PhotoPickerIntent(Activity_Selection.this);
                        intent.setPhotoCount(16);
                        intent.setShowCamera(true);
                        intent.setShowGif(true);
                        startActivityForResult(intent, REQUEST_CODE);
                    } else {
                        Intent intent = new Intent(Activity_Selection.this, Activity_LuckyWheel.class);
                        startActivity(intent);
                    }
                } catch (Exception e) {

                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            if (data != null) {
                ArrayList<String> photos = data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS);
                Intent intent = new Intent(this, Activity_Settings_WheelOfLuck_DivisionContent.class);
                intent.putExtra("NUMBER_OF_DIVISION", photos.size());
                intent.putExtra("SELECTED_PHOTOS", photos);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            //resume tasks needing this permission
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
