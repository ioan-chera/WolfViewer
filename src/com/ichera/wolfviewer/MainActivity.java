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

import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ichera.wolfviewer.document.Document;
import com.ichera.wolfviewer.document.LevelContainer;
import com.ichera.wolfviewer.ui.HXScrollView;
import com.ichera.wolfviewer.ui.ScrollViewListener;
import com.ichera.wolfviewer.ui.VXScrollView;
import com.ichera.wolfviewer.ui.VisibilityGrid;

/**
 * Startup activity
 * @author ioan
 *
 */
public class MainActivity extends ActionBarActivity implements 
ScrollViewListener, View.OnTouchListener, View.OnClickListener, 
VisibilityGrid.Delegate
{
	static final String TAG = "MainActivity";
	
	private static final int REQUEST_OPEN_WOLF = 1;
	private static final String EXTRA_CURRENT_PATH = "currentPath";
	private static final String EXTRA_CURRENT_LEVEL = "currentLevel";
	private static final String EXTRA_SCROLL_X = "scrollX";
	private static final String EXTRA_SCROLL_Y = "scrollY";
	
	// saved
	private File mCurrentPath;
	
	// generated
	private Document mDocument;
	
	// nonsaved
//	private GridView mGridView;
	private RelativeLayout mGridLayout;
	private VXScrollView mVerticalScroll;
	private HXScrollView mHorizontalScroll;
	private ProgressBar mProgressIndicator;
	private TextView mProgressInfoLabel;
	
	private Rect mViewRect;
	private int mTileSize;
	private ImageView[][] mTileViews;
	private int mCurrentLevel;
	private Point mViewportSize;
	private VisibilityGrid mVisGrid;
	
	// workers
	private DocumentLoadAsyncTask mDocumentLoadAsyncTask;
	
	// sound engine
	private AudioTrack mTrack;
	
	// static
	private static int sFloorColour = Palette.WL6[25];

    @Override
    protected void onCreate(final Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mGridLayout = (RelativeLayout)findViewById(R.id.grid_layout);
        mVerticalScroll = (VXScrollView)findViewById(R.id.vertical_scroll);
        mHorizontalScroll = (HXScrollView)findViewById(R.id.horizontal_scroll);
        mProgressIndicator = (ProgressBar)findViewById(R.id.progress_indicator);
        mProgressInfoLabel = (TextView)findViewById(R.id.progress_info_label);
        
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
        mGridLayout.getLayoutParams().width = 
    			mGridLayout.getLayoutParams().height = LevelContainer.MAPSIZE *
    			mTileSize;
        
        
        ViewTreeObserver observer = mVerticalScroll.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() 
        {
        	// Can't get rid of warning otherwise. Can't use the recommended
        	// function here
            @SuppressWarnings("deprecation")
			@Override
            public void onGlobalLayout() 
            {
            	mVerticalScroll.getViewTreeObserver()
            			.removeGlobalOnLayoutListener(this);
            	mViewportSize = new Point(mVerticalScroll.getMeasuredWidth(), 
            			mVerticalScroll.getMeasuredHeight());
            	if(savedInstanceState != null)
                {
                	Log.i(TAG, "Recovered: " + savedInstanceState.getInt(EXTRA_SCROLL_X) + " " + savedInstanceState.getInt(EXTRA_SCROLL_Y));
                	mHorizontalScroll.scrollTo(savedInstanceState.getInt(EXTRA_SCROLL_X), 0);
                	mVerticalScroll.scrollTo(0, savedInstanceState.getInt(EXTRA_SCROLL_Y));
                }
            	updateGridLayout();
            }
        });
        
        
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
    	if(mCurrentPath != null)
    		outState.putString(EXTRA_CURRENT_PATH, mCurrentPath.getPath());
    	outState.putInt(EXTRA_CURRENT_LEVEL, mCurrentLevel);
    	outState.putInt(EXTRA_SCROLL_X, mHorizontalScroll.getScrollX());
    	outState.putInt(EXTRA_SCROLL_Y, mVerticalScroll.getScrollY());
    	
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
    		
    		if(mDocumentLoadAsyncTask != null)
    			Global.showErrorAlert(this, "", "A document is currently opening.");
    		else
    		{
    			mDocumentLoadAsyncTask = new DocumentLoadAsyncTask();
    			mDocumentLoadAsyncTask.execute(mCurrentPath);
    		}
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
    		return true;
    	}
    	case R.id.action_prev:
			mCurrentLevel--;
			updateGridLayout();
			return true;
    	case R.id.action_next:
			mCurrentLevel++;
			updateGridLayout();
			return true;
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
    	
    	getSupportActionBar().setTitle(mDocument.getLevels().getLevelName(mCurrentLevel));
    	
    	int ceilingColour = Palette.WL6[LevelContainer.getCeilingColours()[mCurrentLevel]];
    	
    	
    	
    	
    	mGridLayout.setBackgroundColor(ceilingColour);
    	
    	Point viewportPosition = new Point(mHorizontalScroll.getScrollX(), 
    			mVerticalScroll.getScrollY());
    	Log.i(TAG, "Point: " + viewportPosition);
    	Rect viewportRect = new Rect(viewportPosition.x, 
    			viewportPosition.y, viewportPosition.x + mViewportSize.x, 
    			viewportPosition.y + mViewportSize.y);
    	mGridLayout.removeAllViews();
    	if(mVisGrid == null)
    		mVisGrid = new VisibilityGrid();
    	
    	mVisGrid.create(new Point(LevelContainer.MAPSIZE * mTileSize, 
    			LevelContainer.MAPSIZE * mTileSize), viewportRect, mTileSize, this);
    	
    }
    
    @Override
    public void clearVisTiles()
    {
    	if(mTileViews == null)
    		mTileViews = new ImageView[LevelContainer.MAPSIZE][LevelContainer.MAPSIZE];
    	else
    		for(int i = 0; i < LevelContainer.MAPSIZE; ++i)
    			for(int j = 0; j < LevelContainer.MAPSIZE; ++j)
    				mTileViews[i][j] = null;
    }

	@Override
	public void createVisTile(int i, int j) 
	{
		int ms = LevelContainer.MAPSIZE - 1;
		if(!Global.inBounds(i, 0, ms) || !Global.inBounds(i, 0, ms))
		{
//			Log.d(TAG, "Refused create " + i + " " + j);
			return;
		}
		mTileViews[i][j] = new ImageView(this);
		RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams(
				mTileSize, mTileSize);
		rllp.leftMargin = j * mTileSize;
		rllp.topMargin = i * mTileSize;
		mTileViews[i][j].setLayoutParams(rllp);
		mGridLayout.addView(mTileViews[i][j]);
		
		updateGraphics(mTileViews[i][j], j, i);
	}

	@Override
	public void moveVisTile(int i1, int j1, int i2, int j2) 
	{
		int ms = LevelContainer.MAPSIZE - 1;
		if(!Global.inBounds(i2, 0, ms) || !Global.inBounds(j2, 0, ms))
		{
//			Log.d(TAG, "Refused move " + i1 + " " + j1 + " " + i2 + " " + j2);
			return;
		}
		ImageView iv = null;
		if(Global.inBounds(i1, 0, ms) && Global.inBounds(j1, 0, ms))
			iv= mTileViews[i1][j1];
		// let's be careful, okay?
		if(iv == null)
		{
			// either just create or update
			iv = mTileViews[i2][j2];
			if(iv == null)
				createVisTile(i2, j2);
			else
				updateGraphics(iv, j2, i2);
		}
		else
		{
			// delete or move
			ImageView iv2 = mTileViews[i2][j2];
			if(iv2 == null)
			{
				RelativeLayout.LayoutParams rllp = (RelativeLayout.LayoutParams)
						iv.getLayoutParams();
				rllp.leftMargin = j2 * mTileSize;
				rllp.topMargin = i2 * mTileSize;
				mTileViews[i1][j1] = null;
				mTileViews[i2][j2] = iv;
				
//				mGridLayout.invalidate(new Rect(rllp.leftMargin, rllp.topMargin, 
//						rllp.leftMargin + mTileSize, rllp.topMargin + mTileSize));
				updateGraphics(iv, j2, i2);
			}
			else
			{
				// delete the old one
				mGridLayout.removeView(iv);
				mTileViews[i1][j1] = null;
				updateGraphics(iv2, j2, i2);
			}
		}
	}
	
	@Override
	public void finalizeMoveVisTiles()
	{
		mGridLayout.requestLayout();
	}
	
	private void updateGraphics(ImageView iv, int x, int y)
	{
		short[][] level = mDocument.getLevels().getLevel(mCurrentLevel);
    	
    	short[] wallplane = level[0];
    	short[] actorplane = level[1];
		
    	int texture;
		int cell = wallplane[y * LevelContainer.MAPSIZE + x];
		if(cell >= 90 && cell <= 100 && cell % 2 == 0)
		{
			iv.setImageResource(R.drawable.door_vertical);
		}
		else if(cell >= 91 && cell <= 101 && cell % 2 == 1)
		{
			iv.setImageResource(R.drawable.door_horizontal);
		}
		else
		{
			texture = 2 * (cell - 1);
			if(texture >= 0 && texture < mDocument.getVSwap().getSpriteStart())
			{
				iv.setImageBitmap(mDocument.getVSwap().getWallBitmap(texture));
			}
			else
			{
				cell = Global.getActorSpriteMap().get(
						actorplane[y * LevelContainer.MAPSIZE + x], -1);
				if(cell == -1)
					iv.setImageBitmap(null);
				else
					iv.setImageBitmap(mDocument.getVSwap().getSpriteBitmap(cell));
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
	public void onScrollChanged(FrameLayout scrollView, int x, int y, int oldx,
			int oldy) 
	{
		if(scrollView == mHorizontalScroll || scrollView == mVerticalScroll)
		{
			if(mDocument == null || !mDocument.isLoaded())
				return;
			if(mVisGrid == null)
				return;
			
			oldx /= mTileSize;
			x /= mTileSize;
			oldy /= mTileSize;
			y /= mTileSize;
			
			x = Global.boundValue(x, 0, LevelContainer.MAPSIZE - 1);
			y = Global.boundValue(y, 0, LevelContainer.MAPSIZE - 1);
			oldx = Global.boundValue(oldx, 0, LevelContainer.MAPSIZE - 1);
			oldy = Global.boundValue(oldy, 0, LevelContainer.MAPSIZE - 1);
			
			if(x != oldx)
				mVisGrid.columnUpdate(oldx, x);
			else if(y != oldy)
				mVisGrid.rowUpdate(oldy, y);
		}
		
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
	
	}
	
	private class DocumentLoadAsyncTask extends AsyncTask<File, String, Boolean>
	{
		@Override
		protected void onPreExecute()
		{
			mProgressIndicator.setVisibility(View.VISIBLE);
			mProgressInfoLabel.setVisibility(View.VISIBLE);
			mProgressInfoLabel.setText("");
		}
		
		@Override
		protected Boolean doInBackground(File... params) 
		{
			return mDocument.loadFromDirectory(params[0], new RunnableArg<String>() {
				@Override
				public void run() 
				{
					publishProgress(mArgs);
				}
			});
		}
		
		@Override
		protected void onProgressUpdate(String... values)
		{
			mProgressInfoLabel.setText(values[0]);
		}
		
		@Override
		protected void onPostExecute(Boolean result)
		{
			mDocumentLoadAsyncTask = null;
			mProgressIndicator.setVisibility(View.GONE);
			mProgressInfoLabel.setVisibility(View.GONE);
			if(result)
				updateGridLayout();
			else
				Global.showErrorAlert(MainActivity.this, 
						"", "Can't open document " + mCurrentPath);
		}
	}

}
