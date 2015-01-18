package org.i_chera.wolfensteineditor.fragments;

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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.i_chera.wolfensteineditor.R;

/**
 * Created by ioan_chera on 15.01.2015.
 */
public class StartFragment extends SwitchableFragment {

    // Dynamic/automatic
    private ViewPager mPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_start, container, false);
        mPager = (ViewPager)v.findViewById(R.id.pager);

        // Needs to be set now
        mPager.setAdapter(new StartAdapter(getChildFragmentManager()));

        return v;
    }

    ////////////////////////////////////////////////////////////////////////////

    public void setProgressText(String text)
    {
//        mProgressInfoLabel.setText(text);
    }

    public void endProgress()
    {
//        mProgressIndicator.setVisibility(View.GONE);
//        mProgressInfoLabel.setVisibility(View.GONE);
    }

    @Override
    protected void saveState(Bundle target)
    {
    }

    @Override
    public boolean handleBackButton()
    {
        return false;
    }

    private class StartAdapter extends FragmentPagerAdapter
    {

        public StartAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return new FileOpenFragment();
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            return "Browse";
        }

    }
}
