package com.example.makoto.simpletasklist;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Date;


public class ListEditActivity extends Activity implements MyAlertDialogFragment.MyAlertDialogFragmentCallbacks {

    private static final String ALERT_DIALOG_FRAGMENT_TAG = "alertDialog";

    private long listId;
    private boolean isNewList;
    private EditText myListTitle;
    private TextView myListUpdated;
    private String listTitle;
    private String listUpdated;

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
            getActionBar().setTitle(R.string.title_activity_edit_list_new);
        } else {
            getActionBar().setTitle(R.string.title_activity_edit_list);
            Uri uri = ContentUris.withAppendedId(MyContentProvider.TASK_LISTS_URI, listId);
            String[] projection = new String[] {
                    MyContract.TaskLists.COLUMN_TITLE,
                    MyContract.TaskLists.COLUMN_UPDATED
            };
            String selection = MyContract.TaskLists.COLUMN_ID + " = ?";
            String[] selectionArgs = new String[] { Long.toString(listId) };
            Cursor cursor = getContentResolver().query(
                    uri,
                    projection,
                    selection,
                    selectionArgs,
                    null
            );
            while (cursor.moveToNext()) {
                listTitle = cursor.getString(cursor.getColumnIndex(MyContract.TaskLists.COLUMN_TITLE));
                listUpdated ="Updated: " + cursor.getString(cursor.getColumnIndex(MyContract.TaskLists.COLUMN_UPDATED));
            }

            myListTitle.setText(listTitle);
            myListUpdated.setText(listUpdated);
        }

        // show keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        myListTitle.requestFocus();

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
                    ContentValues values = new ContentValues();
                    values.put(MyContract.TaskLists.COLUMN_TITLE, listTitle);
                    if (isNewList) {
                        // insert
                        getContentResolver().insert(MyContentProvider.TASK_LISTS_URI, values);
                    } else {
                        // update
                        values.put(
                                MyContract.TaskLists.COLUMN_UPDATED,
                                android.text.format.DateFormat.format(
                                        "yyyy-MM-dd kk-mm-ss",
                                        new Date()
                                ).toString()
                        );
                        Uri uri = ContentUris.withAppendedId(MyContentProvider.TASK_LISTS_URI, listId);
                        getContentResolver().update(uri, values, MyContract.TaskLists.COLUMN_ID + " = ?", new String[]{Long.toString(listId)});
                    }
                    Intent intent = new Intent(ListEditActivity.this, ListsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                break;
            case R.id.action_delete_list:
                if (listId == 1) {
                    toast.setText(R.string.toast_unable_to_remove_default_list);
                    toast.show();
                    break;
                }
                DialogFragment dialogFragment = MyAlertDialogFragment.newInstance(R.string.delete_list_alert_dialog_title);
                dialogFragment.show(getFragmentManager(), ALERT_DIALOG_FRAGMENT_TAG);
                break;
            case android.R.id.home:
                break;
            default:
                throw new IllegalArgumentException("Unknown Action!");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMyAlertDialogPositiveClick() {
        Log.i(ALERT_DIALOG_FRAGMENT_TAG, "Positive click!");

        // delete tasks associated with the list.
        Uri deleteUri = MyContentProvider.TASKS_URI;
        String selection = MyContract.Tasks.COLUMN_LIST_ID + " = ?";
        String[] selectionArgs = new String[] { Long.toString(listId) };
        getContentResolver().delete(deleteUri, selection, selectionArgs);

        // delete the list.
        deleteUri = ContentUris.withAppendedId(MyContentProvider.TASK_LISTS_URI, listId);
        selection = MyContract.TaskLists.COLUMN_ID + " = ?";
        selectionArgs = new String[] { Long.toString(listId) };
        getContentResolver().delete(deleteUri, selection, selectionArgs);

        Intent intent = new Intent(ListEditActivity.this, ListsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onMyAlertDialogNegativeClick() {
        Log.i(ALERT_DIALOG_FRAGMENT_TAG, "Negative click!");
    }
}
