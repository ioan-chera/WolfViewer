package com.ichera.wolfviewer;

import java.io.File;
import java.util.Stack;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
	public static final String KEY_PATH = "curPath";
	
	private LayoutInflater	m_inflater;
	private OpenAdapter		m_adapter;
	private Stack<File>		m_history;
	private File			m_currentPath;
	
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
			
			File file = mm_fileList[position];
			String text;
			
			if(file.isDirectory())
				text = "<" + file.getName() + ">";
			else
				text = file.getName();
			
			((Holder)item.getTag()).tv.setText(text);
			
			return item;
		}
		
	}
	
	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		m_inflater = (LayoutInflater)getSystemService(Context
				.LAYOUT_INFLATER_SERVICE);
		
		// Load path location
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		String currentPathString = extras.getString(KEY_PATH);
		
		m_history = new Stack<File>();
		
		updateList(new File(currentPathString));
	}
	
	/**
	 * Updates the file list
	 */
	private void updateList(File currentPath, boolean addToHistory)
	{
		if(currentPath.isDirectory() && currentPath.exists())
		{
			if(m_currentPath != null && addToHistory)
				m_history.push(m_currentPath);
			m_currentPath = currentPath;
			m_adapter = new OpenAdapter(this, m_currentPath.listFiles());
			setListAdapter(m_adapter);
		}
	}
	private void updateList(File currentPath)
	{
		updateList(currentPath, true);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		File target = (File)m_adapter.getItem(position);
		
		if(target.isDirectory())
		{
			updateList(target);
		}
		else
		{
			VSwapContainer vswap = new VSwapContainer();
			vswap.loadFile(target);
		}
	}
	
	@Override
	public void onBackPressed()
	{
		if(m_history.size() == 0)
			super.onBackPressed();
		else
			updateList(m_history.pop(), false);
	}
}
