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

import android.widget.FrameLayout;

/**
 * Created by ioan_chera on 15.01.2015.
 */
public interface ScrollViewListener
{
    void onScrollChanged(FrameLayout scrollView, int x, int y, int oldx, int oldy);
    void onOverScrolled(FrameLayout scrollView, int scrollX, int scrollY,
                        boolean clampedX, boolean clampedY);
}
