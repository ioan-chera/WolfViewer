package com.ichera.wolfviewer;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Activity to open files from file system
 * @author ioan
 *
 */
public class OpenActivity extends ListActivity 
{
	private LayoutInflater		m_inflater;
	private OpenAdapter			m_adapter;
	private ProgressBar			m_progressWheel;
	private TextView			m_progressText;
	private boolean				m_dataLoaded;
	private DirFindAsyncTask	m_dirFindAsyncTask;
	
	/**
	 * Adapter for this list
	 * @author ioan
	 *
	 */
	class OpenAdapter extends BaseAdapter
	{
		File[]	mm_fileList;
		Context	mm_context;
		
		/**
		 * Item holder
		 * @author ioan
		 *
		 */
		class Holder
		{
			public TextView tv;
		}
		
		/**
		 * Main constructor
		 * @param list List of files
		 */
		public OpenAdapter(Context context, File[] list)
		{
			mm_context = context;
			mm_fileList = list;
		}
		
		@Override
		public int getCount() 
		{
			return mm_fileList.length;
		}

		@Override
		public Object getItem(int position) 
		{
			return mm_fileList[position];
		}

		@Override
		public long getItemId(int position) 
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) 
		{
			View item;
			if(convertView == null)
			{
				item = m_inflater.inflate(R.layout.open_list_item, null);
				AbsListView.LayoutParams alvlp = new AbsListView.LayoutParams(
						AbsListView.LayoutParams.MATCH_PARENT, (int)(48 * Global
								.s_scale));
				item.setLayoutParams(alvlp);
				Holder h = new Holder();
				h.tv = (TextView)item.findViewById(R.id.label);
				item.setTag(h);
			}
			else
				item = convertView;
			
			((Holder)item.getTag()).tv.setText(mm_fileList[position].getPath());
			
			return item;
		}
		
	}
	
	class DirFindAsyncTask extends AsyncTask<File, String, ArrayList<File>>
	{
		OpenActivity mm_activity;
		
		public DirFindAsyncTask(OpenActivity activity)
		{
			mm_activity = activity;
		}
		
		@Override
		protected void onProgressUpdate (String... values)
		{
			m_progressText.setText(values[0]);
		}
		
		@Override
		protected void onPreExecute ()
		{
			m_progressText.setVisibility(View.VISIBLE);
			m_progressWheel.setVisibility(View.VISIBLE);
		}
		
		/**
		 * Looks thru file system for wolf3d folders
		 * @param file The root folder
		 * @param relevantFiles The list to fill
		 */
		private void lookForRelevantFiles(File file, ArrayList<File> relevantFiles)
		{
			if(isCancelled())
				return;
			if(file.isDirectory())
			{
				publishProgress(file.getPath());
				File[] subdirs = file.listFiles(new FileFilter() 
				{
					@Override
					public boolean accept(File pathname) 
					{
						return pathname.isDirectory();
					}
				});
				File[] wl6files = file.listFiles(new FilenameFilter() 
				{
					@Override
					public boolean accept(File dir, String filename) 
					{
						for(String name : Global.s_wolfFileNames)
						{
							if(name.compareToIgnoreCase(filename) == 0)
								return true;
						}
						return false;
					}
				});
				
				if(wl6files.length == Global.s_wolfFileNames.length)
					relevantFiles.add(file);
			
				for(File subdir : subdirs)
					lookForRelevantFiles(subdir, relevantFiles);
			}
		}
		
		@Override
		protected ArrayList<File> doInBackground(File... params) 
		{
			ArrayList<File> dirList = new ArrayList<File>();
			lookForRelevantFiles(params[0],	dirList);
			return dirList;
		}
		
		@Override
		protected void onPostExecute (ArrayList<File> result)
		{
			m_adapter = new OpenAdapter(mm_activity, result.toArray(new 
					File[result.size()]));
			m_progressWheel.setVisibility(View.GONE);
			m_progressText.setVisibility(View.GONE);
			setListAdapter(m_adapter);
			m_dataLoaded = true;
			
			Log.i("T", "Task finished");
		}
		
		@Override
		protected void onCancelled (ArrayList<File> result)
		{
			Log.i("T", "Task cancelled");
		}
	}
	
	
	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_open);
		
		m_inflater = (LayoutInflater)getSystemService(Context
				.LAYOUT_INFLATER_SERVICE);
		
		m_progressWheel = (ProgressBar)findViewById(R.id.progress_wheel);
		m_progressText = (TextView)findViewById(R.id.progress_text);
		
		m_dirFindAsyncTask = new DirFindAsyncTask(this);
		
		if(!m_dataLoaded)
			updateList();
	}
	
	@Override
	protected void onSaveInstanceState (Bundle outState)
	{
		m_dirFindAsyncTask.cancel(true);
		super.onSaveInstanceState(outState);
	}
	
	/**
	 * Updates the file list
	 */
	private void updateList()
	{
		m_dirFindAsyncTask.execute(Environment.getExternalStorageDirectory());
	}
}
