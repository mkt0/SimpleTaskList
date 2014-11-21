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

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + MyContract.Memos.TABLE_NAME);
    private static final int MEMOS = 1;
    private static final int MEMO_ITEM = 2;

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY,MyContract.Memos.TABLE_NAME, MEMOS);
        uriMatcher.addURI(AUTHORITY,MyContract.Memos.TABLE_NAME + "/#", MEMO_ITEM);
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
            case MEMOS:
            case MEMO_ITEM:
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }
        cursor = myDbHelper.getReadableDatabase().query(
                MyContract.Memos.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        if (uriMatcher.match(uri) != MEMOS) {
            throw new IllegalArgumentException("Unknown URI");
        }

        SQLiteDatabase db =  myDbHelper.getWritableDatabase();
        long insertedId = db.insert(
                MyContract.Memos.TABLE_NAME,
                null,
                contentValues
        );
        Uri newUri = ContentUris.withAppendedId(MyContentProvider.CONTENT_URI,insertedId);
        getContext().getContentResolver().notifyChange(uri, null);
        return newUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (uriMatcher.match(uri) != MEMO_ITEM) {
            throw new IllegalArgumentException("Unknown URI");
        }

        SQLiteDatabase db =  myDbHelper.getWritableDatabase();
        int count = db.delete(
                MyContract.Memos.TABLE_NAME,
                selection,
                selectionArgs
        );
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        if (uriMatcher.match(uri) != MEMO_ITEM) {
            throw new IllegalArgumentException("Unknown URI");
        }
        SQLiteDatabase db =  myDbHelper.getWritableDatabase();
        int count = db.update(
                MyContract.Memos.TABLE_NAME,
                contentValues,
                selection,
                selectionArgs
        );
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
