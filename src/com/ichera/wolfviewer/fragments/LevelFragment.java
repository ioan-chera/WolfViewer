package com.ichera.wolfviewer.fragments;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ichera.wolfviewer.Global;
import com.ichera.wolfviewer.Palette;
import com.ichera.wolfviewer.R;
import com.ichera.wolfviewer.document.Document;
import com.ichera.wolfviewer.document.LevelContainer;
import com.ichera.wolfviewer.ui.HXScrollView;
import com.ichera.wolfviewer.ui.ScrollViewListener;
import com.ichera.wolfviewer.ui.VXScrollView;
import com.ichera.wolfviewer.ui.VisibilityGrid;

public class LevelFragment extends SwitchableFragment implements View.OnTouchListener,
ScrollViewListener, VisibilityGrid.Delegate
{
	static final String TAG = "LevelFragment";
	
	private static final String EXTRA_CURRENT_LEVEL = "currentLevel";
	private static final String EXTRA_SCROLL_X = "scrollX";
	private static final String EXTRA_SCROLL_Y = "scrollY";
	private int mCurrentLevel;
	
	// widgets
	private RelativeLayout mGridLayout;
	private VXScrollView mVerticalScroll;
	private HXScrollView mHorizontalScroll;
	
	// display
	private int mTileSize;
	private ImageView[][] mTileViews;
	private Point mViewportSize;
	private VisibilityGrid mVisGrid;
		
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Bundle b = getActualState(savedInstanceState); 
    	if(b != null)
        {
        	mCurrentLevel = b.getInt(EXTRA_CURRENT_LEVEL);
        }
		
		mTileSize = (int)(48 * Global.getScale());
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) 
    {
        inflater.inflate(R.menu.level, menu);
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch(item.getItemId())
    	{
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
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			final Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.fragment_level, container, false);
		
        mGridLayout = (RelativeLayout)v.findViewById(R.id.grid_layout);
        mVerticalScroll = (VXScrollView)v.findViewById(R.id.vertical_scroll);
        mHorizontalScroll = (HXScrollView)v.findViewById(R.id.horizontal_scroll);
        
        mHorizontalScroll.setScrollingEnabled(false);
        mVerticalScroll.setOnTouchListener(this);
        mHorizontalScroll.setScrollViewListener(this);
        mVerticalScroll.setScrollViewListener(this);

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
            	Bundle b = getActualState(savedInstanceState); 
            	if(b != null)
            	{
            		mHorizontalScroll.scrollTo(b.getInt(EXTRA_SCROLL_X), 0);
                	mVerticalScroll.scrollTo(0, b.getInt(EXTRA_SCROLL_Y));
            	}
            	updateGridLayout();
            }
        });
		
		return v;
	}
	
	@Override
	public void saveState(Bundle target) 
	{
		target.putInt(EXTRA_CURRENT_LEVEL, mCurrentLevel);
		target.putInt(EXTRA_SCROLL_X, mHorizontalScroll.getScrollX());
		target.putInt(EXTRA_SCROLL_Y, mVerticalScroll.getScrollY());
	}
		
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		mGridLayout.removeAllViews();
	}
	
	public void updateGridLayout()
    {
		if(mGridLayout == null)
		{
//			mDeferUpdateGridLayout = true;
			// It would be done anyway upon fragment creation
			return;
		}
		Document document = Document.getInstance();
    	if(!document.isLoaded())
    		return;
    	
    	if(mCurrentLevel < 0)
    		mCurrentLevel = 0;
    	else if(mCurrentLevel >= LevelContainer.NUMMAPS)
    		mCurrentLevel = LevelContainer.NUMMAPS - 1;
    	
    	if(getActivity() instanceof ActionBarActivity)
    		((ActionBarActivity)getActivity()).getSupportActionBar()
    		.setTitle(document.getLevels().getLevelName(mCurrentLevel));
    	
    	int ceilingColour = Palette.WL6[LevelContainer
    	                                .getCeilingColours()[mCurrentLevel]];
    	
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
    			LevelContainer.MAPSIZE * mTileSize), viewportRect, mTileSize, 
    			this);
    	
    }

	////////////////////////////////////////////////////////////////////////////
	// View.OnTouchListener
	////////////////////////////////////////////////////////////////////////////
	
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

	////////////////////////////////////////////////////////////////////////////
	// ScrollViewListener
	////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onScrollChanged(FrameLayout scrollView, int x, int y, int oldx,
			int oldy) 
	{
		if(scrollView == mHorizontalScroll || scrollView == mVerticalScroll)
		{
			Document document = Document.getInstance();
			if(document == null || !document.isLoaded())
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
		
	}

	////////////////////////////////////////////////////////////////////////////
	// VisibilityGrid.Delegate
	////////////////////////////////////////////////////////////////////////////
	
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
		if(!Global.inBounds(i, 0, ms) || !Global.inBounds(j, 0, ms))
		{
//			Log.d(TAG, "Refused create " + i + " " + j);
			return;
		}
		if(getActivity() == null)
			return;
		mTileViews[i][j] = new ImageView(getActivity());
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
		Document document = Document.getInstance();
		short[][] level = document.getLevels().getLevel(mCurrentLevel);
    	
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
			if(texture >= 0 && texture < document.getVSwap().getSpriteStart())
			{
				iv.setImageBitmap(document.getVSwap().getWallBitmap(texture));
			}
			else
			{
				cell = Global.getActorSpriteMap().get(
						actorplane[y * LevelContainer.MAPSIZE + x], -1);
				if(cell == -1)
					iv.setImageBitmap(null);
				else
					iv.setImageBitmap(document.getVSwap().getSpriteBitmap(cell));
			}
		}
	}
}
