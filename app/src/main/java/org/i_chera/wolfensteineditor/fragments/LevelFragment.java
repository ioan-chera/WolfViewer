package org.i_chera.wolfensteineditor.fragments;

/*
 * Wolfenstein 3D editor for Android
 * Copyright (C) 2015  Ioan Chera
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

import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import org.i_chera.wolfensteineditor.BackButtonHandler;
import org.i_chera.wolfensteineditor.Global;
import org.i_chera.wolfensteineditor.MainActivity;
import org.i_chera.wolfensteineditor.Palette;
import org.i_chera.wolfensteineditor.R;
import org.i_chera.wolfensteineditor.StateSaver;
import org.i_chera.wolfensteineditor.document.Document;
import org.i_chera.wolfensteineditor.document.LevelContainer;
import org.i_chera.wolfensteineditor.fragments.tasks.DocumentLoadAsyncTask;
import org.i_chera.wolfensteineditor.fragments.tasks.DocumentSaveAsyncTask;
import org.i_chera.wolfensteineditor.ui.HXScrollView;
import org.i_chera.wolfensteineditor.ui.ScrollViewListener;
import org.i_chera.wolfensteineditor.ui.VXScrollView;
import org.i_chera.wolfensteineditor.ui.VisibilityGrid;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class LevelFragment extends Fragment implements
        AdapterView.OnItemClickListener,
        BackButtonHandler,
        CompoundButton.OnCheckedChangeListener,
        LevelContainer.Observer,
        ScrollViewListener,
        View.OnClickListener,
        View.OnTouchListener,
        VisibilityGrid.Delegate
{
    static final String TAG = "LevelFragment";

    // State
    private static final String STATE_CURRENT_LEVEL = "currentLevel";
    private static final String STATE_SCROLL_X = "scrollX";
    private static final String STATE_SCROLL_Y = "scrollY";
    private static final String STATE_SCROLL_LOCK = "scrollLock";
    private static final String STATE_CURRENT_WALL_CHOICE = "currentWallChoice";
    private static final String STATE_DRAWER_OPEN = "drawerOpen";
    public static final String ARG_PATH_NAME = "pathName";
    private int mCurrentLevel;
    private int mCurrentWallChoice;
    private File mPath;

    // document
    private Document mDocument;
    private DocumentLoadAsyncTask mAutoloadTask;
    private static DocumentSaveAsyncTask sAutosaveTask; // one global one

    // widgets
    private RelativeLayout mGridLayout;
    private VXScrollView mVerticalScroll;
    private HXScrollView mHorizontalScroll;
    private ListView mWallList;
    private CheckBox mScrollLockCheck;
    private RelativeLayout mCentralContent;
    private ActionBarDrawerToggle mDrawerToggle;
    private ImageView mUndoButton;
    private ImageView mRedoButton;

    // display
    private int mTileSize;
    private ImageView[][] mTileViews;
    private Point mViewportSize;
    private VisibilityGrid mVisGrid;

    // other data
    private JSONArray mWallChoices;

    // Item click control
    private boolean mPressDown;

    // Drawer
    private DrawerLayout mDrawerLayout;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null)
        {
            mCurrentLevel = savedInstanceState.getInt(STATE_CURRENT_LEVEL);
        }
        readArguments();
        if(mPath != null && mDocument == null)
        {
            mDocument = StateSaver.withdrawDocument(mPath);
        }

        setHasOptionsMenu(true);
    }

    private void readArguments()
    {
        if(getArguments() != null)
            mPath = new File(getArguments().getString(ARG_PATH_NAME));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.level, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
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
        mDrawerLayout = (DrawerLayout)v;

        // Static data
        mTileSize = (int)(48 * ((MainActivity)getActivity()).getPixelScale());
        readWallChoices();

        mGridLayout = (RelativeLayout)v.findViewById(R.id.grid_layout);
        mVerticalScroll = (VXScrollView)v.findViewById(R.id.vertical_scroll);
        mHorizontalScroll = (HXScrollView)v.findViewById(R.id.horizontal_scroll);
        mWallList = (ListView)v.findViewById(R.id.wall_list);
        mScrollLockCheck = (CheckBox)v.findViewById(R.id.scroll_lock_check);
        mCentralContent = (RelativeLayout)v.findViewById(R.id.central_content);
        mUndoButton = (ImageView)v.findViewById(R.id.button_undo);
        mRedoButton = (ImageView)v.findViewById(R.id.button_redo);

        mHorizontalScroll.setScrollingEnabled(false);
        mHorizontalScroll.setOnTouchListener(this);
        mVerticalScroll.setOnTouchListener(this);
        mVerticalScroll.setAlwaysInterceptMove(true);
        mCentralContent.setOnTouchListener(this);
//        mVerticalScroll.setClickable(true);
//        mHorizontalScroll.setOnClickListener(this);
        mHorizontalScroll.setScrollViewListener(this);
        mVerticalScroll.setScrollViewListener(this);
        mScrollLockCheck.setOnClickListener(this);
        mScrollLockCheck.setOnCheckedChangeListener(this);
        mUndoButton.setOnClickListener(this);
        mRedoButton.setOnClickListener(this);

        mWallList.setAdapter(new WallListAdapter());
        mWallList.setOnItemClickListener(this);

        mGridLayout.getLayoutParams().width =
                mGridLayout.getLayoutParams().height = LevelContainer.MAPSIZE *
                        mTileSize;


        ViewTreeObserver observer = mVerticalScroll.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
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
                    mHorizontalScroll.scrollTo(savedInstanceState.getInt(STATE_SCROLL_X), 0);
                    mVerticalScroll.scrollTo(0, savedInstanceState.getInt(STATE_SCROLL_Y));
                    mCurrentWallChoice = savedInstanceState.getInt(STATE_CURRENT_WALL_CHOICE);
                    boolean checked = savedInstanceState.getBoolean(STATE_SCROLL_LOCK);
                    mScrollLockCheck.setChecked(checked);
                    if(savedInstanceState.getBoolean(STATE_DRAWER_OPEN))
                        mDrawerLayout.openDrawer(Gravity.START);
                }
                updateData();
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout,
                R.string.contentdesc_open_level_drawer,
                R.string.contentdesc_close_level_drawer);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        ((ActionBarActivity)getActivity()).getSupportActionBar()
                .setDisplayHomeAsUpEnabled(true);
        ((ActionBarActivity)getActivity()).getSupportActionBar()
                .setHomeButtonEnabled(true);
        mDrawerToggle.syncState();
    }

    public void setDocument(Document document)
    {
        mDocument = document;
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSaveInstanceState(Bundle target)
    {
        target.putInt(STATE_CURRENT_LEVEL, mCurrentLevel);
        target.putInt(STATE_SCROLL_X, mHorizontalScroll.getScrollX());
        target.putInt(STATE_SCROLL_Y, mVerticalScroll.getScrollY());
        target.putInt(STATE_CURRENT_WALL_CHOICE, mCurrentWallChoice);
        target.putBoolean(STATE_SCROLL_LOCK, mScrollLockCheck.isChecked());
        target.putBoolean(STATE_DRAWER_OPEN, mDrawerLayout.isDrawerOpen(Gravity.START));
        if(mDocument != null) {
            StateSaver.putDocument(mPath, mDocument);
        }
        super.onSaveInstanceState(target);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if(mDocument == null)
        {
            mDocument = new Document();
        }
        if(mDocument.isLoaded())
        {
            Log.i(TAG, "Adding observer");
            mDocument.getLevels().addObserver(this);
        }
        else
        {
            tryCancelAutoloadTask();
            mAutoloadTask = new DocumentLoadAsyncTask(getActivity().getApplicationContext(), this, mPath,
                    new DocumentLoadAsyncTask.Listener()
            {
                @Override
                public void tryCancelDocumentTask()
                {
                    tryCancelAutoloadTask();
                }

                @Override
                public void removeDocumentTask()
                {
                    mAutoloadTask = null;
                }

                @Override
                public void onSuccessDocumentTask(Document document, File path)
                {
                    setDocument(document);
                    if (getArguments() != null)
                        getArguments().putString(ARG_PATH_NAME, path.getPath());
                    readArguments();
                    updateData();
                    onResume(); // on resume needs to be re-called
                }

                @Override
                public void onFailureDocumentTask()
                {
                    ((MainActivity) getActivity()).goToStartFragment();
                }
            });
            mAutoloadTask.setAutoload();
            mAutoloadTask.execute();
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if(mDocument != null && mDocument.isLoaded())
        {
            Log.i(TAG, "Removing observer");
            mDocument.getLevels().removeObserver(this);
        }
    }

    private void tryCancelAutoloadTask(boolean destroy)
    {
        if(mAutoloadTask != null && !mAutoloadTask.isCancelled())
        {
            if(destroy)
                mAutoloadTask.destroy();
            else
                mAutoloadTask.cancel(false);
        }
    }

    private void tryCancelAutoloadTask()
    {
        tryCancelAutoloadTask(false);
    }

    private void tryCancelAutosaveTask(boolean destroy)
    {
        if(sAutosaveTask != null && !sAutosaveTask.isCancelled())
        {
            if(destroy)
                sAutosaveTask.destroy();
            else
                sAutosaveTask.cancel(false);
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if(!mDocument.isLoaded())
            return;

        if(sAutosaveTask != null)
        {
            Log.w("LevelFragment", "Autosave task already running; exiting");
            return;
        }
        // Also look if it's in the StateSaver

        // TODO: consider the manual saving situation
        sAutosaveTask = new DocumentSaveAsyncTask(getActivity().getApplicationContext(),
                new DocumentSaveAsyncTask.Listener()
        {
            @Override
            public void removeDocumentTask()
            {
                Log.i("LevelFragment", "Document autosaved");
                sAutosaveTask = null;
            }
        });
        sAutosaveTask.execute(mDocument);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mGridLayout.removeAllViews();
        tryCancelAutoloadTask(true);
    }

    private void updateData()
    {
        if(mGridLayout == null)
            return;	// will be done anyway upon creation
        updateGridLayout();
        ((WallListAdapter)mWallList.getAdapter()).notifyDataSetChanged();
    }

    private void updateGridLayout()
    {
        if(!mDocument.isLoaded())
            return;

        if(mCurrentLevel < 0)
            mCurrentLevel = 0;
        else if(mCurrentLevel >= LevelContainer.NUMMAPS)
            mCurrentLevel = LevelContainer.NUMMAPS - 1;

        if(getActivity() instanceof ActionBarActivity)
            ((ActionBarActivity)getActivity()).getSupportActionBar()
                    .setTitle(mDocument.getLevels().getLevelName(mCurrentLevel));

        int ceilingColour = Palette.WL6[LevelContainer
                .getCeilingColour(mCurrentLevel)];

        mGridLayout.setBackgroundColor(ceilingColour);

        Point viewportPosition = new Point(mHorizontalScroll.getScrollX(),
                mVerticalScroll.getScrollY());
//        Log.i(TAG, "Point: " + viewportPosition);
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

    private void hitTileOnView(MotionEvent event, int k, boolean makeSound)
    {
        int i, j;
        j = (int)(MotionEventCompat.getX(event, k)
                + mHorizontalScroll.getScrollX()) / mTileSize;
        i = (int)(MotionEventCompat.getY(event, k) +
                mVerticalScroll.getScrollY()) / mTileSize;
        if(j >= 0 && j < LevelContainer.MAPSIZE
                && i >= 0 && i < LevelContainer.MAPSIZE)
            if(mTileViews[i][j] != null)
            {
                onClick(mTileViews[i][j]);
                if(makeSound)
                    mTileViews[i][j].playSoundEffect(
                            SoundEffectConstants.CLICK);
            }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        int action = MotionEventCompat.getActionMasked(event);

        if(mScrollLockCheck.isChecked())
        {
            // We also need to avoid interfering with the left drawer
            // Cancel DOWN if too left. But don't cancel MOVE if too left
            if(v == mCentralContent
                    && !mDrawerLayout.isDrawerVisible(Gravity.START))
            {
                int count = MotionEventCompat.getPointerCount(event);
                for(int k = 0; k < count; ++k)
                {
                    if(action != MotionEvent.ACTION_DOWN
                            && action != MotionEventCompat.ACTION_POINTER_DOWN
                            || MotionEventCompat.getX(event, k) >= mTileSize)
                    {
                        hitTileOnView(event, k, false);
                    }
                }
                return true;
            }
            return false;
        }
        if(v == mVerticalScroll)
        {
            // LOL trickery needed to scroll the below horiz view while this is
            // getting operated on
            if(mVerticalScroll.isScrollable())
            {
                mHorizontalScroll.setScrollingEnabled(true);
                mHorizontalScroll.dispatchTouchEvent(event);
                mHorizontalScroll.setScrollingEnabled(false);
            }
            if(action == MotionEvent.ACTION_DOWN ||
                    action == MotionEventCompat.ACTION_POINTER_DOWN)
            {
                mPressDown = true;
                //return true;
            }
            else if(action == MotionEvent.ACTION_UP ||
                    action == MotionEvent.ACTION_POINTER_UP)
            {
                if(mPressDown)
                {
                    hitTileOnView(event, (event.getAction() &
                            MotionEvent.ACTION_POINTER_INDEX_MASK) >>
                            MotionEvent.ACTION_POINTER_INDEX_SHIFT, true);
                }
            }
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
            if(mPressDown)
                mPressDown = false;	// cancel any pressed thing

            // this may be needed to prevent useless scrolling responses
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

    }

    @Override
    public void onOverScrolled(FrameLayout scrollView, int scrollX,
                               int scrollY, boolean clampedX, boolean clampedY)
    {
        // this method must be here despite doing nothing
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
        int mapSize = LevelContainer.MAPSIZE - 1;
        if(!Global.inBounds(i, 0, mapSize) || !Global.inBounds(j, 0, mapSize))
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
            iv = mTileViews[i1][j1];
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
//		short[][] level = document.getLevels().getLevel(mCurrentLevel);

//    	short[] wallplane = level[0];
//    	short[] actorplane = level[1];

        int cell = mDocument.getLevels().getTile(mCurrentLevel, 0, x, y);

        if(!setBitmapFromMapValue(mDocument, iv, cell))
        {
            cell = Global.getActorSpriteMap().get(
                    mDocument.getLevels().getTile(mCurrentLevel, 1, x, y), -1);
            if(cell == -1)
                iv.setImageBitmap(null);
            else
                iv.setImageBitmap(mDocument.getVSwap().getSpriteBitmap(cell));
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
                MainActivity activity = (MainActivity)getActivity();
                item = new ImageView(getActivity());
                AbsListView.LayoutParams alvlp = new AbsListView.LayoutParams(
                        (int)(48 * activity.getPixelScale()),
                        (int)(48 * activity.getPixelScale()));
                item.setLayoutParams(alvlp);
                item.setPadding(0, (int)(5 * activity.getPixelScale()),
                        0, (int)(5 * activity.getPixelScale()));
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
        JSONArray array;
        try
        {
            array = new JSONArray(jsonString);
        }
        catch(JSONException e)
        {
            e.printStackTrace();
            ((MainActivity)getActivity()).showErrorAlert("Internal error", "Failed to read the wall choice list!");
            return;
        }
        mWallChoices = array;
        mCurrentWallChoice = Global.boundValue(mCurrentWallChoice, 0, mWallChoices.length() - 1);
    }

    ////////////////////////////////////////////////////////////////////////////

    @Override
    public void onClick(View v)
    {
        if(v == mUndoButton)
        {
            undo();
        }
        else if(v == mRedoButton)
        {
            redo();
        }
        else if(v instanceof ImageView)
        {
            if(mWallChoices == null || !Global.inBounds(mCurrentWallChoice, 0,
                    mWallChoices.length() - 1))
            {
                Log.e(TAG, "Wall choices error: " + mWallChoices + " " + mCurrentWallChoice);
                return;
            }
            JSONObject obj = mWallChoices.optJSONObject(mCurrentWallChoice);
            if(obj == null)
            {
                Log.e(TAG, "Empty wall choice");
                return;
            }
//			Log.i(TAG, "Clicked " + v.getId() % 64 + " " + v.getId() / 64 + " real " +
//				((RelativeLayout.LayoutParams)v.getLayoutParams()).leftMargin / v.getWidth() + " " +
//				((RelativeLayout.LayoutParams)v.getLayoutParams()).topMargin / v.getHeight());
            mDocument.getLevels().setTile(mCurrentLevel, 0, v.getId(),
                    (short)obj.optInt("id"));
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        if(buttonView == mScrollLockCheck)
            mVerticalScroll.setScrollingEnabled(!isChecked);
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
            mHorizontalScroll.smoothScrollTo(x * mTileSize -
                    mViewportSize.x / 2, 0);
            mVerticalScroll.smoothScrollTo(0, y * mTileSize -
                    mViewportSize.y / 2);
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

    private boolean undo()
    {
        if(mDocument != null && mDocument.isLoaded() &&
                Global.inBounds(mCurrentLevel, 0, LevelContainer.NUMMAPS - 1) &&
                mDocument.getLevels().hasUndo(mCurrentLevel))
        {
            mDocument.getLevels().undo(mCurrentLevel);
            return true;
        }
        return false;
    }

    private boolean redo()
    {
        if(mDocument != null && mDocument.isLoaded() &&
                Global.inBounds(mCurrentLevel, 0, LevelContainer.NUMMAPS - 1) &&
                mDocument.getLevels().hasRedo(mCurrentLevel))
        {
            mDocument.getLevels().redo(mCurrentLevel);
            return true;
        }
        return false;
    }

    @Override
    public boolean handleBackButton()
    {
        if(mDrawerLayout.isDrawerOpen(Gravity.START))
        {
            mDrawerLayout.closeDrawers();
            return true;
        }

        if(undo())
            return true;

        // Nothing to undo
        final MainActivity activity = (MainActivity)getActivity();
        activity.showConfirmAlert(getString(R.string.close_document), getString(R.string.close_document_question),
                getString(R.string.close_document), getString(R.string.do_not_close), new Runnable()
        {
            @Override
            public void run()
            {
                activity.popFragment();
            }
        });

        return true;
    }
}