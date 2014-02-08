package com.ichera.wolfviewer.ui;

import android.widget.FrameLayout;

public interface ScrollViewListener 
{
	void onScrollChanged(FrameLayout scrollView, int x, int y, int oldx, int oldy);
}
