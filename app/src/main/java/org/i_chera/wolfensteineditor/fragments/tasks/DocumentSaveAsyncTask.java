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

import android.content.Context;
import android.os.AsyncTask;

import org.i_chera.wolfensteineditor.ProgressCallback;
import org.i_chera.wolfensteineditor.document.Document;

public class DocumentSaveAsyncTask extends AsyncTask<Document, ProgressCallback.Data, Boolean>
{
    public interface Listener
    {
        public void removeDocumentTask();
    }

    private final Context mContext;
    private final Listener mListener;
    private boolean mDestroyed;

    public DocumentSaveAsyncTask(Context context, Listener listener)
    {
        mContext = context;
        mListener = listener;
    }

    @Override
    protected Boolean doInBackground(Document... params)
    {
        boolean result = params[0].autosave(mContext, null);
        // FIXME: this won't resist concurrency
        return !isCancelled();
    }

    private void endTask()
    {
        if(mDestroyed)
            return;

        if(mListener != null)
            mListener.removeDocumentTask();
    }

    public void destroy()
    {
        endTask();
        mDestroyed = true;
        cancel(true);
    }

    @Override
    protected void onCancelled()
    {
        endTask();
    }

    @Override
    protected void onPostExecute(Boolean result)
    {
        endTask();
    }
}
