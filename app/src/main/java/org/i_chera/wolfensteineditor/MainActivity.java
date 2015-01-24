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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
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

    private float mPixelScale;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        initializeStaticData();
        setContentView(R.layout.activity_main);

        if(savedInstanceState == null)
        {
            // TODO: get path from shared preferences
            goToStartFragment();
        }
    }

    private void initializeStaticData()
    {
        mPixelScale = getResources().getDisplayMetrics().density;
    }

    public float getPixelScale()
    {
        return mPixelScale;
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

    private void setToFragment(Fragment fragment, String name)
    {
        getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        pushFragment(fragment, name);
    }

    private void pushFragment(Fragment fragment, String name)
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(android.R.id.content, fragment, name);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.addToBackStack(name);
        transaction.commitAllowingStateLoss();
    }

    public void popFragment()
    {
        if(getSupportFragmentManager().getBackStackEntryCount() >= 2)
            getSupportFragmentManager().popBackStack();
        else
            finish();   // don't remove all fragments. Just quit.
    }

    private Fragment getTopFragment()
    {
        int fragmentCount = getSupportFragmentManager().getBackStackEntryCount();
        if(fragmentCount > 0)
        {
            String topFragmentName = getSupportFragmentManager().getBackStackEntryAt(fragmentCount - 1).getName();
            return getSupportFragmentManager().findFragmentByTag(topFragmentName);
        }
        return null;
    }

    public void goToLevelFragment(Document document, File path)
    {
        // Need to pop whatever is now set
        getSupportFragmentManager().popBackStackImmediate(TAG_START_FRAGMENT, 0);
        if(!(getTopFragment() instanceof StartFragment))
            throw new IllegalStateException("LevelFragment can only be pushed over a StartFragment; found "
                    + getTopFragment().getClass().getName());

        LevelFragment fragment = new LevelFragment();
        fragment.setDocument(document); // set it, so it doesn't have to set it again when loaded

        Bundle args = new Bundle();
        args.putString(LevelFragment.ARG_PATH_NAME, path.getPath());
        fragment.setArguments(args);

        pushFragment(fragment, TAG_LEVEL_FRAGMENT);
    }

    public void goToStartFragment()
    {
        // TODO: use path as argument
        StartFragment fragment = new StartFragment();

        setToFragment(fragment, TAG_START_FRAGMENT);
    }

    @Override
    public void onBackPressed()
    {
        Fragment fragment = getTopFragment();
        if(fragment instanceof BackButtonHandler && ((BackButtonHandler) fragment).handleBackButton())
        {
            return;
        }

        showConfirmAlert(getString(R.string.exit), getString(R.string.exit_question),
                getString(R.string.exit), getString(R.string.do_not_exit), new Runnable()
        {

            @Override
            public void run()
            {
                finish();
            }
        });
    }

    public void showErrorAlert(String title, String message)
    {
        new AlertDialog.Builder(this).setTitle(title)
                .setMessage(message).setNeutralButton(getString(R.string.dismiss), null).show();
    }

    public void showConfirmAlert(String title, String message, String yesText, String noText, final Runnable yesAction)
    {
        assert yesAction != null;
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setNegativeButton(noText, null)
                .setPositiveButton(yesText, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        yesAction.run();
                    }
                }).show();
    }
}
