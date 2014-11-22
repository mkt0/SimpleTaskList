package com.example.makoto.simpletasklist;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;


public class ListEditActivity extends Activity {

    private long listId;
    private boolean isNewList;
    private EditText myListTitle;
    private TextView myListUpdated;
    private String listTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_edit);

        Intent intent = getIntent();
        listId = intent.getLongExtra(ListsActivity.EXTRA_LIST_ID, 0L);

        myListTitle = (EditText) findViewById(R.id.listTitle);
        myListUpdated = (TextView) findViewById(R.id.listUpdated);

        isNewList = listId == 0L ? true : false;

        if (isNewList) {
            getActionBar().setTitle("New List");
        } else {
            getActionBar().setTitle("Edit List");
//            Uri uri = ContentUris.withAppendedId(MyContentProvider.CONTENT_URI, taskId);
//            String[] projection = new String[] {
//                    MyContract.Tasks.COLUMN_BODY,
//                    MyContract.Tasks.COLUMN_UPDATED
//            };
//            String selection = MyContract.Tasks.COLUMN_ID + " = ?";
//            String[] selectionArgs = new String[] { Long.toString(taskId) };
//            Cursor cursor = getContentResolver().query(
//                    uri,
//                    projection,
//                    selection,
//                    selectionArgs,
//                    null
//            );
//            while (cursor.moveToNext()) {
//                body = cursor.getString(cursor.getColumnIndex(MyContract.Tasks.COLUMN_BODY));
//                updated ="Updated: " + cursor.getString(cursor.getColumnIndex(MyContract.Tasks.COLUMN_UPDATED));
//            }
//
//            myTaskBody.setText(body);
//            myTaskUpdated.setText(updated);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list_edit, menu);
        if (isNewList) {
            menu.getItem(0).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Toast toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        switch (item.getItemId()) {
            case R.id.action_save_list:
                listTitle = myListTitle.getText().toString().trim();
                if (listTitle.equals("")) {
                    toast.setText("Title is empty.");
                    toast.show();
                } else {
//                    ContentValues values = new ContentValues();
//                    values.put(MyContract.TaskLists.COLUMN_TITLE, listTitle);
//                    if (isNewList) {
//                        // insert
//                        getContentResolver().insert(MyContentProvider.TASK_LISTS_URI, values);
//                    } else {
//                        // update
//                        values.put(
//                                MyContract.TaskLists.COLUMN_UPDATED,
//                                android.text.format.DateFormat.format(
//                                        "yyyy-MM-dd kk-mm-ss",
//                                        new Date()
//                                ).toString()
//                        );
//                        Uri uri = ContentUris.withAppendedId(MyContentProvider.TASK_LISTS_URI, listId);
//                        getContentResolver().update(uri, values, MyContract.TaskLists.COLUMN_ID + " = ?", new String[]{Long.toString(listId)});
//                    }
//                    Intent intent = new Intent(ListEditActivity.this, ListsActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    startActivity(intent);
                }
                break;
            case R.id.action_delete_list:
                toast.setText("delete.");
                toast.show();
                break;
            default:
                toast.setText("Unknown!");
                toast.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}