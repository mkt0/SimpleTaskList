package com.example.makoto.simpletasklist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

/**
 * Created by makoto on 2014/12/09.
 */
public class ListSelectionDialogFragment extends DialogFragment implements LoaderManager.LoaderCallbacks {

    private static final String ARG_TITLE = "titleArgument";
    private static final String ARG_QUERY = "queryArgument";

    private ListSelectionDialogCallbacks mCallbacks;
    private SimpleCursorAdapter adapter;

    static ListSelectionDialogFragment newInstance(int title) {
        ListSelectionDialogFragment fragment = new ListSelectionDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new SimpleCursorAdapter(
                getActivity(),
                android.R.layout.select_dialog_item,
                null,
                new String[] { MyContract.TaskLists.COLUMN_TITLE },
                new int[] { android.R.id.text1 },
                0
        );

        Bundle queryArgs = getArguments().getBundle(ARG_QUERY);
        getLoaderManager().restartLoader(0, queryArgs, this);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int dialogTitle = getArguments().getInt(ARG_TITLE);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_menu_set_as)
                .setTitle(dialogTitle)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position) {
                        mCallbacks.onListSelectionDialogItemClick(
                                position,
                                (int) adapter.getItemId(position),
                                ((Cursor) adapter.getItem(position)).getString(
                                        adapter.getCursor().getColumnIndex(MyContract.TaskLists.COLUMN_TITLE)
                                )
                        );
                    }
                });

        return alertDialogBuilder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (ListSelectionDialogCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement SingleSelectionDialogFragmentCallbacks.");
        }
    }

    @Override
    public Loader onCreateLoader(int i, Bundle bundle) {
        Uri uri = MyContentProvider.TASK_LISTS_URI;
        String[] projection = new String[] {
                MyContract.TaskLists.COLUMN_ID,
                MyContract.TaskLists.COLUMN_TITLE
        };
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = "updated desc";

        return new CursorLoader(getActivity(), uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        adapter.swapCursor((Cursor) data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        adapter.swapCursor(null);
    }

    public static interface ListSelectionDialogCallbacks {
        void onListSelectionDialogItemClick(int position, int id, String title);
    }
}
