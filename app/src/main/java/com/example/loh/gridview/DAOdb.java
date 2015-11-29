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
}
