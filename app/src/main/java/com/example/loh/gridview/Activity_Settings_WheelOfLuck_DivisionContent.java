package com.example.loh.gridview;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class Activity_Settings_WheelOfLuck_DivisionContent extends AppCompatActivity {
    @Bind (R.id.listView)
    ListView listView;
    private int numberOfDivision;
    private ArrayList<String> photos;
    private Adapter_ListView al;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_wheel_of_luck_division_content);
        ButterKnife.bind(this);

        numberOfDivision = getIntent().getIntExtra("NUMBER_OF_DIVISION", 8);
        photos = getIntent().getStringArrayListExtra("SELECTED_PHOTOS");

        al = new Adapter_ListView(this, numberOfDivision,photos);
        listView.setAdapter(al);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<DivisionItem> divisionItems = al.getDivisionItems();
                boolean editTextNotEmpty = false;
                for(DivisionItem divisionItem:divisionItems){
                    if(divisionItem.getTitle().isEmpty()){
                        new AlertDialog.Builder(Activity_Settings_WheelOfLuck_DivisionContent.this)
                                .setTitle("Error")
                                .setMessage("Empty title")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialoginterface, int i) {
                                        dialoginterface.dismiss();
                                    }
                                })
                                .create().show();
                        break;
                    }else{
                        editTextNotEmpty=true;
                    }
                }

                if(editTextNotEmpty){
                    Intent intent = new Intent(Activity_Settings_WheelOfLuck_DivisionContent.this, Activity_LuckyWheel.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("value", (Serializable) (al.getDivisionItems()));
                    DAOdb daOdb = new DAOdb(getApplicationContext());
                    daOdb.addDivisionItems(al.getDivisionItems());
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });
    }
}
