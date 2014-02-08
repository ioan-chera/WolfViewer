package com.ichera.wolfviewer;

import java.io.File;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ichera.wolfviewer.document.Document;
import com.ichera.wolfviewer.document.LevelContainer;
import com.ichera.wolfviewer.ui.HXScrollView;
import com.ichera.wolfviewer.ui.ScrollViewListener;
import com.ichera.wolfviewer.ui.VXScrollView;

/**
 * Startup activity
 * @author ioan
 *
 */
public class MainActivity extends ActionBarActivity implements 
AdapterView.OnItemClickListener, ScrollViewListener, View.OnTouchListener,
View.OnClickListener
{
//	@SuppressWarnings("unused")
	private static final String TAG = "MainActivity";
	
	private static final int REQUEST_OPEN_WOLF = 1;
	private static final String EXTRA_CURRENT_PATH = "currentPath";
	private static final String EXTRA_CURRENT_LEVEL = "currentLevel";
	
	// saved
	private File mCurrentPath;
	
	// generated
	private Document mDocument;
	
	// nonsaved
//	private GridView mGridView;
	private GridLayout mGridLayout;
	private VXScrollView mVerticalScroll;
	private HXScrollView mHorizontalScroll;
	private TextView mLevelNameLabel;
	private ImageButton mPrevLevelButton;
	private ImageButton mNextLevelButton;
	
	private Rect mViewRect;
	private int mTileSize;
	private ImageView[][] mTileViews;
	private int mCurrentLevel;
	
	// sound engine
	private AudioTrack mTrack;
	
	// static
	private static int sFloorColour = Palette.WL6[25];

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGridLayout = (GridLayout)findViewById(R.id.grid_layout);
        mVerticalScroll = (VXScrollView)findViewById(R.id.vertical_scroll);
        mHorizontalScroll = (HXScrollView)findViewById(R.id.horizontal_scroll);
        mLevelNameLabel = (TextView)findViewById(R.id.level_name_label);
        mPrevLevelButton = (ImageButton)findViewById(R.id.prev_level_button);
        mNextLevelButton = (ImageButton)findViewById(R.id.next_level_button);
        
        mPrevLevelButton.setOnClickListener(this);
        mNextLevelButton.setOnClickListener(this);
        
        mHorizontalScroll.setScrollingEnabled(false);
        mVerticalScroll.setOnTouchListener(this);
        mHorizontalScroll.setScrollViewListener(this);
        mVerticalScroll.setScrollViewListener(this);
//        mGridView = (GridView)findViewById(R.id.grid);
//        mGridView.setOnItemClickListener(this);
//        mGridView.setSoundEffectsEnabled(false);
        findViewById(android.R.id.content).setBackgroundColor(sFloorColour);
        
        mDocument = Document.getInstance();
//        mGridView.setAdapter(new GridAdapter());
        
        if(savedInstanceState != null)
        {
        	String value = savedInstanceState.getString(EXTRA_CURRENT_PATH);
        	if(value != null)
        		mCurrentPath = new File(value);
        	mCurrentLevel = savedInstanceState.getInt(EXTRA_CURRENT_LEVEL);
        }
        
        Global.initialize(this);
        
        mTileSize = (int)(48 * Global.getScale());
        
        updateGridLayout();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
    	if(mCurrentPath != null)
    		outState.putString(EXTRA_CURRENT_PATH, mCurrentPath.getPath());
    	outState.putInt(EXTRA_CURRENT_LEVEL, mCurrentLevel);
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
    	
    	if(mCurrentLevel < 0)
    		mCurrentLevel = 0;
    	else if(mCurrentLevel >= LevelContainer.NUMMAPS)
    		mCurrentLevel = LevelContainer.NUMMAPS - 1;
    	
    	mLevelNameLabel.setText(mDocument.getLevels().getLevelName(mCurrentLevel));
    	short[][] level = mDocument.getLevels().getLevel(mCurrentLevel);
    	
    	short[] wallplane = level[0];
    	
    	int x, y, texture, cell;
    	ImageView iv;
    	GridLayout.LayoutParams gllp;
    	    	
    	if(mTileViews == null)
    		mTileViews = new ImageView[LevelContainer.MAPSIZE][LevelContainer.MAPSIZE];
    	
    	for(x = 0; x < LevelContainer.MAPSIZE; ++x)
    		for(y = 0; y < LevelContainer.MAPSIZE; ++y)
    		{
    			if(mTileViews[y][x] == null)
    			{
    				iv = new ImageView(this);
        			gllp = new GridLayout.LayoutParams();
        			gllp.width = mTileSize;
    				gllp.height = mTileSize;
    				gllp.columnSpec = GridLayout.spec(x);
    				gllp.rowSpec = GridLayout.spec(y);
    				iv.setLayoutParams(gllp);
    				iv.setBackgroundColor(sFloorColour);
    				mTileViews[y][x] = iv;
    				iv.setId(LevelContainer.MAPSIZE * y + x);
    				mGridLayout.addView(iv);
    			}
    			else
    				iv = mTileViews[y][x];
				
//				if(x >= xmin && x <= xmax && y >= ymin && y <= ymax)
				{
					cell = wallplane[y * LevelContainer.MAPSIZE + x];
	    			texture = 2 * (cell - 1);
	    			if(texture >= 0 && texture < mDocument.getVSwap().getSpriteStart())
	    			{
	    				iv.setImageBitmap(mDocument.getVSwap().getTextureBitmap(texture));
	    			}
					else
						iv.setImageBitmap(null);
				}
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

	@Override
	public void onScrollChanged(FrameLayout scrollView, int px, int py, int poldx,
			int poldy) 
	{
//		if(scrollView == mHorizontalScroll || scrollView == mVerticalScroll)
//		{
//			// deltay is 0 OR deltax is 0
//			int oldx = poldx / mTileSize;
//			int x = px / mTileSize;
//			int oldy = poldy/ mTileSize;
//			int y = py / mTileSize;
//			int i, j;
//			int cmin, cmax, vmin, vmax;
//			if(x != oldx)
//			{
//				cmin = py / mTileSize;
//				cmax = (py + mVerticalScroll.getHeight()) / mTileSize;
//				// tiles disappeared from view
//				for(i = cmin; i <= cmax; ++i)
//				{
//					vmin = Math.min(oldx, x);
//					vmax = Math.max(oldx, x);
//					for(j = vmin; j <= vmax; ++j)
//					{
//						mTileViews[i][j].setImageDrawable(null);
//					}
//					x = (px + mHorizontalScroll.getWidth()) / mTileSize;
//					oldx = (poldx + mHorizontalScroll.getWidth()) / mTileSize;
//					vmin = Math.min(oldx, x);
//					vmax = Math.max(oldx, x);
//					for(j = vmin; j <= vmax; ++j)
//					{
//						mTileViews[i][j].setImageDrawable(null);
//					}
//				}
//				
//			}
//			else if(y != oldy)
//			{
//				cmin = px / mTileSize;
//				cmax = (px + mHorizontalScroll.getWidth()) / mTileSize;
//				for(i = cmin; i <= cmax; ++i)
//				{
//					vmin = Math.min(oldy, y);
//					vmax = Math.max(oldy, y);
//					for(j = vmin; j <= vmax; ++j)
//					{
//						mTileViews[j][i].setImageDrawable(null);
//					}
//					y = (py + mVerticalScroll.getHeight()) / mTileSize;
//					oldy = (poldy + mVerticalScroll.getHeight()) / mTileSize;
//					vmin = Math.min(oldy, y);
//					vmax = Math.max(oldy, y);
//					for(j = vmin; j <= vmax; ++j)
//					{
//						mTileViews[j][i].setImageDrawable(null);
//					}
//				}
//			}
//		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) 
	{
		if(v == mVerticalScroll)
		{
			// LOL trickery
			mHorizontalScroll.setScrollingEnabled(true);
			mHorizontalScroll.dispatchTouchEvent(event);
			mHorizontalScroll.setScrollingEnabled(false);
			return false;
		}
		return false;
	}

	@Override
	public void onClick(View v) 
	{
		if(v == mPrevLevelButton && mCurrentLevel > 0)
		{
			mCurrentLevel--;
			updateGridLayout();
			return;
		}
		if(v == mNextLevelButton && mCurrentLevel < LevelContainer.NUMMAPS - 1)
		{
			mCurrentLevel++;
			updateGridLayout();
			return;
		}
	}
}
