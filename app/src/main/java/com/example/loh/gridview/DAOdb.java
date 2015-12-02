package com.example.loh.gridview;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kelok on 28/11/2015.
 */
public class DAOdb {

    private SQLiteDatabase database;
    private DBhelper dbHelper;

    public DAOdb(Context context) {
        dbHelper = new DBhelper(context);
        database = dbHelper.getWritableDatabase();
    }

    /**
     * close any database object
     */
    public void close() {
        dbHelper.close();
    }

    /**
     * insert a text report item to the location database table
     *
     * @param image
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public long addBoxImage(Box image) {
        ContentValues cv = new ContentValues();
        cv.put(DBhelper.COLUMN_FRONT, image.getFront());
        cv.put(DBhelper.COLUMN_BACK, image.getBack());
        return database.insert(DBhelper.TABLE_NAME, null, cv);
    }

    public long addBoxBack(String image){
        ContentValues cv = new ContentValues();
        cv.put(DBhelper.COLUMN_BACK, image);
        return database.update(DBhelper.TABLE_NAME, cv, null, null);
    }

    public void addDivisionItems(List<DivisionItem> divisionItemList){
        for(DivisionItem item : divisionItemList) {
            ContentValues cv = new ContentValues();
            cv.put(DBhelper.COLUMN_COLOR, item.getColor());
            cv.put(DBhelper.COLUMN_TITLE, item.getTitle());
            cv.put(DBhelper.COLUMN_PHOTO, item.getPicturePath());
            database.insert(DBhelper.TABLE_NAME_2, null, cv);
        }
    }


    /**
     * delete the given image from database
     *
     * @param image
     */
    public void deleteBoxImage(Box image) {
        String whereClause =
                DBhelper.COLUMN_FRONT + "=? AND " + DBhelper.COLUMN_BACK +
                        "=?";
        String[] whereArgs = new String[]{image.getFront(),
                String.valueOf(image.getBack())};
        database.delete(DBhelper.TABLE_NAME, whereClause, whereArgs);
    }

    public void deleteAllBoxes(){
        database.delete(DBhelper.TABLE_NAME,null,null);
    }

    public void deleteAllDivisionItems(){ database.delete(DBhelper.TABLE_NAME_2,null,null);}



    /**
     * @return all image as a List
     */
    public List<Box> getBoxImages() {
        List<Box> MyImages = new ArrayList<>();
        Cursor cursor =
                database.query(DBhelper.TABLE_NAME, null, null, null, null,
                        null, null + " DESC");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Box MyImage = cursorToMyImage(cursor);
            MyImages.add(MyImage);
            cursor.moveToNext();
        }
        cursor.close();
        return MyImages;
    }

    public List<DivisionItem> getDivisionItems(){
        List<DivisionItem> divisionItemList = new ArrayList<>();
        Cursor cursor =
                database.query(DBhelper.TABLE_NAME_2, null, null, null, null,
                        null, null + " DESC");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            DivisionItem MyImage = cursorToDivisionItem(cursor);
            divisionItemList.add(MyImage);
            cursor.moveToNext();
        }
        cursor.close();
        return divisionItemList;
    }

    /**
     * read the cursor row and convert the row to a MyImage object
     *
     * @param cursor
     * @return MyImage object
     */
    private Box cursorToMyImage(Cursor cursor) {
        Box image = new Box();
        image.setFront(
                cursor.getString(cursor.getColumnIndex(DBhelper.COLUMN_FRONT)));
        image.setBack(
                cursor.getString(cursor.getColumnIndex(DBhelper.COLUMN_BACK)));
        return image;
    }

    private DivisionItem cursorToDivisionItem(Cursor cursor){
        DivisionItem divisionItem = new DivisionItem();
        divisionItem.setColor(cursor.getInt(cursor.getColumnIndex(DBhelper.COLUMN_COLOR)));
        divisionItem.setTitle(cursor.getString(cursor.getColumnIndex(DBhelper.COLUMN_TITLE)));
        divisionItem.setPicturePath(cursor.getString(cursor.getColumnIndexOrThrow(DBhelper.COLUMN_PHOTO)));
        return divisionItem;
    }
}
