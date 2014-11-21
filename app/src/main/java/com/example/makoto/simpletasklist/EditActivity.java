package com.example.makoto.simpletasklist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;


public class EditActivity extends Activity {

    private boolean isNewMemo = true;
    private long memoId;
    private EditText myMemoTitle;
    private EditText myMemoBody;
    private TextView myMemoUpdated;
    private String title = "";
    private String body = "";
    private String updated = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Intent intent = getIntent();
        memoId = intent.getLongExtra(MyActivity.EXTRA_MY_ID, 0L);

        myMemoTitle = (EditText) findViewById(R.id.myMemoTitle);
        myMemoBody = (EditText) findViewById(R.id.myMemoBody);
        myMemoUpdated = (TextView) findViewById(R.id.myMemoUpdated);

        isNewMemo = memoId == 0L ? true : false;

        if (isNewMemo) {
            getActionBar().setTitle("New Memo");
        } else {
            getActionBar().setTitle("Edit Memo");
            Uri uri = ContentUris.withAppendedId(MyContentProvider.CONTENT_URI, memoId);
            String[] projection = new String[] {
                    MyContract.Memos.COLUMN_TITLE,
                    MyContract.Memos.COLUMN_BODY,
                    MyContract.Memos.COLUMN_UPDATED
            };
            String selection = MyContract.Memos.COLUMN_ID + " = ?";
            String[] selectionArgs = new String[] { Long.toString(memoId) };
            Cursor cursor = getContentResolver().query(
                    uri,
                    projection,
                    selection,
                    selectionArgs,
                    null
            );
            while (cursor.moveToNext()) {
                title = cursor.getString(cursor.getColumnIndex(MyContract.Memos.COLUMN_TITLE));
                body = cursor.getString(cursor.getColumnIndex(MyContract.Memos.COLUMN_BODY));
                updated ="Updated: " + cursor.getString(cursor.getColumnIndex(MyContract.Memos.COLUMN_UPDATED));
            }

            myMemoTitle.setText(title);
            myMemoBody.setText(body);
            myMemoUpdated.setText(updated);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit, menu);
        if (isNewMemo) {
            menu.getItem(0).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_delete:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Delete Memo");
                alertDialog.setMessage("Are you sure?");
                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Uri deleteUri = ContentUris.withAppendedId(MyContentProvider.CONTENT_URI, memoId);
                        String selection = MyContract.Memos.COLUMN_ID + " = ?";
                        String[] selectionArgs = new String[] { Long.toString(memoId) };
                        getContentResolver().delete(deleteUri, selection, selectionArgs);

                        Intent intent = new Intent(EditActivity.this, MyActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });
                alertDialog.create().show();
                break;
            case R.id.action_save:
                title = myMemoTitle.getText().toString().trim();
                body = myMemoBody.getText().toString().trim();
                if (title.equals("")) {
                    Toast toast = Toast.makeText(this,"Title is empty.", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    ContentValues values = new ContentValues();
                    values.put(MyContract.Memos.COLUMN_TITLE, title);
                    values.put(MyContract.Memos.COLUMN_BODY, body);
                    if (isNewMemo) {
                        // insert
                        getContentResolver().insert(MyContentProvider.CONTENT_URI, values);
                    } else {
                        // update
                        values.put(
                                MyContract.Memos.COLUMN_UPDATED,
                                android.text.format.DateFormat.format(
                                        "yyyy-MM-dd kk-mm-ss",
                                        new Date()
                                ).toString()
                        );
                        Uri uri = ContentUris.withAppendedId(MyContentProvider.CONTENT_URI, memoId);
                        getContentResolver().update(uri, values, MyContract.Memos.COLUMN_ID + " = ?", new String[] { Long.toString(memoId) });
                    }
                    Intent intent = new Intent(EditActivity.this, MyActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
