package com.example.loh.gridview;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 12/1/2015.
 */
public class Adapter_ListView extends BaseAdapter{
    private static final int RESULT_LOAD_IMAGE = 123;
    private int numberOfDivision;
    private List<String> photos;
    private List<Integer> color = new ArrayList<Integer>();
    private List<String> title = new ArrayList<String>();
    private Context context;
    private LayoutInflater inflater;

    public Adapter_ListView(Context context, int numberOfDivision, ArrayList<String> photos) {
        this.numberOfDivision = numberOfDivision;
        this.photos = photos;
        this.context = context;
        inflater = LayoutInflater.from(context);
        for(int i=0; i<numberOfDivision ; i++){
            if (i % 2 == 0) color.add(0xFFFFC300);
            else color.add(0xFFF17E01);
            title.add("");
        }
    }

    @Override
    public int getCount() {
        return numberOfDivision;
    }

    @Override
    public Object getItem(int position) {
        return numberOfDivision;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public class ViewHolder {
        EditText title;
        ImageView color, image;

        public ViewHolder(View view) {
            title=(EditText)view.findViewById(R.id.et_title);
            color=(ImageView)view.findViewById(R.id.imgview_color);
            image=(ImageView)view.findViewById(R.id.imgview_image);
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;
        final ViewHolder holder;

        if (v == null) {
            v = inflater.inflate(R.layout.item_division_settings, parent, false);
            holder = new ViewHolder(v);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        holder.title.setText(title.get(position));
        holder.color.setBackgroundColor(color.get(position));
        Picasso.with(context).load(new File(photos.get(position))).resize(100,100).centerInside().into(holder.image);
        holder.image.setTag(photos.get(position));
        holder.color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialogBuilder.with(context)
                        .setTitle("Choose color")
                        .initialColor(Color.RED)
                        .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                        .density(12)
                        .setPositiveButton("ok", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                holder.color.setBackgroundColor(selectedColor);
                                color.set(position, selectedColor);
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .build()
                        .show();
            }
        });

        holder.title.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    title.set(position, holder.title.getText().toString());
                }
            }
        });

        return v;
    }

    public ArrayList<DivisionItem> getDivisionItems(){
        ArrayList<DivisionItem> divisionItems = new ArrayList<DivisionItem>();
        for(int i = 0; i<numberOfDivision; i++){
            divisionItems.add(new DivisionItem(color.get(i),title.get(i),photos.get(i)));
        }
        return divisionItems;
    }
}
