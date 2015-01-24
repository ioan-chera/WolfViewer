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

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;

import org.i_chera.wolfensteineditor.document.Document;
import org.i_chera.wolfensteineditor.fragments.LevelFragment;
import org.i_chera.wolfensteineditor.fragments.StartFragment;

import java.io.File;


public class MainActivity extends ActionBarActivity
{
//    static final String TAG = "MainActivity";
    private static final String TAG_START_FRAGMENT = "startFragment";
    private static final String TAG_LEVEL_FRAGMENT = "levelFragment";

    // sound engine
//    private AudioTrack mTrack;

    // static
    public static final int FLOOR_COLOUR = Palette.WL6[25];

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Global.initialize(this);
        setContentView(R.layout.activity_main);

        if(savedInstanceState == null)
        {
            // TODO: get path from shared preferences
            goToStartFragment();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    public void goToLevelFragment(Document document, File path)
    {
        LevelFragment fragment = new LevelFragment();
        fragment.setDocument(document); // unsaved data

        Bundle args = new Bundle();
        args.putString(LevelFragment.ARG_PATH_NAME, path.getPath());
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(android.R.id.content, fragment, TAG_LEVEL_FRAGMENT);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.commitAllowingStateLoss();
    }

    public void goToStartFragment()
    {
        // TODO: use path as argument
        StartFragment fragment = new StartFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(android.R.id.content, fragment, TAG_START_FRAGMENT);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.commitAllowingStateLoss();
    }
}
