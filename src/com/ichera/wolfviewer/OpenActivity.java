package com.ichera.wolfviewer;

import java.io.File;
import java.io.FilenameFilter;
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
	
	private OpenAdapter			m_adapter;
	private File				m_currentPath;
	private Stack<FolderState>	m_folderStack;
	private TextView			m_currentPathLabel;
	private Button				m_openButton;
	private Button				m_cancelButton;
	private ListView			m_listView;
	
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
		m_currentPathLabel = (TextView)findViewById(R.id.current_folder);
		m_openButton = (Button)findViewById(R.id.open_button);
		m_cancelButton = (Button)findViewById(R.id.cancel_button);
		m_listView = (ListView)findViewById(R.id.list);
		
		m_openButton.setOnClickListener(this);
		m_cancelButton.setOnClickListener(this);
		
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
					m_folderStack = new Stack<OpenActivity.FolderState>();
					for(int i = pathHistory.length - 1; i >= 0; --i)
					{
						FolderState fs = new FolderState();
						fs.path = new File(pathHistory[i]);
						fs.listInstanceState = stateHistory[i];
						m_folderStack.push(fs);
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
					m_currentPath = new File(value);
			}
		}
		else
			m_currentPath = new File(savedValue);		
		if(m_currentPath == null)
			m_currentPath = Environment.getExternalStorageDirectory();
		
		
		m_adapter = new OpenAdapter(m_currentPath);
		m_listView.setAdapter(m_adapter);
		m_listView.setOnItemClickListener(this);
	}
	
	@Override
	protected void onSaveInstanceState (Bundle outState)
	{
		if(m_folderStack != null && m_folderStack.size() > 0)
		{
			String[] pathArray = new String[m_folderStack.size()];
			Parcelable[] stateArray = new Parcelable[m_folderStack.size()];
			for(int i = 0; i < pathArray.length; ++i)
			{
				FolderState fs = m_folderStack.pop();
				pathArray[i] = fs.path.getPath();
				stateArray[i] = fs.listInstanceState;
			}
			outState.putStringArray(EXTRA_PATH_HISTORY, pathArray);
			outState.putParcelableArray(EXTRA_STATE_HISTORY, stateArray);
		}
		outState.putString(EXTRA_CURRENT_PATH, m_currentPath.getPath());
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onBackPressed()
	{
		if(m_folderStack != null && m_folderStack.size() > 0)
		{
			FolderState fs = m_folderStack.pop();
			m_currentPath = fs.path;
			m_adapter.setFileList(m_currentPath);
			m_listView.onRestoreInstanceState(fs.listInstanceState);
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
		private File[]	m_fileList;
				
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
			if(m_fileList != files)
			{
				m_currentPathLabel.setText(dir.getPath());
				m_fileList = files;
				if(notify)
					notifyDataSetChanged();
			}
		}
		
		@Override
		public int getCount() 
		{
			return m_fileList.length;
		}

		@Override
		public Object getItem(int position) 
		{
			return m_fileList[position];
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
			
			if(m_fileList[position].isDirectory())
				item.setTypeface(Typeface.DEFAULT_BOLD);
			else
				item.setTypeface(Typeface.DEFAULT);
			item.setText(m_fileList[position].getName());
			
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
		File dir =  (File)m_adapter.getItem(position);
		if(dir.isDirectory())
		{
			if(m_folderStack == null)
				m_folderStack = new Stack<FolderState>();
			FolderState fs = new FolderState();
			fs.listInstanceState = m_listView.onSaveInstanceState();
			fs.path = m_currentPath;
			m_folderStack.push(fs);
			m_currentPath = dir;
			m_adapter.setFileList(m_currentPath);
		}
	}

	@Override
	public void onClick(View v) 
	{
		if(v == m_cancelButton)
		{
			setResult(RESULT_CANCELED);
			finish();
		}
		else if(v == m_openButton)
		{
			boolean invalid = false;
			File[] wl6files = m_currentPath.listFiles(new FilenameFilter() 
			{
				@Override
				public boolean accept(File dir, String filename) 
				{
					for(String name : Global.getWolfFileNames())
					{
						if(name.compareToIgnoreCase(filename) == 0)
							return true;
					}
					return false;
				}
			});
			if(wl6files == null)
				invalid = true;
			
			if(wl6files.length != Global.getWolfFileNames().length)
				invalid = true;
			
			if(invalid)
			{
				Global.showErrorAlert(this, "Cannot open", 
						"Current folder has no Wolfenstein data.");
			}
			else
			{
				Intent data = new Intent();
				data.putExtra(EXTRA_CURRENT_PATH, m_currentPath.getPath());
				setResult(RESULT_OK, data);
				finish();
			}
		}
	}
}
