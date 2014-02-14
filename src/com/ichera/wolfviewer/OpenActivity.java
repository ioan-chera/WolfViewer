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

package com.ichera.wolfviewer;

import java.io.File;
import java.util.Arrays;
import java.util.Stack;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Activity to open files from file system
 * @author ioan
 *
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
	private Button				mOpenButton;
	private Button				mCancelButton;
	private ListView			mListView;
	
	private class FolderState
	{
		public File 		path;
		public Parcelable	listInstanceState;
	}
	
	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
			setContentView(R.layout.activity_open_portrait);
		else
			setContentView(R.layout.activity_open_landscape);
		mCurrentPathLabel = (TextView)findViewById(R.id.current_folder);
		mOpenButton = (Button)findViewById(R.id.open_button);
		mCancelButton = (Button)findViewById(R.id.cancel_button);
		mListView = (ListView)findViewById(R.id.list);
		
		mOpenButton.setOnClickListener(this);
		mCancelButton.setOnClickListener(this);
		
		// Init current path
		String savedValue = null;
		if(savedInstanceState != null)
		{
			savedValue = savedInstanceState.getString(EXTRA_CURRENT_PATH);
			
			String[] pathHistory = savedInstanceState
					.getStringArray(EXTRA_PATH_HISTORY);
			if(pathHistory != null)
			{
				Parcelable[] stateHistory = savedInstanceState
						.getParcelableArray(EXTRA_STATE_HISTORY);
				if(stateHistory != null && 
						stateHistory.length == pathHistory.length)
				{
					mFolderStack = new Stack<OpenActivity.FolderState>();
					for(int i = pathHistory.length - 1; i >= 0; --i)
					{
						FolderState fs = new FolderState();
						fs.path = new File(pathHistory[i]);
						fs.listInstanceState = stateHistory[i];
						mFolderStack.push(fs);
					}
				}
			}
		}
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
	
	@Override
	protected void onSaveInstanceState (Bundle outState)
	{
		if(mFolderStack != null && mFolderStack.size() > 0)
		{
			String[] pathArray = new String[mFolderStack.size()];
			Parcelable[] stateArray = new Parcelable[mFolderStack.size()];
			for(int i = 0; i < pathArray.length; ++i)
			{
				FolderState fs = mFolderStack.pop();
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
		 * @param list List of files
		 */
		public OpenAdapter(File dir)
		{
			setFileList(dir, false);
		}
		
		/**
		 * Sets new file list
		 * @param fileList
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
			return mFileList.length;
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
						AbsListView.LayoutParams.MATCH_PARENT, (int)(48 * Global
								.getScale()));
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
	
//		private void lookForRelevantFiles(File file, ArrayList<File> relevantFiles)
//		{
//			if(isCancelled())
//				return;
//			if(file.isDirectory())
//			{
//				publishProgress(file.getPath());
//				File[] subdirs = file.listFiles(new FileFilter() 
//				{
//					@Override
//					public boolean accept(File pathname) 
//					{
//						return pathname.isDirectory();
//					}
//				});
//				File[] wl6files = file.listFiles(new FilenameFilter() 
//				{
//					@Override
//					public boolean accept(File dir, String filename) 
//					{
//						for(String name : Global.s_wolfFileNames)
//						{
//							if(name.compareToIgnoreCase(filename) == 0)
//								return true;
//						}
//						return false;
//					}
//				});
//				
//				if(wl6files == null || subdirs == null)
//					return;
//				
//				if(wl6files.length == Global.s_wolfFileNames.length)
//					relevantFiles.add(file);
//			
//				for(File subdir : subdirs)
//					lookForRelevantFiles(subdir, relevantFiles);
//			}
//		}
		

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, 
			long id) 
	{
		File dir =  (File)mAdapter.getItem(position);
		if(dir.isDirectory())
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
