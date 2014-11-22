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

    private boolean isNewTask = true;
    private long taskId;
    private EditText myTaskBody;
    private TextView myTaskUpdated;
    private String body = "";
    private String updated = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Intent intent = getIntent();
        taskId = intent.getLongExtra(MyActivity.EXTRA_MY_ID, 0L);

        myTaskBody = (EditText) findViewById(R.id.myMemoBody);
        myTaskUpdated = (TextView) findViewById(R.id.myMemoUpdated);

        isNewTask = taskId == 0L ? true : false;

        if (isNewTask) {
            getActionBar().setTitle("New Task");
        } else {
            getActionBar().setTitle("Edit Task");
            Uri uri = ContentUris.withAppendedId(MyContentProvider.CONTENT_URI, taskId);
            String[] projection = new String[] {
                    MyContract.Tasks.COLUMN_BODY,
                    MyContract.Tasks.COLUMN_UPDATED
            };
            String selection = MyContract.Tasks.COLUMN_ID + " = ?";
            String[] selectionArgs = new String[] { Long.toString(taskId) };
            Cursor cursor = getContentResolver().query(
                    uri,
                    projection,
                    selection,
                    selectionArgs,
                    null
            );
            while (cursor.moveToNext()) {
                body = cursor.getString(cursor.getColumnIndex(MyContract.Tasks.COLUMN_BODY));
                updated ="Updated: " + cursor.getString(cursor.getColumnIndex(MyContract.Tasks.COLUMN_UPDATED));
            }

            myTaskBody.setText(body);
            myTaskUpdated.setText(updated);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit, menu);
        if (isNewTask) {
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
                        Uri deleteUri = ContentUris.withAppendedId(MyContentProvider.CONTENT_URI, taskId);
                        String selection = MyContract.Tasks.COLUMN_ID + " = ?";
                        String[] selectionArgs = new String[] { Long.toString(taskId) };
                        getContentResolver().delete(deleteUri, selection, selectionArgs);

                        Intent intent = new Intent(EditActivity.this, MyActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });
                alertDialog.create().show();
                break;
            case R.id.action_save:
                body = myTaskBody.getText().toString().trim();
                if (body.equals("")) {
                    Toast toast = Toast.makeText(this,"Body is empty.", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    ContentValues values = new ContentValues();
                    values.put(MyContract.Tasks.COLUMN_BODY, body);
                    if (isNewTask) {
                        // insert
                        getContentResolver().insert(MyContentProvider.CONTENT_URI, values);
                    } else {
                        // update
                        values.put(
                                MyContract.Tasks.COLUMN_UPDATED,
                                android.text.format.DateFormat.format(
                                        "yyyy-MM-dd kk-mm-ss",
                                        new Date()
                                ).toString()
                        );
                        Uri uri = ContentUris.withAppendedId(MyContentProvider.CONTENT_URI, taskId);
                        getContentResolver().update(uri, values, MyContract.Tasks.COLUMN_ID + " = ?", new String[] { Long.toString(taskId) });
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
