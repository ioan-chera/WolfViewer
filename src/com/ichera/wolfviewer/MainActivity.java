package com.ichera.wolfviewer;

import java.io.File;

import android.content.Intent;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.ichera.wolfviewer.document.Document;
import com.ichera.wolfviewer.document.LevelContainer;

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
	
	// saved
	private File mCurrentPath;
	
	// generated
	private Document mDocument;
	
	// nonsaved
//	private GridView mGridView;
	private GridLayout mGridLayout;
	
	// sound engine
	private AudioTrack mTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGridLayout = (GridLayout)findViewById(R.id.grid_layout);
//        mGridView = (GridView)findViewById(R.id.grid);
//        mGridView.setOnItemClickListener(this);
//        mGridView.setSoundEffectsEnabled(false);
        findViewById(android.R.id.content).setBackgroundColor(Palette.WL6[25]);
        
        mDocument = Document.getInstance();
//        mGridView.setAdapter(new GridAdapter());
        
        if(savedInstanceState != null)
        {
        	String value = savedInstanceState.getString(EXTRA_CURRENT_PATH);
        	if(value != null)
        		mCurrentPath = new File(value);
        }
        
        updateGridLayout();
        
        Global.initialize(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
    	if(mCurrentPath != null)
    		outState.putString(EXTRA_CURRENT_PATH, mCurrentPath.getPath());
    	super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, 
    		Intent data)
    {
    	super.onActivityResult(requestCode, resultCode, data);
    	if(requestCode == REQUEST_OPEN_WOLF && resultCode == RESULT_OK)
    	{
    		mCurrentPath = new File(data.getStringExtra(OpenActivity.EXTRA_CURRENT_PATH));
    		if(!mDocument.loadFromDirectory(mCurrentPath))
    			Global.showErrorAlert(this, "Error", "Cannot open document " + mCurrentPath);
    		else
    			updateGridLayout();
//    			((BaseAdapter)mGridView.getAdapter()).notifyDataSetChanged();
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
    		if(mCurrentPath != null)
    			intent.putExtra(OpenActivity.EXTRA_CURRENT_PATH, 
    					mCurrentPath.getPath());
    		startActivityForResult(intent, REQUEST_OPEN_WOLF);
    	}
    		break;
    	default:
    		break;
    	}
    	return super.onOptionsItemSelected(item);
    }
    
    private void updateGridLayout()
    {
    	if(!mDocument.isLoaded())
    		return;
    	
    	mGridLayout.removeAllViews();
    	
    	short[][] level = mDocument.getLevels().getLevel(9);
    	
    	short[] wallplane = level[0];
    	
    	int x, y, texture, cell;
    	ImageView iv;
    	GridLayout.LayoutParams gllp;
    	for(x = 0; x < LevelContainer.MAPSIZE; ++x)
    		for(y = 0; y < LevelContainer.MAPSIZE; ++y)
    		{
    			iv = new ImageView(this);
    			gllp = new GridLayout.LayoutParams();
    			gllp.width = (int)(64 * Global.getScale());
				gllp.height = (int)(64 * Global.getScale());
				gllp.columnSpec = GridLayout.spec(x);
				gllp.rowSpec = GridLayout.spec(y);
				iv.setLayoutParams(gllp);
				iv.setBackgroundColor(Color.BLACK);
				
				cell = wallplane[x * LevelContainer.MAPSIZE + y];
				if(cell > 0)
				{
	    			texture = 2 * (cell - 1);
	    			if(texture < mDocument.getVSwap().getSpriteStart())
	    			{
	    				iv.setImageBitmap(mDocument.getVSwap().getTextureBitmap(texture));
	    			}
				}
    			
    			mGridLayout.addView(iv);
    		}
    }
    
    /**
     * Adapter for the grid view
     * @author ioan
     *
     */
    private class GridAdapter extends BaseAdapter
    {

		@Override
		public int getCount() 
		{
			return mDocument.isLoaded() ? mDocument.getVSwap().getNumChunks() : 
				0;
		}

		@Override
		public Object getItem(int position) 
		{
			return mDocument.isLoaded() ? mDocument.getVSwap().getPage(position) 
					: null;
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
			
			if(position < mDocument.getVSwap().getSpriteStart())
				iv.setImageBitmap(mDocument.getVSwap()
						.getTextureBitmap(position));
			else if(position < mDocument.getVSwap().getSoundStart())
				iv.setImageBitmap(mDocument.getVSwap()
						.getSpriteBitmap(position));
			else
				iv.setImageResource(R.drawable.ic_action_play);
			return iv;
		}
    	
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) 
	{
		if(!mDocument.isLoaded())
			return;
		if(position >= mDocument.getVSwap().getSoundStart())
		{
			try
			{
				if(mTrack != null)
				{
					mTrack.release();
					mTrack = null;
				}
				mTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 
							Global.SOUND_SAMPLE_RATE_HZ, AudioFormat.CHANNEL_OUT_MONO, 
							AudioFormat.ENCODING_PCM_8BIT, 
							mDocument.getVSwap().getPage(position).length, 
							AudioTrack.MODE_STATIC);
				
				mTrack.write(mDocument.getVSwap().getPage(position), 0, 
						mDocument.getVSwap().getPage(position).length);
				mTrack.play();
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
		if(mTrack != null)
			mTrack.release();
		mGridLayout.removeAllViews();
	}
}
