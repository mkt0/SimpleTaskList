package com.example.makoto.simpletasklist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by makoto on 2014/12/08.
 */
public class MyAlertDialogFragment extends DialogFragment {

    private static final String ARG_TITLE = "title";
    private MyAlertDialogFragmentCallbacks mCallbacks;

    public static MyAlertDialogFragment newInstance(int title) {
        MyAlertDialogFragment fragment = new MyAlertDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt(ARG_TITLE);

        return new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int whichButton) {
                        mCallbacks.onMyAlertDialogPositiveClick();
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int whichButton) {
                        mCallbacks.onMyAlertDialogNegativeClick();
                    }
                }).create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (MyAlertDialogFragmentCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement MyAlertDialogFragmentCallbacks.");
        }
    }

    public static interface MyAlertDialogFragmentCallbacks {
        void onMyAlertDialogPositiveClick();
        void onMyAlertDialogNegativeClick();
    }
}
