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

import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.ichera.wolfviewer.Global;

public class VisibilityGrid 
{
	static final String TAG = "VisibilityGrid";
	
	private Point mSize, mPosition;	// size and position of viewport, in tiles
	private Delegate mDelegate;
	
	public interface Delegate
	{
		void clearVisTiles();
		void createVisTile(int i, int j);
		void moveVisTile(int i1, int j1, int i2, int j2);
		void finalizeMoveVisTiles();
	}
	
	public void create(Point cCanvasSize, Rect cViewportRect, int itemSize,
			Delegate delegate)
	{
		mDelegate = delegate;
		// contain the data
		Point canvasSize = new Point(cCanvasSize);
		Rect viewportRect = new Rect(cViewportRect);
		
		viewportRect.right = Global.boundValue(viewportRect.right, 0, canvasSize.x);
		viewportRect.left = Global.boundValue(viewportRect.left, 0, canvasSize.x);
		viewportRect.bottom = Global.boundValue(viewportRect.bottom, 0, canvasSize.y);
		viewportRect.top = Global.boundValue(viewportRect.top, 0, canvasSize.y);
		
		mSize = new Point(viewportRect.width() / itemSize + 2, 
				viewportRect.height() / itemSize + 2);
		// so if item size is 64, then:
		// w = 0-63: 2; 64-127: 3. This makes sure that it will always be covered 
		mPosition = new Point(viewportRect.left / itemSize, viewportRect.top / itemSize);
		
		mDelegate.clearVisTiles();
		for(int i = 0; i < mSize.y; ++i)
			for(int j = 0; j < mSize.x; ++j)
				mDelegate.createVisTile(mPosition.y + i, mPosition.x + j);
	}
	
	public void columnUpdate(int oldJ, int newJ)
	{
//		Log.i(TAG, "columnUpdate: " + oldJ + " " + newJ + " mPosition.y " + mPosition.y);
		if(newJ > oldJ)
			for(int j = oldJ; j < newJ; ++j)
				for(int i = 0; i < mSize.y; ++i)
					mDelegate.moveVisTile(i + mPosition.y, j, i + mPosition.y, j + mSize.x);
		else if(newJ < oldJ)
			for(int j = newJ; j < oldJ; ++j)
				for(int i = 0; i < mSize.y; ++i)
					mDelegate.moveVisTile(i + mPosition.y, j + mSize.x, i + mPosition.y, j);
		mPosition.x = newJ;
		mDelegate.finalizeMoveVisTiles();
	}
	
	public void rowUpdate(int oldI, int newI)
	{
//		Log.i(TAG, "rowUpdate: " + oldI + " " + newI + " mPosition.x " + mPosition.x);
		if(newI > oldI)
			for(int i = oldI; i < newI; ++i)
				for(int j = 0; j < mSize.x; ++j)
					mDelegate.moveVisTile(i, j + mPosition.x, i + mSize.y, j + mPosition.x);
		else if(newI < oldI)
			for(int i = newI; i < oldI; ++i)
				for(int j = 0; j < mSize.x; ++j)
					mDelegate.moveVisTile(i + mSize.y, j + mPosition.x, i, j + mPosition.x);
		mPosition.y = newI;
		mDelegate.finalizeMoveVisTiles();
	}
}
