package org.i_chera.wolfensteineditor.document;

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
import android.util.Log;

import org.i_chera.wolfensteineditor.DefinedSizeObject;
import org.i_chera.wolfensteineditor.FileUtil;
import org.i_chera.wolfensteineditor.ProgressCallback;
import org.i_chera.wolfensteineditor.R;

import java.io.File;

public class Document implements DefinedSizeObject
{
    private static final String AUTOSAVE_DIRECTORY = "autosave";

    // Wolf data
    private VSwapContainer	mVSwap;
    private LevelContainer	mLevels;

    // If it was loaded, it has a corresponding directory. NOTE: it may not
    // exist any more or be invalidated. Either declare the Document damaged
    // (if not the whole data was loaded) or recreate the file tree (if the
    // whole was loaded)
    private File mDirectory;

    /**
     * Private constructor
     */
    public Document()
    {

    }

    @Override
    public int getSizeInBytes()
    {
        int size = 0;
        if(mVSwap != null)
            size += mVSwap.getSizeInBytes();
        if(mLevels != null)
            size += mLevels.getSizeInBytes();
        if(mDirectory != null)
            size += mDirectory.getPath().length();
        return size;
    }


    /**
     * Returns true if the document was successfully loaded
     *
     */
    public boolean isLoaded()
    {
        return mDirectory != null;
    }

    /**
     * Loads data from a given directory. It only overwrites its contents if it
     * returns true.
     * @param directory Directory containing Wolfenstein files
     * @return True on success
     */
    public boolean loadFromDirectory(Context context, File directory, ProgressCallback progressUpdater)
    {
        return loadFromDirectory(context, directory, false, progressUpdater);
    }
    public boolean loadFromDirectory(Context context, File directory, boolean autoload, ProgressCallback progressUpdater)
    {
        File realDirectory = directory;
        if(autoload)
            directory = new File(context.getFilesDir(), AUTOSAVE_DIRECTORY);
        if(!directory.isDirectory())
            return false;

        int max = 4;
        if(autoload)
            max = 5;

        // First check if files exist
        if(progressUpdater != null)
            progressUpdater.onProgress(1, max, context.getString(R.string.checking_for_files));
        File vSwapFile = new File(directory, "vswap.wl6");
        if(!vSwapFile.exists())
        {
            Log.e("Document", "vswap file doesn't exist");
            return false;
        }
        File mapHeadFile = new File(directory, "maphead.wl6");
        if(!mapHeadFile.exists())
        {
            Log.e("Document", "maphead file doesn't exist");
            return false;
        }
        File gameMapsFile = new File(directory, "gamemaps.wl6");
        if(!gameMapsFile.exists())
        {
            Log.e("Document", "gamemaps file doesn't exist");
            return false;
        }

        // Try loading vswap container
        if(progressUpdater != null)
            progressUpdater.onProgress(2, max, context.getString(R.string.loading_vswap_file));
        VSwapContainer vswap = new VSwapContainer();
        if(!vswap.loadFile(vSwapFile))
        {
            Log.e("Document", "Couldn't load vswap");
            return false;
        }

        // Try loading level container
        if(progressUpdater != null)
            progressUpdater.onProgress(3, max, context.getString(R.string.loading_maphead_gamemaps));
        LevelContainer levels = new LevelContainer();
        if(!levels.loadFile(mapHeadFile, gameMapsFile))
        {
            Log.e("Document", "Couldn't load levels");
            return false;
        }
        if(autoload)
        {
            if(progressUpdater != null)
                progressUpdater.onProgress(4, max, context.getString(R.string.loading_current_state));
            File undoFile = new File(directory, "undo");
            File redoFile = new File(directory, "redo");
            if(!undoFile.exists())
                undoFile = null;
            if(!redoFile.exists())
                redoFile = null;
            if((undoFile != null || redoFile != null) && !levels.loadUndoRedo(undoFile, redoFile))
            {
                Log.e("Document", "Couldn't load undo/redo");
                return false;
            }
        }

        // Success
        mDirectory = realDirectory;
        mVSwap = vswap;
        mLevels = levels;
        return true;
    }

    public boolean autosave(Context context, ProgressCallback progressUpdater)
    {
        if(progressUpdater != null)
            progressUpdater.onProgress(1, 5, context.getString(R.string.autosaving));
        File directory = new File(context.getFilesDir(), AUTOSAVE_DIRECTORY);
        if(!directory.mkdir() && !directory.isDirectory())
        {
            FileUtil.deleteRecursively(directory);
            return false;
        }

        File vSwapFile = new File(directory, "vswap.wl6");
        File mapHeadFile = new File(directory, "maphead.wl6");
        File gameMapsFile = new File(directory, "gamemaps.wl6");
        File undoFile = new File(directory, "undo");
        File redoFile = new File(directory, "redo");

        if(progressUpdater != null)
            progressUpdater.onProgress(2, 5, context.getString(R.string.saving_vswap_file));
        if(!mVSwap.writeFile(vSwapFile))
        {
            FileUtil.deleteRecursively(directory);
            return false;
        }

        if(progressUpdater != null)
            progressUpdater.onProgress(3, 5, context.getString(R.string.saving_maphead_gamemaps));
        if(!mLevels.writeFile(mapHeadFile, gameMapsFile))
        {
            FileUtil.deleteRecursively(directory);
            return false;
        }

        if(progressUpdater != null)
            progressUpdater.onProgress(3, 5, context.getString(R.string.saving_maphead_gamemaps));
        if(!mLevels.writeUndoRedo(undoFile, redoFile))
        {
            FileUtil.deleteRecursively(directory);
            return false;
        }

        return true;
    }

    /**
     * Gets reference to VSWap container
     *
     */
    public VSwapContainer getVSwap()
    {
        return mVSwap;
    }

    public LevelContainer getLevels()
    {
        return mLevels;
    }
}
