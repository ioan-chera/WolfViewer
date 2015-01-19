package org.i_chera.wolfensteineditor;

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

/**
 * Created by ioan_chera on 15.01.2015.
 */
public abstract class RunnableArg<T> implements Runnable {

    public T[] mArgs;

    public RunnableArg() {
    }

    public void run(T... args) {
        setArgs(args);
        run();
    }

    public void setArgs(T... args) {
        mArgs = args;
    }

    public int getArgCount() {
        return mArgs == null ? 0 : mArgs.length;
    }

    public Object[] getArgs() {
        return mArgs;
    }
}
