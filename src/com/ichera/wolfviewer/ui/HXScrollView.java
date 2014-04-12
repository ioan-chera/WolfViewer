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

package com.ichera.wolfviewer.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

public class HXScrollView extends HorizontalScrollView {
    private ScrollViewListener scrollViewListener = null;

    public HXScrollView(Context context) {
        super(context);
    }

    public HXScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public HXScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
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
    
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }
}
