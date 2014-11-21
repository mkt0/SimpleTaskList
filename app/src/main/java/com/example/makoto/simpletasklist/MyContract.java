package com.example.makoto.simpletasklist;

import android.provider.BaseColumns;

/**
 * Created by makoto on 2014/11/03.
 */
public class MyContract {
    public MyContract() {}

    public static abstract class Memos implements BaseColumns {
        public static final String TABLE_NAME = "memos";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_BODY = "body";
        public static final String COLUMN_CREATED = "created";
        public static final String COLUMN_UPDATED = "updated";

        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " ( " +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_BODY + " TEXT, " +
                COLUMN_CREATED + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                COLUMN_UPDATED + " DATETIME DEFAULT CURRENT_TIMESTAMP )";
        public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
        public static final String INIT_TABLE =
                "INSERT INTO memos ( title, body ) VALUES ( 'title1', 'body1'), ( 'title2', 'body2')";


    }
}
