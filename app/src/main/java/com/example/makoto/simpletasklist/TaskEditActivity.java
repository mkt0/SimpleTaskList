package com.example.makoto.simpletasklist;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;


public class TaskEditActivity extends Activity implements
        MyAlertDialogFragment.MyAlertDialogFragmentCallbacks,
        ListSelectionDialogFragment.ListSelectionDialogCallbacks {

    private static final String DELETION_ALERT_DIALOG = "deletionAlertDialog";
    private static final String LIST_SELECTION_DIALOG = "listSelectionDialog";

    private boolean isNewTask = true;
    private long taskId;
    private EditText myTaskBody;
    private TextView myTaskUpdated;
    private String body = "";
    private String updated = "";
    private long listId;
    private int listPosition;
    private Bundle queryArgs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Intent intent = getIntent();
        taskId = intent.getLongExtra(TasksActivity.EXTRA_TASK_ID, 0L);
        listId = intent.getLongExtra(TasksActivity.EXTRA_LIST_ID, 1L);
        listPosition = intent.getIntExtra(TasksActivity.EXTRA_LIST_POSITION, 0);
        Log.d("debug", "Create TaskEditActivity with listId: " + listId + ", listPos: " + listPosition);

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

        queryArgs = new Bundle();

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
                MyAlertDialogFragment deleteAlertDialog = MyAlertDialogFragment.newInstance(R.string.delete_list_alert_dialog_title);
                deleteAlertDialog.show(getFragmentManager(), DELETION_ALERT_DIALOG);
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
                        // associate with current list.
                        values.put(MyContract.Tasks.COLUMN_LIST_ID, listId);
                        Log.d("debug", "new task associated with listId: " + listId);
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
                    
                    Intent intent = new Intent(TaskEditActivity.this, TasksActivity.class);
                    intent.putExtra(TasksActivity.EXTRA_LIST_POSITION, listPosition);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    Log.d("debug", "Start TasksActivity bundles listPos: " + listPosition);
                    startActivity(intent);
                }
                break;
            case R.id.action_associate:
                Uri uri = MyContentProvider.TASK_LISTS_URI;
                String[] projection = new String[] {
                        MyContract.TaskLists.COLUMN_ID,
                        MyContract.TaskLists.COLUMN_TITLE
                };
                String selection = null;
                String[] selectionArgs = null;
                String sortOrder = "updated desc";

                // setup bundle for query in dialogFragment.
                queryArgs.putParcelable(ListSelectionDialogFragment.QUERY_URI, uri);
                queryArgs.putStringArray(ListSelectionDialogFragment.QUERY_PROJECTION, projection);
                queryArgs.putString(ListSelectionDialogFragment.QUERY_SELECTION, selection);
                queryArgs.putStringArray(ListSelectionDialogFragment.QUERY_SELECTION_ARGS, selectionArgs);
                queryArgs.putString(ListSelectionDialogFragment.QUERY_SORT_ORDER, sortOrder);

                ListSelectionDialogFragment dialogFragment = ListSelectionDialogFragment.newInstance(
                        R.string.select_list_dialog_title,
                        queryArgs
                );
                dialogFragment.show(getFragmentManager(), LIST_SELECTION_DIALOG);
                break;
            case android.R.id.home:
                break;
            default:
                throw new IllegalArgumentException("Unknown Action.");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPositiveClick() {
        Uri deleteUri = ContentUris.withAppendedId(MyContentProvider.TASKS_URI, taskId);
        String selection = MyContract.Tasks.COLUMN_ID + " = ?";
        String[] selectionArgs = new String[]{Long.toString(taskId)};
        getContentResolver().delete(deleteUri, selection, selectionArgs);

        Intent intent = new Intent(TaskEditActivity.this, TasksActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onNegativeClick() { }

    @Override
    public void onListSelectionDialogItemClick(int position, int id, String title) {
        int newListId = id;
        Uri uri = ContentUris.withAppendedId(MyContentProvider.TASKS_URI, taskId);
        ContentValues contentValues = new ContentValues();
        contentValues.put(MyContract.Tasks.COLUMN_LIST_ID, newListId);
        String selection = MyContract.Tasks.COLUMN_ID + " = ?";
        String[] selectionArgs = new String[] { Long.toString(taskId) };
        getContentResolver().update(uri, contentValues, selection, selectionArgs);
        Log.d("DEBUG", "task:" + taskId + " associated to list:" + newListId);
    }
}
