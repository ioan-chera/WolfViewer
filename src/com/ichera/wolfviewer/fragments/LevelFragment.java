package com.ichera.wolfviewer.fragments;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.ichera.wolfviewer.Global;
import com.ichera.wolfviewer.MainActivity;
import com.ichera.wolfviewer.Palette;
import com.ichera.wolfviewer.R;
import com.ichera.wolfviewer.document.Document;
import com.ichera.wolfviewer.document.LevelContainer;
import com.ichera.wolfviewer.ui.HXScrollView;
import com.ichera.wolfviewer.ui.ScrollViewListener;
import com.ichera.wolfviewer.ui.VXScrollView;
import com.ichera.wolfviewer.ui.VisibilityGrid;

public class LevelFragment extends SwitchableFragment implements View.OnTouchListener,
ScrollViewListener, VisibilityGrid.Delegate, View.OnClickListener, 
LevelContainer.Observer, AdapterView.OnItemClickListener
{
	static final String TAG = "LevelFragment";
	
	private static final String EXTRA_CURRENT_LEVEL = "currentLevel";
	private static final String EXTRA_SCROLL_X = "scrollX";
	private static final String EXTRA_SCROLL_Y = "scrollY";
	private static final String EXTRA_CURRENT_WALL_CHOICE = "currentWallChoice";
	private int mCurrentLevel;
	private int mCurrentWallChoice;
	
	// widgets
	private RelativeLayout mGridLayout;
	private VXScrollView mVerticalScroll;
	private HXScrollView mHorizontalScroll;
	private ListView mWallList;
	private LinearLayout mLeftDrawer;
	
	// display
	private int mTileSize;
	private ImageView[][] mTileViews;
	private Point mViewportSize;
	private VisibilityGrid mVisGrid;
	
	// other data
	private JSONArray mWallChoices;
	private boolean mDeferAddObserver;
	
	// Item click control
	private boolean mPressDown;
	private long mPressDownTimeMilli;
		
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
		
		readWallChoices();
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
        mWallList = (ListView)v.findViewById(R.id.wall_list);
        mLeftDrawer = (LinearLayout)v.findViewById(R.id.left_drawer);
        
        mHorizontalScroll.setScrollingEnabled(false);
        mVerticalScroll.setOnTouchListener(this);
//        mVerticalScroll.setClickable(true);
//        mHorizontalScroll.setOnClickListener(this);
        mHorizontalScroll.setScrollViewListener(this);
        mVerticalScroll.setScrollViewListener(this);
        
        mLeftDrawer.setBackgroundColor(MainActivity.FLOOR_COLOUR);
        mWallList.setAdapter(new WallListAdapter());
        mWallList.setOnItemClickListener(this);
        
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
                	mCurrentWallChoice = b.getInt(EXTRA_CURRENT_WALL_CHOICE);
            	}
            	updateData();
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
		target.putInt(EXTRA_CURRENT_WALL_CHOICE, mCurrentWallChoice);
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		if(Document.getInstance().isLoaded())
		{
			Log.i(TAG, "Adding observer");
			Document.getInstance().getLevels().addObserver(this);
		}
		else
			mDeferAddObserver = true;
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		if(Document.getInstance().isLoaded())
		{
			Log.i(TAG, "Removing observer");
			Document.getInstance().getLevels().removeObserver(this);
		}
	}
		
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		mGridLayout.removeAllViews();
	}
	
	public void updateData()
    {
		if(mGridLayout == null)
			return;	// will be done anyway upon creation
		if(Document.getInstance().isLoaded() && mDeferAddObserver)
		{
			Log.i(TAG, "Adding deferred observer");
			mDeferAddObserver = false;
			Document.getInstance().getLevels().addObserver(this);
		}
		updateGridLayout();
    	((WallListAdapter)mWallList.getAdapter()).notifyDataSetChanged();
    }
	
	private void updateGridLayout()
	{
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
    	                                .getCeilingColour(mCurrentLevel)];
    	
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
			if(event.getActionMasked() == MotionEvent.ACTION_MOVE)
			{
				if(mPressDown && System.currentTimeMillis() - mPressDownTimeMilli > 80)
					mPressDown = false;	// cancel any pressed thing
			}
			if(event.getActionMasked() == MotionEvent.ACTION_UP)
			{
				// More hackery: interception galore
				if(mPressDown)	// not cancelled by scrolling.
				{
					// Get the view from the visible spot
					mPressDown = false;
					if(mWallChoices == null || !Global.inBounds(mCurrentWallChoice, 0, 
							mWallChoices.length() - 1) || !Document.getInstance().isLoaded())
						return false;
					JSONObject obj = mWallChoices.optJSONObject(mCurrentWallChoice);
					if(obj == null)
						return false;
					int x = (int)((event.getX() + mHorizontalScroll.getScrollX()) 
							/ mTileSize);
					int y = (int)((event.getY() + mVerticalScroll.getScrollY())
							/ mTileSize);
					if(Global.inBounds(x, 0, LevelContainer.MAPSIZE - 1) &&
							Global.inBounds(y, 0, LevelContainer.MAPSIZE - 1))
					{
						Document.getInstance().getLevels().setTile(mCurrentLevel, 0, 
								LevelContainer.MAPSIZE * y + x, (short)obj.optInt("id"));
						return true;
					}
				}
			}
			
			// LOL trickery
			mHorizontalScroll.setScrollingEnabled(true);
			mHorizontalScroll.dispatchTouchEvent(event);
			mHorizontalScroll.setScrollingEnabled(false);
			return false;
		}
		else if(v instanceof ImageView)
		{
			mPressDown = true;	
			mPressDownTimeMilli = System.currentTimeMillis();
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
			if(mPressDown && System.currentTimeMillis() - mPressDownTimeMilli > 80)
				mPressDown = false;	// cancel any pressed thing
			
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
	
	@Override
	public void onOverScrolled(FrameLayout scrollView, int scrollX,
			int scrollY, boolean clampedX, boolean clampedY) 
	{
		// unreliable
//		if(scrollView == mHorizontalScroll || scrollView == mVerticalScroll)
//		{
//			mPressDown = false;
//		}
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
		mTileViews[i][j].setId(i * LevelContainer.MAPSIZE + j);
		RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams(
				mTileSize, mTileSize);
		rllp.leftMargin = j * mTileSize;
		rllp.topMargin = i * mTileSize;
		mTileViews[i][j].setLayoutParams(rllp);
//		mTileViews[i][j].setOnClickListener(this);
		mTileViews[i][j].setOnTouchListener(this);
//		mTileViews[i][j].setClickable(true);
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
				mTileViews[i2][j2].setId(i2 * LevelContainer.MAPSIZE + j2);
				
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
//		short[][] level = document.getLevels().getLevel(mCurrentLevel);
    	
//    	short[] wallplane = level[0];
//    	short[] actorplane = level[1];
		
    	int cell = document.getLevels().getTile(mCurrentLevel, 0, x, y);
    	
    	if(!setBitmapFromMapValue(document, iv, cell))
    	{
    		cell = Global.getActorSpriteMap().get(
					document.getLevels().getTile(mCurrentLevel, 1, x, y), -1);
			if(cell == -1)
				iv.setImageBitmap(null);
			else
				iv.setImageBitmap(document.getVSwap().getSpriteBitmap(cell));
    	}
	}
	
	private boolean setBitmapFromMapValue(Document document, ImageView iv, int cell)
	{
		if(cell >= 90 && cell <= 100 && cell % 2 == 0)
		{
			iv.setImageResource(R.drawable.door_vertical);
			return true;
		}
		else if(cell >= 91 && cell <= 101 && cell % 2 == 1)
		{
			iv.setImageResource(R.drawable.door_horizontal);
			return true;
		}
		else
		{
			int texture = 2 * (cell - 1);
			if(texture >= 0 && texture < document.getVSwap().getSpriteStart())
			{
				iv.setImageBitmap(document.getVSwap().getWallBitmap(texture));
				return true;
			}
		}
		return false;
	}
	
	////////////////////////////////////////////////////////////////////////////
	// Wall list adapter
	////////////////////////////////////////////////////////////////////////////
	
	private class WallListAdapter extends BaseAdapter
	{
		private Document mDocument;
		
		WallListAdapter()
		{
			mDocument = Document.getInstance();
		}
		
		@Override
		public int getCount() 
		{
			return mWallChoices != null ? mWallChoices.length() : 0;
		}

		@Override
		public Object getItem(int position) 
		{
			return mWallChoices != null ? mWallChoices.optJSONObject(position) 
					: null;
		}

		@Override
		public long getItemId(int position) 
		{
			if(mWallChoices == null)
				return -1;
			else
			{
				JSONObject object = (JSONObject)getItem(position);
				if(object != null)
					return object.optLong("id");
				else
					return -1;
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) 
		{
			ImageView item;
			if(convertView == null)
			{
				item = new ImageView(getActivity());
				AbsListView.LayoutParams alvlp = new AbsListView.LayoutParams(
						(int)(48 * Global.getScale()), 
						(int)(48 * Global.getScale()));
				item.setLayoutParams(alvlp);
				item.setPadding(0, (int)(5 * Global.getScale()), 0, (int)(5 * Global.getScale()));
			}
			else
				item = (ImageView)convertView;
			int index = (int)getItemId(position);
			if(mCurrentWallChoice == position)
				item.setBackgroundResource(R.drawable.frame_selection);
			else
				item.setBackgroundResource(0);
			if(!mDocument.isLoaded() || !setBitmapFromMapValue(mDocument, item, index))
				item.setImageDrawable(null);
			
			return item;
		}
		
	}
	
	////////////////////////////////////////////////////////////////////////////
	
	void readWallChoices()
	{
		// Called upon creation
		InputStream is = null;
		StringBuilder sb;
		try
		{
			sb = new StringBuilder(5000);
			is = getActivity().getAssets().open("wall_choices.json");
			
			byte[] buffer = new byte[512];
			while(is.read(buffer) > 0)
				sb.append(new String(buffer, "UTF-8"));
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return;
		}
		finally
		{
			if(is != null)
			{
				try
				{
					is.close();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		String jsonString = sb.toString();
		JSONArray array = null;
		try
		{
			array = new JSONArray(jsonString);
		}
		catch(JSONException e)
		{
			e.printStackTrace();
			Global.showErrorAlert(getActivity(), "Internal error", 
					"Failed to read the wall choice list!");
			return;
		}
		if(array != null)
		{
			mWallChoices = array;
			Global.boundValue(mCurrentWallChoice, 0, mWallChoices.length() - 1);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onClick(View v) 
	{
		
		if(v instanceof ImageView)
		{
			if(mWallChoices == null || !Global.inBounds(mCurrentWallChoice, 0, 
					mWallChoices.length() - 1))
				return;
			JSONObject obj = mWallChoices.optJSONObject(mCurrentWallChoice);
			if(obj == null)
				return;
//			Log.i(TAG, "Clicked " + v.getId() % 64 + " " + v.getId() / 64 + " real " + 
//				((RelativeLayout.LayoutParams)v.getLayoutParams()).leftMargin / v.getWidth() + " " + 
//				((RelativeLayout.LayoutParams)v.getLayoutParams()).topMargin / v.getHeight());
			Document.getInstance().getLevels().setTile(mCurrentLevel, 0, v.getId(), 
					(short)obj.optInt("id"));
		}
	}

	@Override
	public void observeLocalChange(int level, int plane, int i, short value)
	{
		int x = i % LevelContainer.MAPSIZE;
		int y = i / LevelContainer.MAPSIZE;
		if(mTileViews[y][x] != null && level == mCurrentLevel)
			updateGraphics(mTileViews[y][x], x, y);
		else if(level == mCurrentLevel)
		{
			mHorizontalScroll.scrollTo(x * mTileSize - mViewportSize.x / 2, 0);
			mVerticalScroll.scrollTo(0, y * mTileSize - mViewportSize.y / 2);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
	{
		if(arg0 == mWallList)
		{
			mCurrentWallChoice = arg2;
			((WallListAdapter)mWallList.getAdapter()).notifyDataSetChanged();
		}
	}

	@Override
	public boolean handleBackButton() 
	{
		if(Document.getInstance().isLoaded() && 
				Global.inBounds(mCurrentLevel, 0, LevelContainer.NUMMAPS - 1)
				&& Document.getInstance().getLevels().hasUndo(mCurrentLevel))
		{
			Document.getInstance().getLevels().undo(mCurrentLevel);
			return true;
		}
		return false;
	}

	
}
