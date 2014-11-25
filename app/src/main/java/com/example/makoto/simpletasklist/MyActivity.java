package com.example.makoto.simpletasklist;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SpinnerAdapter;


public class MyActivity extends Activity implements LoaderManager.LoaderCallbacks {

    public static final String EXTRA_MY_ID = "com.example.makoto.simpletasklist.EXTRA_MY_ID";
    private static final int TASK_LOADER_ID = 0;
    private static final int LIST_LOADER_ID = 1;

    private SimpleCursorAdapter taskListAdapter;
    private SpinnerAdapter spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        // create SpinnerAdapter
        String[] listFrom = new String[] { MyContract.TaskLists.COLUMN_TITLE };
        int[] listTo = new int[] { android.R.id.text1 };
        // TODO: ドロップダウンの文字色を変える
        spinnerAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_dropdown_item, null, listFrom, listTo, 0);

        // implement OnNavigationListener callback
        ActionBar.OnNavigationListener onNavigationListener = new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int position, long itemId) {
                // TODO: リスト切り替え処理
                Log.d("DEBUG", "Navigation item selected: " + position);
                return false;
            }
        };

        // enable Actionbar dropdown list
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setListNavigationCallbacks(spinnerAdapter, onNavigationListener);


        // set taskListAdapter to inboxListView
        String[] taskFrom = new String[] { MyContract.Tasks.COLUMN_BODY };
        int[] taskTo = new int[] { android.R.id.text1 };
        taskListAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null, taskFrom, taskTo, 0);
        ListView inboxListView = (ListView) findViewById(R.id.inboxListView);
        inboxListView.setAdapter(taskListAdapter);

        // set Listeners to inboxListView
        inboxListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MyActivity.this, EditActivity.class);
                intent.putExtra(EXTRA_MY_ID, l);
                startActivity(intent);
            }
        });
        inboxListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("EVENT", "Long Clicked! Item: " + l);
                // TODO:リスト選択ダイアログを表示させる。
                return true;
            }
        });

        getLoaderManager().initLoader(TASK_LOADER_ID, null, this);
        getLoaderManager().initLoader(LIST_LOADER_ID, null, this);
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
            Intent intent = new Intent(this, EditActivity.class);
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
        String[] projection;
        switch (loaderId) {
            case TASK_LOADER_ID:
                projection = new String[] {
                        MyContract.Tasks.COLUMN_ID,
                        MyContract.Tasks.COLUMN_BODY
                };
                return new CursorLoader(this, MyContentProvider.TASKS_URI, projection, null, null, "updated desc");
            case LIST_LOADER_ID:
                projection = new String[] {
                        MyContract.TaskLists.COLUMN_ID,
                        MyContract.TaskLists.COLUMN_TITLE
                };
                return new CursorLoader(this, MyContentProvider.TASK_LISTS_URI, projection, null, null, "updated desc");
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
                ((SimpleCursorAdapter) spinnerAdapter).swapCursor((android.database.Cursor) cursor);
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
