package com.example.makoto.simpletasklist;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SpinnerAdapter;

import java.util.ArrayList;
import java.util.HashMap;


public class TasksActivity extends Activity implements LoaderManager.LoaderCallbacks {

    public static final String EXTRA_MY_ID = "com.example.makoto.simpletasklist.EXTRA_MY_ID";
    private static final int TASK_LOADER_ID = 0;
    private static final int LIST_LOADER_ID = 1;
    private static final String BUNDLE_TASK_SELECTION = "list_selection";
    private static final String BUNDLE_TASK_SELECTION_ARGS = "list_selection_args";

    private SimpleCursorAdapter taskListAdapter;
    private SpinnerAdapter spinnerAdapter;
    private ArrayList<HashMap<String,String>> loadedLists;
    private long currentListId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        // create SpinnerAdapter
        String[] listFrom = new String[] { MyContract.TaskLists.COLUMN_TITLE };
        int[] listTo = new int[] { android.R.id.text1 };
        spinnerAdapter = new SimpleCursorAdapter(getActionBar().getThemedContext(), android.R.layout.simple_spinner_dropdown_item, null, listFrom, listTo, 0);

        // implement OnNavigationListener callback
        ActionBar.OnNavigationListener onNavigationListener = new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int position, long itemId) {
                Log.d("debug", "Spinner Pos: " + position + ", id: " + itemId);
                Log.d("debug", "Current List Id: " + currentListId);
                if (currentListId != itemId) {
                    Bundle bundle = new Bundle();
                    bundle.putString(BUNDLE_TASK_SELECTION, MyContract.Tasks.COLUMN_LIST_ID + " = ?");
                    bundle.putString(BUNDLE_TASK_SELECTION_ARGS, Long.toString(itemId));
                    getLoaderManager().restartLoader(TASK_LOADER_ID, bundle, TasksActivity.this);
                    currentListId = itemId;
                    Log.d("debug", "Current List: " + getLoadedListTitle(currentListId));
                }
                return false;
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
                intent.putExtra(EXTRA_MY_ID, l);
                startActivity(intent);
            }
        });
        taskListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("EVENT", "Long Clicked! Item: " + l);
                // TODO:リスト選択ダイアログを表示させる。
                return true;
            }
        });

        loadedLists = new ArrayList<HashMap<String, String>>();
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_TASK_SELECTION, MyContract.Tasks.COLUMN_LIST_ID + " = ?");
        bundle.putString(BUNDLE_TASK_SELECTION_ARGS, "1");
        getLoaderManager().initLoader(TASK_LOADER_ID, bundle, this);
        getLoaderManager().initLoader(LIST_LOADER_ID, null, this);
    }

    private String getLoadedListTitle(long id) {
        for (HashMap<String, String> map : loadedLists) {
            if (map.get(MyContract.TaskLists.COLUMN_ID).equals(Long.toString(id))) {
                return map.get(MyContract.TaskLists.COLUMN_TITLE);
            }
        }
        return null;
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
        int id = item.getItemId();
        if (id == R.id.action_add) {
            Intent intent = new Intent(this, TaskEditActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_agenda) {
            Intent intent = new Intent(this, ListsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader onCreateLoader(int loaderId, Bundle bundle) {
        Log.d("debug", "onCreateLoader called!");
        String[] projection;
        String selection = null;
        String[] selectionArgs = null;

        switch (loaderId) {
            case TASK_LOADER_ID:
                projection = new String[] {
                        MyContract.Tasks.COLUMN_ID,
                        MyContract.Tasks.COLUMN_BODY
                };
                if ( bundle != null ) {
                    selection = bundle.getString(BUNDLE_TASK_SELECTION);
                    selectionArgs = new String[] { bundle.getString(BUNDLE_TASK_SELECTION_ARGS) };
                    Log.d("debug", "got loader bundle!");
                }

                return new CursorLoader(this, MyContentProvider.TASKS_URI, projection, selection, selectionArgs, "updated desc");
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
                        MyContract.TaskLists.COLUMN_ORDER + " asc"
                );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader loader, Object cursor) {
        switch (loader.getId()) {
            case TASK_LOADER_ID:
                taskListAdapter.swapCursor((android.database.Cursor) cursor);
                break;
            case LIST_LOADER_ID:
                Cursor c = (Cursor) cursor;
                while (c.moveToNext()) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(MyContract.TaskLists.COLUMN_ID, c.getString(c.getColumnIndex(MyContract.TaskLists.COLUMN_ID)));
                    map.put(MyContract.TaskLists.COLUMN_TITLE, c.getString(c.getColumnIndex(MyContract.TaskLists.COLUMN_TITLE)));
                    loadedLists.add(map);
                }
                ((SimpleCursorAdapter) spinnerAdapter).swapCursor(c);
                currentListId = Long.parseLong( loadedLists.get(0).get(MyContract.TaskLists.COLUMN_ID) );
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
                ((SimpleCursorAdapter) spinnerAdapter).swapCursor(null);
                break;
        }
    }
}
