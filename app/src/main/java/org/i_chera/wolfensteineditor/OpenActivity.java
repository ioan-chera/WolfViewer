package org.i_chera.wolfensteineditor;

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

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.Arrays;
import java.util.Stack;

/**
 * Created by ioan_chera on 15.01.2015.
 */
public class OpenActivity extends ActionBarActivity implements
        AdapterView.OnItemClickListener, View.OnClickListener
{
    public static final String	EXTRA_CURRENT_PATH = "currentPath";
    private static final String	EXTRA_PATH_HISTORY = "pathHistory";
    private static final String EXTRA_STATE_HISTORY = "stateHistory";

    private OpenAdapter			mAdapter;
    private File				mCurrentPath;
    private Stack<FolderState>	mFolderStack;
    private TextView			mCurrentPathLabel;
    private Button mOpenButton;
    private Button				mCancelButton;
    private ListView mListView;

    private static float sScale;

    private class FolderState
    {
        public File 		path;
        public Parcelable listInstanceState;
    }

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            setContentView(R.layout.activity_open_portrait);
        else
            setContentView(R.layout.activity_open_landscape);

        if(sScale == 0)
        {
            try
            {
                sScale = getResources().getDisplayMetrics().density;
            }
            catch(NullPointerException npe)
            {
                sScale = 1;
            }
        }

        mCurrentPathLabel = (TextView)findViewById(R.id.current_folder);
        mOpenButton = (Button)findViewById(R.id.open_button);
        mCancelButton = (Button)findViewById(R.id.cancel_button);
        mListView = (ListView)findViewById(R.id.list);

        mOpenButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);

        // Init current path
        String savedValue = loadPreservedData(savedInstanceState);

        if(savedValue == null)
        {
            Bundle args = getIntent().getExtras();
            if(args != null)
            {
                String value = args.getString(EXTRA_CURRENT_PATH);
                if(value != null)
                    mCurrentPath = new File(value);
            }
        }
        else
            mCurrentPath = new File(savedValue);
        if(mCurrentPath == null)
            mCurrentPath = Environment.getExternalStorageDirectory();


        mAdapter = new OpenAdapter(mCurrentPath);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
    }

    /**
     * Loads the current folder and history from the saved instance state
     * @param savedInstanceState
     * @return The current path.
     */
    private final String loadPreservedData(final Bundle savedInstanceState)
    {
        String savedValue = null;
        if(savedInstanceState != null)
        {
            savedValue = savedInstanceState.getString(EXTRA_CURRENT_PATH);

            final String[] pathHistory = savedInstanceState
                    .getStringArray(EXTRA_PATH_HISTORY);
            if(pathHistory != null)
            {
                final Parcelable[] stateHistory = savedInstanceState
                        .getParcelableArray(EXTRA_STATE_HISTORY);
                if(stateHistory != null && stateHistory.length == pathHistory.length)
                {
                    mFolderStack = new Stack<OpenActivity.FolderState>();
                    for(int i = pathHistory.length - 1; i >= 0; --i)
                    {
                        final FolderState fs = new FolderState();
                        fs.path = new File(pathHistory[i]);
                        fs.listInstanceState = stateHistory[i];
                        mFolderStack.push(fs);
                    }
                }
            }
        }
        return savedValue;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.open, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        if(item.getItemId() == R.id.action_up_one_level)
        {
            final File superdir = mCurrentPath.getParentFile();
            if(superdir != null)
                changeDirectory(superdir);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState (final Bundle outState)
    {
        if(mFolderStack != null && mFolderStack.size() > 0)
        {
            String[] pathArray = new String[mFolderStack.size()];
            Parcelable[] stateArray = new Parcelable[mFolderStack.size()];
            for(int i = 0; i < pathArray.length; ++i)
            {
                final FolderState fs = mFolderStack.pop();
                pathArray[i] = fs.path.getPath();
                stateArray[i] = fs.listInstanceState;
            }
            outState.putStringArray(EXTRA_PATH_HISTORY, pathArray);
            outState.putParcelableArray(EXTRA_STATE_HISTORY, stateArray);
        }
        outState.putString(EXTRA_CURRENT_PATH, mCurrentPath.getPath());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed()
    {
        if(mFolderStack != null && mFolderStack.size() > 0)
        {
            FolderState fs = mFolderStack.pop();
            mCurrentPath = fs.path;
            mAdapter.setFileList(mCurrentPath);
            mListView.onRestoreInstanceState(fs.listInstanceState);
        }
        else
            super.onBackPressed();
    }

    /**
     * Adapter for this list
     * @author ioan
     *
     */
    private class OpenAdapter extends BaseAdapter
    {
        private File[]	mFileList;

        /**
         * Main constructor
         * @param dir List of files
         */
        public OpenAdapter(File dir)
        {
            setFileList(dir, false);
        }

        /**
         * Sets new file list
         * @param dir
         */
        public void setFileList(File dir)
        {
            setFileList(dir, true);
        }

        /**
         * Private method
         * @param dir
         * @param notify
         */
        private void setFileList(File dir, boolean notify)
        {
            File[] files = dir.listFiles();
            if(files != null)
                Arrays.sort(files);
            if(mFileList != files)
            {
                mCurrentPathLabel.setText(dir.getPath());
                mFileList = files;
                if(notify)
                    notifyDataSetChanged();
            }
        }

        @Override
        public int getCount()
        {
            return mFileList != null ? mFileList.length : 0;
        }

        @Override
        public Object getItem(int position)
        {
            return mFileList[position];
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            TextView item;
            if(convertView == null)
            {
                item = new TextView(OpenActivity.this);
                AbsListView.LayoutParams alvlp = new AbsListView.LayoutParams(
                        AbsListView.LayoutParams.MATCH_PARENT, (int)(48 * sScale));
                item.setLayoutParams(alvlp);
                item.setGravity(Gravity.CENTER_VERTICAL);
            }
            else
                item = (TextView)convertView;

            if(mFileList[position].isDirectory())
                item.setTypeface(Typeface.DEFAULT_BOLD);
            else
                item.setTypeface(Typeface.DEFAULT);
            item.setText(mFileList[position].getName());

            return item;
        }

    }

    private void changeDirectory(File dir)
    {
        if(mFolderStack == null)
            mFolderStack = new Stack<FolderState>();
        FolderState fs = new FolderState();
        fs.listInstanceState = mListView.onSaveInstanceState();
        fs.path = mCurrentPath;
        mFolderStack.push(fs);
        mCurrentPath = dir;
        mAdapter.setFileList(mCurrentPath);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id)
    {
        File dir =  (File)mAdapter.getItem(position);
        if(dir.isDirectory())
        {
            changeDirectory(dir);
        }
    }

    @Override
    public void onClick(View v)
    {
        if(v == mCancelButton)
        {
            setResult(RESULT_CANCELED);
            finish();
        }
        else if(v == mOpenButton)
        {
            Intent data = new Intent();
            data.putExtra(EXTRA_CURRENT_PATH, mCurrentPath.getPath());
            setResult(RESULT_OK, data);
            finish();
        }
    }
}
