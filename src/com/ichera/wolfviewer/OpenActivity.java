package com.ichera.wolfviewer;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Activity to open files from file system
 * @author ioan
 *
 */
public class OpenActivity extends ListActivity 
{
	private LayoutInflater	m_inflater;
	private OpenAdapter		m_adapter;
	
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
	
	private void lookForRelevantFiles(File file, ArrayList<File> relevantFiles)
	{
		
		if(file.isDirectory())
		{
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
			else
				for(File subdir : subdirs)
					lookForRelevantFiles(subdir, relevantFiles);
		}
	}
	
	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		m_inflater = (LayoutInflater)getSystemService(Context
				.LAYOUT_INFLATER_SERVICE);
		
		updateList();
	}
	
	/**
	 * Updates the file list
	 */
	private void updateList()
	{
		ArrayList<File> dirList = new ArrayList<File>();
		lookForRelevantFiles(Environment.getExternalStorageDirectory(),	
				dirList);
		m_adapter = new OpenAdapter(this, dirList.toArray(new File[dirList
		                                                           .size()]));
		setListAdapter(m_adapter);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
	}	
}
