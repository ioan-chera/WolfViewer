package com.ichera.wolfviewer;

import java.io.File;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * Startup activity
 * @author ioan
 *
 */
public class MainActivity extends ActionBarActivity implements 
AdapterView.OnItemClickListener
{
	@SuppressWarnings("unused")
	private static final String TAG = "MainActivity";
	
	private static final int REQUEST_OPEN_WOLF = 1;
	private static final String EXTRA_CURRENT_PATH = "currentPath";
	private static final String EXTRA_HAS_VSWAP_FILE = "hasVswapFile";
	
	// saved
	private File m_currentPath;
	private File m_vswapFile;
	
	// generated
	private VSwapContainer m_vswap;
	
	// nonsaved
	private GridView m_gridView;
	
	// sound engine
	private AudioTrack m_track;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_gridView = (GridView)findViewById(R.id.grid);
        m_gridView.setOnItemClickListener(this);
        m_gridView.setSoundEffectsEnabled(false);
        findViewById(android.R.id.content).setBackgroundColor(Palette.WL6[25]);
        
        if(savedInstanceState != null)
        {
        	String value = savedInstanceState.getString(EXTRA_CURRENT_PATH);
        	if(value != null)
        		m_currentPath = new File(value);
        	boolean has = savedInstanceState.getBoolean(EXTRA_HAS_VSWAP_FILE);
        	if(m_currentPath != null && has)
        	{
        		updateTextureView();
        	}
        }
        
        Global.initialize(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
    	if(m_currentPath != null)
    		outState.putString(EXTRA_CURRENT_PATH, m_currentPath.getPath());
    	if(m_vswapFile != null)
    		outState.putBoolean(EXTRA_HAS_VSWAP_FILE, true);
    	super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    private void updateTextureView()
    {
		m_vswapFile = new File(m_currentPath, "vswap.wl6");
		m_vswap = new VSwapContainer();
		m_vswap.loadFile(m_vswapFile);
		m_gridView.setAdapter(new GridAdapter());
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, 
    		Intent data)
    {
    	super.onActivityResult(requestCode, resultCode, data);
    	if(requestCode == REQUEST_OPEN_WOLF && resultCode == RESULT_OK)
    	{
    		m_currentPath = new File(data.getStringExtra(OpenActivity.EXTRA_CURRENT_PATH));
    		updateTextureView();
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
    		if(m_currentPath != null)
    			intent.putExtra(OpenActivity.EXTRA_CURRENT_PATH, 
    					m_currentPath.getPath());
    		startActivityForResult(intent, REQUEST_OPEN_WOLF);
    	}
    		break;
    	default:
    		break;
    	}
    	return super.onOptionsItemSelected(item);
    }
    
    private class GridAdapter extends BaseAdapter
    {

		@Override
		public int getCount() 
		{
			return m_vswap != null ? m_vswap.getNumChunks() : null;
		}

		@Override
		public Object getItem(int position) 
		{
			return m_vswap != null ? m_vswap.getPage(position) : null;
		}

		@Override
		public long getItemId(int position) 
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) 
		{
			ImageView iv;
			if(convertView == null)
			{
				iv = new ImageView(MainActivity.this);
				iv.setLayoutParams(new AbsListView.LayoutParams(
						(int)(64 * Global.getScale()), (int)(64 * Global.getScale())));
				
			}
			else
				iv = (ImageView)convertView;
			
			if(position < m_vswap.getSpriteStart())
				iv.setImageBitmap(m_vswap.getTextureBitmap(position));
			else if(position < m_vswap.getSoundStart())
				iv.setImageBitmap(m_vswap.getSpriteBitmap(position));
			else
				iv.setImageResource(R.drawable.ic_action_play);
			return iv;
		}
    	
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) 
	{
		if(m_vswap == null)
			return;
		if(position >= m_vswap.getSoundStart())
		{
			try
			{
				if(m_track != null)
				{
					m_track.release();
					m_track = null;
				}
				m_track = new AudioTrack(AudioManager.STREAM_MUSIC, 
							Global.SOUND_SAMPLE_RATE_HZ, AudioFormat.CHANNEL_OUT_MONO, 
							AudioFormat.ENCODING_PCM_8BIT, m_vswap.getPage(position).length, 
							AudioTrack.MODE_STATIC);
				
				m_track.write(m_vswap.getPage(position), 0, m_vswap.getPage(position).length);
				m_track.play();
			}
			catch(IllegalStateException e)
			{
				Global.showErrorAlert(this, "Can't play sound", 
						"Unavailable audio player");
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if(m_track != null)
			m_track.release();
	}
}
