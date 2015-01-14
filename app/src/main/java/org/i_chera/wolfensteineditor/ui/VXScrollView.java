package org.i_chera.wolfensteineditor.ui;

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

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ScrollView;

/**
 * Created by ioan_chera on 15.01.2015.
 */
public class VXScrollView extends ScrollView
{
    private boolean mScrollable = true;
    private boolean mAlwaysInterceptMove;
    private int mSlop;	// we need this again
    private float mStartX;	// needed for always-intercepting-horizontal

    public void setScrollingEnabled(boolean enabled) {
        mScrollable = enabled;
    }

    public void setAlwaysInterceptMove(boolean value)
    {
        mAlwaysInterceptMove = value;
    }

    public boolean isScrollable() {
        return mScrollable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // if we can scroll pass the event to the superclass
                if (mScrollable)
                {
                    mStartX = ev.getX();
                    return super.onTouchEvent(ev);
                }
                // only continue to handle the touch event if scrolling enabled
                return mScrollable; // mScrollable is always false at this point
            default:
                return super.onTouchEvent(ev);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Don't do anything with intercepted touch events if
        // we are not scrollable
        if (!mScrollable)
            return false;
        else if(mAlwaysInterceptMove && Math.abs(ev.getX() - mStartX) > mSlop &&
                ev.getActionMasked() == MotionEvent.ACTION_MOVE)
            return true;
        else
            return super.onInterceptTouchEvent(ev);
    }

    private ScrollViewListener scrollViewListener = null;

    void commonConstruct()
    {
        mSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    public VXScrollView(Context context) {
        super(context);
        commonConstruct();
    }

    public VXScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        commonConstruct();
    }

    public VXScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        commonConstruct();
    }

    public void setScrollViewListener(ScrollViewListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if(scrollViewListener != null) {
            scrollViewListener.onScrollChanged(this, x, y, oldx, oldy);
        }
    }

    @Override
    protected void onOverScrolled (int scrollX, int scrollY, boolean clampedX, boolean clampedY)
    {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        if(scrollViewListener != null)
        {
            scrollViewListener.onOverScrolled(this, scrollX, scrollY, clampedX, clampedY);
        }
    }
}