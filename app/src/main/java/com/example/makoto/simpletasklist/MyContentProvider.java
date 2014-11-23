package com.example.makoto.simpletasklist;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by makoto on 2014/11/04.
 */
public class MyContentProvider extends ContentProvider {
    private static final String AUTHORITY = "com.example.makoto.simpletasklist.mycontentprovider";
    private MyDbHelper myDbHelper;

    public static final Uri TASKS_URI = Uri.parse("content://" + AUTHORITY + "/" + MyContract.Tasks.TABLE_NAME);
    public static final Uri TASK_LISTS_URI = Uri.parse("content://" + AUTHORITY + "/" + MyContract.TaskLists.TABLE_NAME);

    private static final int TASKS = 1;
    private static final int TASK_ITEM = 2;
    private static final int LISTS = 3;
    private static final int LIST_ITEM = 4;


    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, MyContract.Tasks.TABLE_NAME, TASKS);
        uriMatcher.addURI(AUTHORITY, MyContract.Tasks.TABLE_NAME + "/#", TASK_ITEM);
        uriMatcher.addURI(AUTHORITY, MyContract.TaskLists.TABLE_NAME, LISTS);
        uriMatcher.addURI(AUTHORITY, MyContract.TaskLists.TABLE_NAME + "/#", LIST_ITEM);
    }

    @Override
    public boolean onCreate() {
        myDbHelper = new MyDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;

        switch (uriMatcher.match(uri)) {
            case TASKS:
            case TASK_ITEM:
                cursor = myDbHelper.getReadableDatabase().query(
                        MyContract.Tasks.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case LISTS:
            case LIST_ITEM:
                cursor = myDbHelper.getReadableDatabase().query(
                        MyContract.TaskLists.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        Uri newUri;
        long insertedId;

        switch (uriMatcher.match(uri)) {
            case TASKS:
                insertedId = myDbHelper.getWritableDatabase().insert(
                        MyContract.Tasks.TABLE_NAME,
                        null,
                        contentValues
                );
                newUri = ContentUris.withAppendedId(MyContentProvider.TASKS_URI, insertedId);
                break;
            case LISTS:
                insertedId = myDbHelper.getWritableDatabase().insert(
                        MyContract.TaskLists.TABLE_NAME,
                        null,
                        contentValues
                );
                newUri = ContentUris.withAppendedId(MyContentProvider.TASK_LISTS_URI, insertedId);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return newUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count;

        switch (uriMatcher.match(uri)) {
            case TASK_ITEM:
                count = myDbHelper.getWritableDatabase().delete(
                        MyContract.Tasks.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            case LIST_ITEM:
                count = myDbHelper.getWritableDatabase().delete(
                        MyContract.TaskLists.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        int count;

        switch (uriMatcher.match(uri)) {
            case TASK_ITEM:
                count = myDbHelper.getWritableDatabase().update(
                        MyContract.Tasks.TABLE_NAME,
                        contentValues,
                        selection,
                        selectionArgs
                );
                break;
            case LIST_ITEM:
                count = myDbHelper.getWritableDatabase().update(
                        MyContract.TaskLists.TABLE_NAME,
                        contentValues,
                        selection,
                        selectionArgs
                );
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
