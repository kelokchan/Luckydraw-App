package com.example.loh.gridview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Loh on 25/11/2015.
 */
public class Adapter_box extends BaseAdapter {

    ArrayList<Box> box_list = new ArrayList<>();
    Context context;
    boolean status;

    public Adapter_box(Context context, ArrayList<Box> box_list,boolean status) {
        this.context = context;
        this.box_list = box_list;
        this.status = status;

    }

    @Override
    public int getCount() {
        return box_list.size();
    }

    @Override
    public Object getItem(int position) {
        return box_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class ViewHolder
    {
        ImageView front;
        ImageView back;
        ViewHolder(View v){
            front = (ImageView) v.findViewById(R.id.front);
            back = (ImageView) v.findViewById(R.id.back);
        }

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder holder = null;

        if(v ==null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.box_item,parent,false);
            holder= new ViewHolder(v);
            v.setTag(holder);
        }
        else{
            holder = (ViewHolder) v.getTag();
        }

        Box temp = box_list.get(position);
        File front = new File(temp.front);
        Picasso.with(context).load(front).resize(300,300).centerCrop().into(holder.front);
        if(temp.back!=null) {
            Picasso.with(context).load(new File(temp.back)).resize(300,300).centerCrop().into(holder.back);
        }


        if(status){
            holder.front.setVisibility(View.VISIBLE);
            holder.back.setVisibility(View.GONE);
        }
        else if(!status){
            holder.front.setVisibility(View.GONE);
            holder.back.setVisibility(View.VISIBLE);
        }

        return v;
    }

}
