package org.i_chera.wolfensteineditor.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import org.i_chera.wolfensteineditor.MainActivity;

public class AlertFragment extends DialogFragment
{
    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";
    private static final String ARG_DISMISS_TEXT = "dismissText";
    private static final String ARG_POSITIVE_TEXT = "positiveText";
    private static final String ARG_NEGATIVE_TEXT = "negativeText";

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getArguments().getString(ARG_TITLE));
        builder.setMessage(getArguments().getString(ARG_MESSAGE));
        builder.setNeutralButton(getArguments().getString(ARG_DISMISS_TEXT), null);
        String positive = getArguments().getString(ARG_POSITIVE_TEXT);
        if(positive != null && !positive.isEmpty())
            builder.setPositiveButton(positive, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    ((MainActivity)getActivity()).onAlertYes(getTag());
                }
            });
        String negative = getArguments().getString(ARG_NEGATIVE_TEXT);
        if(negative != null && !negative.isEmpty())
            builder.setNegativeButton(negative, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    ((MainActivity)getActivity()).onAlertNo(getTag());
                }
            });
        return builder.show();
    }

    public static class Builder
    {
        private final FragmentManager mManager;
        private final String mTitle;
        private final String mMessage;
        private final String mDismissText;
        private String mPositiveText;
        private String mNegativeText;


        public Builder(FragmentActivity activity, String title, String message, String dismissText)
        {
            mManager = activity.getSupportFragmentManager();
            mTitle = title;
            mMessage = message;
            mDismissText = dismissText;
        }

        public Builder setPositiveButton(String positiveText)
        {
            mPositiveText = positiveText;
            return this;
        }

        public Builder setNegativeButton(String negativeText)
        {
            mNegativeText = negativeText;
            return this;
        }

        public void show(String tag)
        {
            AlertFragment fragment = new AlertFragment();
            Bundle args = new Bundle();

            args.putString(ARG_TITLE, mTitle);
            args.putString(ARG_MESSAGE, mMessage);
            args.putString(ARG_DISMISS_TEXT, mDismissText);
            args.putString(ARG_POSITIVE_TEXT, mPositiveText);
            args.putString(ARG_NEGATIVE_TEXT, mNegativeText);

            fragment.setArguments(args);

            fragment.show(mManager, tag);
        }
    }
}
