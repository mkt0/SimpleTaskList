package com.example.makoto.simpletasklist;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;


public class ListsActivity extends Activity implements LoaderManager.LoaderCallbacks {

    // TODO: リストが保持するアイテムの数をカッコ内に表示する。

    public static final String EXTRA_LIST_ID = "com.example.makoto.simpletasklist.EXTRA_LIST_ID";
    private ListView listListView;
    private SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lists);

        String[] from = new String[] {
                MyContract.TaskLists.COLUMN_TITLE
        };

        int[] to = new int[] {
                android.R.id.text1
        };

        adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_1,
                null,
                from,
                to,
                0
        );


        listListView = (ListView) findViewById(R.id.listListView);
        listListView.setAdapter(adapter);

        listListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(ListsActivity.this, ListEditActivity.class);
                intent.putExtra(EXTRA_LIST_ID, l);
                startActivity(intent);
            }
        });
        getLoaderManager().initLoader(0, null, this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.lists, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_new_list) {
            Intent intent = new Intent(this, ListEditActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader onCreateLoader(int i, Bundle bundle) {
        String[] projection = new String[] {
                MyContract.TaskLists.COLUMN_ID,
                MyContract.TaskLists.COLUMN_TITLE
        };
        return new CursorLoader(this, MyContentProvider.TASK_LISTS_URI, projection, null, null, "updated desc");
    }

    @Override
    public void onLoadFinished(Loader loader, Object cursor) {
        adapter.swapCursor((android.database.Cursor) cursor);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        adapter.swapCursor(null);
    }
}
