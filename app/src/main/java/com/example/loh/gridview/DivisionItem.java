package com.example.loh.gridview;

import java.io.Serializable;

/**
 * Created by User on 11/25/2015.
 */
public class DivisionItem implements Serializable{
    private int color;
    private String title;
    private String picturePath;

    public DivisionItem() {
    }

    public DivisionItem(int color, String title, String picturePath) {
        this.color = color;
        this.title = title;
        this.picturePath = picturePath;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPicturePath() {
        return picturePath;
    }

    public void setPicturePath(String picturePath) {
        this.picturePath = picturePath;
    }
}
