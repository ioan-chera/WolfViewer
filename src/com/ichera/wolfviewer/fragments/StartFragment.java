/*
 * Wolfenstein 3D editor for Android
 * Copyright (C) 2014  Ioan Chera
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

package com.ichera.wolfviewer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ichera.wolfviewer.R;

public class StartFragment extends SwitchableFragment 
{
	private ProgressBar mProgressIndicator;
	private TextView mProgressInfoLabel;
	
	private boolean mDeferStartProgress;
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.fragment_start, container, false);
		
        mProgressIndicator = (ProgressBar)v.findViewById(R.id.progress_indicator);
        mProgressInfoLabel = (TextView)v.findViewById(R.id.progress_info_label);
        
        if(mDeferStartProgress)
        	startProgress();

		return v;
	}
	
	////////////////////////////////////////////////////////////////////////////
	
	public void startProgress()
	{
		if(mProgressIndicator == null)
		{
			mDeferStartProgress = true;
			return;
		}
		
		mDeferStartProgress = false;
			
		mProgressIndicator.setVisibility(View.VISIBLE);
		mProgressInfoLabel.setVisibility(View.VISIBLE);
		mProgressInfoLabel.setText("");
	}
	
	public void setProgressText(String text)
	{
		mProgressInfoLabel.setText(text);
	}
	
	public void endProgress()
	{
		mProgressIndicator.setVisibility(View.GONE);
		mProgressInfoLabel.setVisibility(View.GONE);
	}

	@Override
	protected void saveState(Bundle target) 
	{
		
	}
}
