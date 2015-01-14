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

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by ioan_chera on 15.01.2015.
 */
public abstract class SwitchableFragment extends Fragment {
    private Bundle mStateBundle;

    public void setStateBundle(Bundle stateBundle)
    {
        mStateBundle = stateBundle;
    }

    protected abstract void saveState(Bundle target);
    public abstract boolean handleBackButton();

    public void saveSwitchState()
    {
        saveState(mStateBundle);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        saveState(outState);
        super.onSaveInstanceState(outState);
    }

    protected Bundle getActualState(Bundle saved)
    {
        return saved == null ? getArguments() : saved;
    }
}
