package com.example.makoto.simpletasklist;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SpinnerAdapter;



public class TasksActivity extends Activity implements
        LoaderManager.LoaderCallbacks,
        ListSelectionDialogFragment.ListSelectionDialogCallbacks,
        MyAlertDialogFragment.MyAlertDialogFragmentCallbacks {

    public static final String EXTRA_TASK_ID = "com.example.makoto.simpletasklist.EXTRA_TASK_ID";
    public static final String EXTRA_LIST_ID = "com.example.makoto.simpletasklist.EXTRA_LIST_ID";
    public static final String EXTRA_LIST_POSITION = "com.example.makoto.simpletasklist.EXTRA_LIST_POSITION";
    public static final String DELETE_ALL_DIALOG_FRAGMENT = "deleteAllDialogFragment";
    private static final int TASK_LOADER_ID = 0;
    private static final int LIST_LOADER_ID = 1;
    private static final String BUNDLE_TASK_SELECTION = "list_selection";
    private static final String BUNDLE_TASK_SELECTION_ARGS = "list_selection_args";
    private static final String LIST_SELECTION_DIALOG_TAG = "list_selection_dialog_tag";
    private static final String INSTANCE_STATE_NAVIGATION_ITEM_POSITION = "navigationItemPosition";
    private static final String INSTANCE_STATE_NAVIGATION_ITEM_ID = "navigationItemId";

    private SimpleCursorAdapter taskListAdapter;
    private SpinnerAdapter spinnerAdapter;
    private long currentListId;
    private int currentListPosition;
    private int receivedListPosition;
    private boolean synthetic;
    private long longClickedTaskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        Intent intent = getIntent();
        receivedListPosition = intent.getIntExtra(EXTRA_LIST_POSITION, -1);
        Log.d("app", "Nav item Position received from intent: " + receivedListPosition);

        if (receivedListPosition == -1) {
            if (savedInstanceState != null) {
                receivedListPosition = savedInstanceState.getInt(INSTANCE_STATE_NAVIGATION_ITEM_POSITION);
                Log.d("app", "Nav item Position received from instance state: " + receivedListPosition);
            } else {
                receivedListPosition = 0;
                Log.d("app", "Nav item Position is default: " + receivedListPosition);
            }
        }

        // TODO: ナビゲーションリストの一番下に、新規リスト追加ボタン、リスト編集ボタンを設置する。
        spinnerAdapter = new ListsActivity.ListItemCursorAdapter(
                getActionBar().getThemedContext(),
                R.layout.navigation_dropdown_row,
                null,
                0
        );

        synthetic = true;

        // implement OnNavigationListener callback
        ActionBar.OnNavigationListener onNavigationListener = new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int position, long itemId) {
                Log.d("app", "fired onNavigationItemSelected");
                if (synthetic) {
                    synthetic = false;
                    Log.d("app", "synthetic NavigationItem selection is detected.");
                    getActionBar().setSelectedNavigationItem(receivedListPosition);
                    Log.d("app", "select NavigationItem(pos=" + receivedListPosition + ")");
                    currentListPosition = receivedListPosition;
                    currentListId = itemId;
                    Log.d("app", "set current listId: " + currentListId + ", pos: " + currentListPosition);
                    return true;
                }
                if (currentListId != itemId) {
                    Bundle bundle = new Bundle();
                    bundle.putString(BUNDLE_TASK_SELECTION, MyContract.Tasks.COLUMN_LIST_ID + " = ?");
                    bundle.putString(BUNDLE_TASK_SELECTION_ARGS, Long.toString(itemId));
                    getLoaderManager().restartLoader(TASK_LOADER_ID, bundle, TasksActivity.this);
                    currentListId = itemId;
                    Log.d("app", "set current listId: " + currentListId);
                    currentListPosition = position;
                    Log.d("app", "set current listPosition: " + currentListPosition);
                }
                return true;
            }
        };

        // enable Actionbar dropdown list
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setListNavigationCallbacks(spinnerAdapter, onNavigationListener);


        // set taskListAdapter to taskListView
        String[] taskFrom = new String[] { MyContract.Tasks.COLUMN_BODY };
        int[] taskTo = new int[] { android.R.id.text1 };
        taskListAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null, taskFrom, taskTo, 0);
        ListView taskListView = (ListView) findViewById(R.id.taskListView);
        taskListView.setAdapter(taskListAdapter);

        // set Listeners to taskListView
        taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(TasksActivity.this, TaskEditActivity.class);
                intent.putExtra(EXTRA_TASK_ID, l);
                intent.putExtra(EXTRA_LIST_ID, currentListId);
                intent.putExtra(EXTRA_LIST_POSITION, currentListPosition);
                Log.d("app", "Start TaskEditActivity bundles taskId: " + l + ", listId: " + currentListId + ", listPos: " + currentListPosition);
                startActivity(intent);
            }
        });
        taskListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                longClickedTaskId = l;
                ListSelectionDialogFragment listDialog = ListSelectionDialogFragment.newInstance(R.string.select_list_dialog_title, currentListId);
                listDialog.show(getFragmentManager(), LIST_SELECTION_DIALOG_TAG);
                return true;
            }
        });

        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_TASK_SELECTION, MyContract.Tasks.COLUMN_LIST_ID + " = ?");
        bundle.putString(BUNDLE_TASK_SELECTION_ARGS, "1");
        getLoaderManager().initLoader(TASK_LOADER_ID, bundle, this);
        getLoaderManager().initLoader(LIST_LOADER_ID, null, this);
    }

    @Override
    protected  void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(INSTANCE_STATE_NAVIGATION_ITEM_POSITION, currentListPosition);
        outState.putLong(INSTANCE_STATE_NAVIGATION_ITEM_ID, currentListId);
        Log.d("app", "saved instance state; pos=" + currentListPosition + ", id=" + currentListId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_add:
                intent = new Intent(this, TaskEditActivity.class);
                intent.putExtra(EXTRA_LIST_ID, currentListId);
                intent.putExtra(EXTRA_LIST_POSITION, currentListPosition);
                startActivity(intent);
                return true;
            case R.id.action_agenda:
                intent = new Intent(this, ListsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_delete_all_tasks:
                Log.d("app", "clicked delete all button.");
                MyAlertDialogFragment deleteAllAlertDialog = MyAlertDialogFragment.newInstance(R.string.delete_all_alert_dialog_title);
                deleteAllAlertDialog.show(getFragmentManager(), DELETE_ALL_DIALOG_FRAGMENT);
            case R.id.action_test:
                getActionBar().setSelectedNavigationItem(1);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader onCreateLoader(int loaderId, Bundle bundle) {
        String[] projection;
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;

        switch (loaderId) {
            case TASK_LOADER_ID:
                projection = new String[] {
                        MyContract.Tasks.COLUMN_ID,
                        MyContract.Tasks.COLUMN_BODY
                };
                if ( bundle != null ) {
                    selection = bundle.getString(BUNDLE_TASK_SELECTION);
                    selectionArgs = new String[] { bundle.getString(BUNDLE_TASK_SELECTION_ARGS) };
                }

                return new CursorLoader(
                        this,
                        MyContentProvider.TASKS_URI,
                        projection,
                        selection,
                        selectionArgs,
                        sortOrder
                );
            case LIST_LOADER_ID:
                projection = new String[] {
                        MyContract.TaskLists.COLUMN_ID,
                        MyContract.TaskLists.COLUMN_TITLE
                };
                return new CursorLoader(
                        this,
                        MyContentProvider.TASK_LISTS_URI,
                        projection,
                        null,
                        null,
                        sortOrder
                );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader loader, Object cursor) {
        switch (loader.getId()) {
            case TASK_LOADER_ID:
                taskListAdapter.swapCursor((Cursor) cursor);
                break;
            case LIST_LOADER_ID:
                Cursor c = (Cursor) cursor;
                ((CursorAdapter) spinnerAdapter).swapCursor(c);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        switch (loader.getId()) {
            case TASK_LOADER_ID:
                taskListAdapter.swapCursor(null);
                break;
            case LIST_LOADER_ID:
                ((CursorAdapter) spinnerAdapter).swapCursor(null);
                break;
        }
    }

    @Override
    public void onListSelectionDialogItemClick(int position, int id, String title) {
        int newListId = id;
        Uri uri = ContentUris.withAppendedId(MyContentProvider.TASKS_URI, longClickedTaskId);
        ContentValues contentValues = new ContentValues();
        contentValues.put(MyContract.Tasks.COLUMN_LIST_ID, newListId);
        String selection = MyContract.Tasks.COLUMN_ID + " = ?";
        String[] selectionArgs = new String[] { Long.toString(longClickedTaskId) };
        getContentResolver().update(uri, contentValues, selection, selectionArgs);
        Log.d("app", "task:" + longClickedTaskId + " associated to list:" + newListId);

        // TODO: update count label of current navigation item.
    }

    @Override
    public void onMyAlertDialogPositiveClick() {
        Log.d("app", "clicked dialog positive button.");
        getContentResolver().delete(
                MyContentProvider.TASKS_URI,
                MyContract.Tasks.COLUMN_LIST_ID + " = ?",
                new String[] { String.valueOf(currentListId) }
        );
    }

    @Override
    public void onMyAlertDialogNegativeClick() {
        Log.d("app", "clicked dialog negative button.");
    }
}
