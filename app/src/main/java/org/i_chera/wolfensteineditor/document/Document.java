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

import org.i_chera.wolfensteineditor.DefinedSizeObject;
import org.i_chera.wolfensteineditor.ProgressCallback;

import java.io.File;

public class Document implements DefinedSizeObject
{
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
    public boolean loadFromDirectory(File directory, ProgressCallback progressUpdater)
    {
        if(!directory.isDirectory())
            return false;

        // First check if files exist
        if(progressUpdater != null)
            progressUpdater.onProgress(1, 4, "Checking for files...");
        File vSwapFile = new File(directory, "vswap.wl6");
        if(!vSwapFile.exists())
            return false;
        File mapHeadFile = new File(directory, "maphead.wl6");
        if(!mapHeadFile.exists())
            return false;
        File gameMapsFile = new File(directory, "gamemaps.wl6");
        if(!gameMapsFile.exists())
            return false;

        // Try loading vswap container
        if(progressUpdater != null)
            progressUpdater.onProgress(2, 4, "Loading VSWAP file...");
        VSwapContainer vswap = new VSwapContainer();
        if(!vswap.loadFile(vSwapFile))
            return false;

        // Try loading level container
        if(progressUpdater != null)
            progressUpdater.onProgress(3, 4, "Loading MAPHEAD and GAMEMAPS files...");
        LevelContainer levels = new LevelContainer();
        if(!levels.loadFile(mapHeadFile, gameMapsFile))
            return false;

        // Success
        mDirectory = directory;
        mVSwap = vswap;
        mLevels = levels;
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
