package com.example.makoto.simpletasklist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class EditActivity extends Activity implements LoaderManager.LoaderCallbacks {

    private boolean isNewTask = true;
    private long taskId;
    private EditText myTaskBody;
    private TextView myTaskUpdated;
    private String body = "";
    private String updated = "";
    private SimpleCursorAdapter adapter;
    private ArrayList<HashMap<String, String>> loadedLists;

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
            Uri uri = ContentUris.withAppendedId(MyContentProvider.TASKS_URI, taskId);
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
        String[] from = new String[] { MyContract.TaskLists.COLUMN_TITLE };
        int[] to = new int[] { android.R.id.text1 };
        adapter = new SimpleCursorAdapter(this, android.R.layout.select_dialog_item, null, from, to, 0);

        loadedLists = new ArrayList<HashMap<String, String>>();

        getLoaderManager().initLoader(0, null, this);
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
                AlertDialog.Builder deletionAlertDialog = new AlertDialog.Builder(this);
                deletionAlertDialog.setTitle("Delete Task");
                deletionAlertDialog.setMessage("Are you sure?");
                deletionAlertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Uri deleteUri = ContentUris.withAppendedId(MyContentProvider.TASKS_URI, taskId);
                        String selection = MyContract.Tasks.COLUMN_ID + " = ?";
                        String[] selectionArgs = new String[]{Long.toString(taskId)};
                        getContentResolver().delete(deleteUri, selection, selectionArgs);

                        Intent intent = new Intent(EditActivity.this, MyActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });
                deletionAlertDialog.create().show();
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
                        getContentResolver().insert(MyContentProvider.TASKS_URI, values);
                    } else {
                        // update
                        values.put(
                                MyContract.Tasks.COLUMN_UPDATED,
                                android.text.format.DateFormat.format(
                                        "yyyy-MM-dd kk-mm-ss",
                                        new Date()
                                ).toString()
                        );
                        Uri uri = ContentUris.withAppendedId(MyContentProvider.TASKS_URI, taskId);
                        getContentResolver().update(uri, values, MyContract.Tasks.COLUMN_ID + " = ?", new String[] { Long.toString(taskId) });
                    }
                    Intent intent = new Intent(EditActivity.this, MyActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                break;
            case R.id.action_set_as:
                AlertDialog.Builder listChoiceDialog = new AlertDialog.Builder(this);
                listChoiceDialog.setTitle("Select List");
                listChoiceDialog.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Long listId = Long.valueOf(loadedLists.get(i).get(MyContract.TaskLists.COLUMN_ID));
                        Uri uri = ContentUris.withAppendedId(MyContentProvider.TASKS_URI, taskId);
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(MyContract.Tasks.COLUMN_LIST_ID, listId);
                        String selection = MyContract.Tasks.COLUMN_ID + " = ?";
                        String[] selectionArgs = new String[] { Long.toString(taskId) };
                        getContentResolver().update(uri, contentValues, selection, selectionArgs);
                        Log.d("DEBUG", "task:" + taskId + " associated to list:" + listId);
                    }
                });
                listChoiceDialog.create().show();
                break;
            case android.R.id.home:
                break;
            default:
                throw new IllegalArgumentException("Unknown Action.");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader onCreateLoader(int i, Bundle bundle) {
        Uri uri = MyContentProvider.TASK_LISTS_URI;
        String[] projection = new String[] {
                MyContract.TaskLists.COLUMN_ID,
                MyContract.TaskLists.COLUMN_TITLE
        };
        return new CursorLoader(this, uri, projection, null, null, "updated desc");
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        Cursor c = (Cursor) data;
        while (c.moveToNext()) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(MyContract.TaskLists.COLUMN_ID, c.getString(c.getColumnIndex(MyContract.TaskLists.COLUMN_ID)));
            map.put(MyContract.TaskLists.COLUMN_TITLE, c.getString(c.getColumnIndex(MyContract.TaskLists.COLUMN_TITLE)));
            loadedLists.add(map);
        }
        // swapする前にcursorを進めるとマズイ？
        adapter.swapCursor(c);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        adapter.swapCursor(null);
    }
}
