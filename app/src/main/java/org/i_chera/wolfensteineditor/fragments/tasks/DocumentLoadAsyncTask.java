package org.i_chera.wolfensteineditor.fragments.tasks;

/*
 * Wolfenstein 3D editor for Android
 * Copyright (C) 2015  Ioan Chera
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import org.i_chera.wolfensteineditor.MainActivity;
import org.i_chera.wolfensteineditor.ProgressCallback;
import org.i_chera.wolfensteineditor.R;
import org.i_chera.wolfensteineditor.document.Document;

import java.io.File;

public class DocumentLoadAsyncTask extends AsyncTask<Void, ProgressCallback.Data, Document>
{
    public interface Listener
    {
        public void tryCancelDocumentTask();
        public void removeDocumentTask();
        public void onSuccessDocumentTask(Document document, File path);
        public void onFailureDocumentTask();
    }

    private final Fragment mFragment;
    private final Listener mListener;
    private final File mPath;
    private final Context mContext;

    private boolean mAutoload;

    private ProgressDialog mProgressDialog;
    private boolean mDestroyed;

    public DocumentLoadAsyncTask(Context context, Fragment fragment, File path, Listener listener)
    {
        assert fragment != null;
        mFragment = fragment;
        mListener = listener;
        mPath = path;
        mContext = context;
    }

    public void setAutoload()
    {
        mAutoload = true;
    }

    @Override
    protected void onPreExecute() {
        if(mFragment.getActivity() != null) {
            mProgressDialog = ProgressDialog.show(mFragment.getActivity(), mContext.getString(R.string.loading_levels),
                    mContext.getString(R.string.starting), false, true, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            if(mListener != null)
                                mListener.tryCancelDocumentTask();
                        }
                    });
        }
    }

    @Override
    protected Document doInBackground(Void... params) {
        Document document = new Document();
        boolean result = document.loadFromDirectory(mContext, mPath, mAutoload, new ProgressCallback() {
            @Override
            public void onProgress(int position, int max, String message) {
                ProgressCallback.Data data = new ProgressCallback.Data();
                data.position = position;
                data.max = max;
                data.message = message;
                publishProgress(data);
            }
        });
        if(isCancelled())
            return null;
        return result ? document : null;
    }

    @Override
    protected void onProgressUpdate(ProgressCallback.Data... values) {
        if(mProgressDialog != null) {
            mProgressDialog.setProgress(values[0].position);
            mProgressDialog.setMax(values[0].max);
            mProgressDialog.setMessage(values[0].message);
        }
    }

    private void endTask()
    {
        if(mDestroyed)  // don't call methods of destroyed fragment
            return;

        if(mListener != null)
            mListener.removeDocumentTask();
        if(mProgressDialog != null && mProgressDialog.isShowing())
        {
            mProgressDialog.dismiss();
        }
    }

    public void destroy()
    {
        endTask();  // call it early
        mDestroyed = true;
        cancel(true);
    }


    @Override
    protected void onCancelled()
    {
        endTask();
        Log.i("FileOpenFragment", "Cancelled document task for " + mPath.getPath());
    }

    @Override
    protected void onPostExecute(Document document) {
        endTask();
        if(mFragment.getActivity() == null) {
            Log.w("FileOpenFragment", "Async task finished after activity was destroyed");
            return;
        }

        if (document != null) {
            if(mListener != null)
                mListener.onSuccessDocumentTask(document, mPath);
        }
        else
        {
            MainActivity activity = (MainActivity)mFragment.getActivity();
            activity.showErrorAlert(mContext.getString(R.string.level_load_error),
                    mContext.getString(R.string.could_not_open_data) + mPath.getPath());
            if(mListener != null)
                mListener.onFailureDocumentTask();
        }
    }
}
