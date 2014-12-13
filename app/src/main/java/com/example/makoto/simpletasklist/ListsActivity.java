package com.example.makoto.simpletasklist;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class ListsActivity extends Activity implements LoaderManager.LoaderCallbacks {

    // TODO: リストが保持するアイテムの数をカッコ内に表示する。

    public static final String EXTRA_LIST_ID = "com.example.makoto.simpletasklist.EXTRA_LIST_ID";
    private static final int TASK_LOADER_ID = 0;
    private static final int LIST_LOADER_ID = 1;

    private ListView listListView;
//    private SimpleCursorAdapter adapter;
    private ListItemCursorAdapter adapter;

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

//        adapter = new SimpleCursorAdapter(
//                this,
//                android.R.layout.simple_list_item_1,
//                null,
//                from,
//                to,
//                0
//        );
        adapter = new ListItemCursorAdapter(this, R.layout.row, null, 0);

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
        getLoaderManager().initLoader(LIST_LOADER_ID, null, this);
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
    public Loader onCreateLoader(int id, Bundle bundle) {
        switch (id) {
            case LIST_LOADER_ID:
                String[] projection = new String[] {
                        MyContract.TaskLists.COLUMN_ID,
                        MyContract.TaskLists.COLUMN_TITLE
                };
                return new CursorLoader(this, MyContentProvider.TASK_LISTS_URI, projection, null, null, "updated desc");
            case TASK_LOADER_ID:
                return null;
        }
        throw new IllegalArgumentException("unknown loader id: " + id);
    }

    @Override
    public void onLoadFinished(Loader loader, Object cursor) {
        switch (loader.getId()) {
            case LIST_LOADER_ID:
                adapter.swapCursor((android.database.Cursor) cursor);
                break;
            case TASK_LOADER_ID:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        adapter.swapCursor(null);
    }

    // prepare ListItemAdapter
    private static class ViewHolder {
        private TextView titleView;
        private TextView countView;

        public ViewHolder(View v) {
            titleView = (TextView) v.findViewById(R.id.titleText);
            countView = (TextView) v.findViewById(R.id.countText);
        }
    }
    public class ListItem {
        private String title;
        private int count;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
    public class ListItemCursorAdapter extends CursorAdapter {

        private LayoutInflater inflater;
        private final int resource;

        public ListItemCursorAdapter(Context context, int layoutResourceId, Cursor c, int flags) {
            super(context, c, flags);
            this.resource = layoutResourceId;
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            final View view = inflater.inflate(this.resource, null);
            ViewHolder viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
            bindView(view, context, cursor);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder viewHolder = (ViewHolder) view.getTag();

            String title = cursor.getString(cursor.getColumnIndex(MyContract.TaskLists.COLUMN_TITLE));
            int id = cursor.getInt(cursor.getColumnIndex(MyContract.TaskLists.COLUMN_ID));
            int count = getContentResolver().query(
                    MyContentProvider.TASKS_URI,
                    new String[] { MyContract.Tasks.COLUMN_ID },
                    MyContract.Tasks.COLUMN_LIST_ID + " = ?",
                    new String[] { String.valueOf(id) },
                    null
            ).getCount();

            viewHolder.titleView.setText(title);
            viewHolder.countView.setText(String.valueOf(count));
        }
    }
}
