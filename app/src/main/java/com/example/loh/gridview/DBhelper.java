package com.example.loh.gridview;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Kelok on 28/11/2015.
 */
public class DBhelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "Database.db";
    public static final int DB_VERSION = 1;

    public static final String COMMA_SEP = ",";
    public static final String TEXT_TYPE = " TEXT";
    public static final String INT_TYPE = " INTEGER";

    public static final String TABLE_NAME = "box";
    public static final String COLUMN_FRONT = "front";
    public static final String COLUMN_BACK = "back";

    public static final String TABLE_NAME_2 = "wheel";
    public static final String COLUMN_PHOTO = "photo";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_COLOR = "color";

    private static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            COLUMN_FRONT + TEXT_TYPE + COMMA_SEP +
            COLUMN_BACK + TEXT_TYPE +
            " )";

    private static final String DELETE_TABLE_2 = "DROP TABLE IF EXISTS " + TABLE_NAME_2;
    private static final String CREATE_TABLE_2 = "CREATE TABLE " + TABLE_NAME_2 + " (" +
            COLUMN_COLOR + INT_TYPE + COMMA_SEP +
            COLUMN_TITLE + TEXT_TYPE + COMMA_SEP +
            COLUMN_PHOTO + TEXT_TYPE +
            " )";

    public DBhelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        db.execSQL(CREATE_TABLE_2);
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DELETE_TABLE);
        db.execSQL(DELETE_TABLE_2);
        onCreate(db);
    }
}
