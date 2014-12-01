package com.example.makoto.simpletasklist;

import android.provider.BaseColumns;

/**
 * Created by makoto on 2014/11/03.
 */
public class MyContract {
    public MyContract() {}

    public static abstract class Tasks implements BaseColumns {
        public static final String TABLE_NAME = "tasks";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_BODY = "body";
        public static final String COLUMN_CREATED = "created";
        public static final String COLUMN_UPDATED = "updated";
        public static final String COLUMN_LIST_ID = "list_id";

        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " ( " +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_BODY + " TEXT, " +
                COLUMN_CREATED + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                COLUMN_UPDATED + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                COLUMN_LIST_ID + " INTEGER DEFAULT 1 )";
        public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
        public static final String INIT_TABLE =
                "INSERT INTO " + TABLE_NAME +
                " ( " + COLUMN_BODY +
                " ) VALUES ( 'task1' ), ( 'task2' ), ( 'task3' )";


    }

    public static abstract class TaskLists implements BaseColumns {
        public static final String TABLE_NAME = "task_lists";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_CREATED = "created";
        public static final String COLUMN_UPDATED = "updated";
        public static final String COLUMN_ORDER = "order_rank";

        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " ( " +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_TITLE + " TEXT, " +
                        COLUMN_CREATED + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        COLUMN_UPDATED + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        COLUMN_ORDER + " INTEGER DEFAULT 100 )";
        public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
        public static final String INIT_TABLE =
                "INSERT INTO " + TABLE_NAME +
                        " ( " + COLUMN_TITLE + ", " + COLUMN_ORDER +
                        " ) VALUES ( 'Inbox', 0 ), ( 'Trash', 1000 ), ( 'Finished', 100 )";


    }
}
