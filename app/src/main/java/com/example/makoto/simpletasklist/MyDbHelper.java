package com.example.makoto.simpletasklist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by makoto on 2014/11/04.
 */
public class MyDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "simple_task_list.db";
    public static final int DB_VERSION = 3;

    public MyDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(MyContract.Tasks.CREATE_TABLE);
        sqLiteDatabase.execSQL(MyContract.Tasks.INIT_TABLE);
        sqLiteDatabase.execSQL(MyContract.TaskLists.CREATE_TABLE);
        sqLiteDatabase.execSQL(MyContract.TaskLists.INIT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL(MyContract.Tasks.DROP_TABLE);
        sqLiteDatabase.execSQL(MyContract.TaskLists.DROP_TABLE);
        onCreate(sqLiteDatabase);
    }
}
