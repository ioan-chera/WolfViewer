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
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import org.i_chera.wolfensteineditor.document.Document;
import org.i_chera.wolfensteineditor.fragments.LevelFragment;
import org.i_chera.wolfensteineditor.fragments.StartFragment;
import org.i_chera.wolfensteineditor.fragments.SwitchableFragment;

import java.io.File;


public class MainActivity extends ActionBarActivity
{

    static final String TAG = "MainActivity";

    private static final int REQUEST_OPEN_WOLF = 1;
    private static final String EXTRA_CURRENT_PATH = "currentPath";

    private static final String EXTRA_FRAGMENT_START = "fragmentStart";
    private static final String EXTRA_FRAGMENT_LEVEL = "fragmentLevel";

    private Bundle mStartBundle;
    private Bundle mLevelBundle;

    private SwitchableFragment mCurrentFragment;

    // saved
    private File mCurrentPath;

    // generated
    private Document mDocument;

    // workers
    private DocumentLoadAsyncTask mDocumentLoadAsyncTask;

    // sound engine
    private AudioTrack mTrack;

    // static
    public static final int FLOOR_COLOUR = Palette.WL6[25];

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Global.initialize(this);
        mDocument = Document.getInstance();
        setContentView(R.layout.activity_main);
        findViewById(android.R.id.content).setBackgroundColor(FLOOR_COLOUR);

        if(savedInstanceState != null)
        {
            String value = savedInstanceState.getString(EXTRA_CURRENT_PATH);
            if(value != null)
                mCurrentPath = new File(value);
            mLevelBundle = savedInstanceState.getBundle(EXTRA_FRAGMENT_LEVEL);
            mStartBundle = savedInstanceState.getBundle(EXTRA_FRAGMENT_START);
            establishCurrentFragment();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        if(mCurrentPath != null)
            outState.putString(EXTRA_CURRENT_PATH, mCurrentPath.getPath());
        if(mStartBundle != null)
            outState.putBundle(EXTRA_FRAGMENT_START, mStartBundle);
        if(mLevelBundle != null)
            outState.putBundle(EXTRA_FRAGMENT_LEVEL, mLevelBundle);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_OPEN_WOLF && resultCode == RESULT_OK)
        {
            // TODO: reimplement
            mCurrentPath = new File(data.getStringExtra(OpenActivity.EXTRA_CURRENT_PATH));

            if(mDocumentLoadAsyncTask != null)
                Global.showErrorAlert(this, "", "A document is currently opening.");
            else
            {
                mDocumentLoadAsyncTask = new DocumentLoadAsyncTask();
                mDocumentLoadAsyncTask.execute(mCurrentPath);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.action_open:
            {
                Intent intent = new Intent(this, OpenActivity.class);
                if(mCurrentPath != null)
                    intent.putExtra(OpenActivity.EXTRA_CURRENT_PATH,
                            mCurrentPath.getPath());
                startActivityForResult(intent, REQUEST_OPEN_WOLF);
                return true;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if(mTrack != null)
            mTrack.release();
    }

    @Override
    public void onBackPressed()
    {
        if(mCurrentFragment.handleBackButton())
            return;
        super.onBackPressed();
    }

    private void  establishCurrentFragment()
    {
        SwitchableFragment curFragment = (SwitchableFragment)getSupportFragmentManager()
                .findFragmentById(android.R.id.content);
        if(curFragment != null)
        {
            mCurrentFragment = curFragment;
            if(curFragment instanceof LevelFragment)
                curFragment.setStateBundle(mLevelBundle);
            else if(curFragment instanceof StartFragment)
                curFragment.setStateBundle(mStartBundle);
        }
    }

    private Fragment showFragment(Class<? extends SwitchableFragment>
                                          fragmentClass, Bundle state)
    {
        SwitchableFragment curFragment = (SwitchableFragment)getSupportFragmentManager()
                .findFragmentById(android.R.id.content);
        if(curFragment != null)
        {
            curFragment.saveSwitchState();
        }

        curFragment = (SwitchableFragment)Fragment.instantiate(this,
                fragmentClass.getName(), state);
        if(fragmentClass == LevelFragment.class)
        {
            if(mLevelBundle == null)
                mLevelBundle = new Bundle();
            curFragment.setStateBundle(mLevelBundle);
        }
        else if(fragmentClass == StartFragment.class)
        {
            if(mStartBundle == null)
                mStartBundle = new Bundle();
            curFragment.setStateBundle(mStartBundle);
        }

        getSupportFragmentManager().beginTransaction().replace(
                android.R.id.content,
                curFragment)
                .commit();

        return mCurrentFragment = curFragment;
    }

    /**
     * Document load async task
     * @author ioan
     *
     */
    private class DocumentLoadAsyncTask extends AsyncTask<File, String, Boolean>
    {
        StartFragment mStartFragment;
        @Override
        protected void onPreExecute()
        {
            mStartFragment = (StartFragment)showFragment(StartFragment.class, mStartBundle);
            mStartFragment.startProgress();
            // TODO: disable access to all fragments while executing
        }

        @Override
        protected Boolean doInBackground(File... params)
        {
            return mDocument.loadFromDirectory(params[0], new RunnableArg
                    <String>()
            {
                @Override
                public void run()
                {
                    publishProgress(mArgs);
                }
            });
        }

        @Override
        protected void onProgressUpdate(String... values)
        {
            mStartFragment.setProgressText(values[0]);
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            mDocumentLoadAsyncTask = null;
            mStartFragment.endProgress();
            // TODO: allow access

            if(result)
            {
                ((LevelFragment)showFragment(LevelFragment.class, mLevelBundle))
                        .updateData();
            }
            else
                Global.showErrorAlert(MainActivity.this,
                        "", "Can't open document " + mCurrentPath);
        }
    }
}
