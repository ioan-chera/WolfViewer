package com.ichera.wolfviewer.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class VXScrollView extends ScrollView 
{
	
    private ScrollViewListener scrollViewListener = null;

    public VXScrollView(Context context) {
        super(context);
    }

    public VXScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public VXScrollView(Context context, AttributeSet attrs) {
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
}
