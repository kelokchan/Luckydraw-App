package com.example.loh.gridview;


/**
 * Created by Loh on 25/11/2015.
 */
public class Box {

    String front;
    String back;

    public Box(){

    }

    public Box(String front, String back) {
        this.front = front;
        this.back = back;
    }

    public String getBack() {
        return back;
    }

    public void setBack(String back) {
        this.back = back;
    }

    public String getFront() {
        return front;
    }

    public void setFront(String front) {
        this.front = front;
    }
}
