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

import android.util.Log;

import org.i_chera.wolfensteineditor.Compression;
import org.i_chera.wolfensteineditor.DefinedSizeObject;
import org.i_chera.wolfensteineditor.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by ioan_chera on 15.01.2015.
 */
public class LevelContainer implements DefinedSizeObject{
    public static final int NUMMAPS = 60;
    public static final int MAPPLANES = 2;
    public static final int MAPSIZE = 64;
    public static final int maparea = MAPSIZE * MAPSIZE;

    private static final int ARBITRARY_RUNNABLE_SIZE = 16;

    private short[][][] mLevels;
    private String[] mLevelNames;

    private ArrayList<Stack<Runnable>> mUndoStacks;
    private ArrayList<Stack<Runnable>> mRedoStacks;
    private ArrayList<Stack<Runnable>> mCurrentStacks;

    private ArrayList<WeakReference<Observer>> mObservers;

    public interface Observer
    {
        void observeLocalChange(int level, int plane, int i, short value);
    }

    @Override
    public int getSizeInBytes()
    {
        int size = 0;
        if(mLevels != null) {
            for (short[][] level : mLevels) {
                if (level != null) {
                    for (short[] layer : level) {
                        if (layer != null)
                            size += 2 * layer.length;
                    }
                }
            }
        }

        if(mLevelNames != null) {
            for (String levelName : mLevelNames) {
                size += levelName.length();
            }
        }

        // It may be different than stated, but approximation is okay
        size += calculateRunnableStacksSize(mUndoStacks);
        size += calculateRunnableStacksSize(mRedoStacks);

        return size;
    }

    private int calculateRunnableStacksSize(ArrayList<Stack<Runnable>> stacks)
    {
        int size = 0;
        if(stacks != null) {
            for (Stack<Runnable> stack : stacks) {
                if (stack != null) {
                    size += ARBITRARY_RUNNABLE_SIZE * stack.size();
                }
            }
        }
        return size;
    }


    private void notifyObserversLocalChange(int level, int plane, int i, short value)
    {
        if(mObservers == null)
            return;
        for(WeakReference<LevelContainer.Observer> wr : mObservers)
        {
            if(wr.get() != null)
            {
                wr.get().observeLocalChange(level, plane, i, value);
            }
        }
    }

    public void addObserver(Observer who)
    {
        if(mObservers == null)
            mObservers = new ArrayList<>();
        mObservers.add(new WeakReference<>(who));
        Log.i("LevelContainer", "Observers: " + mObservers.size());
    }

    public void removeObserver(Observer who)
    {
        if(mObservers == null)
            return;
        int i = 0;
        WeakReference<LevelContainer.Observer> wr;
        while(i < mObservers.size())
        {
            wr = mObservers.get(i);
            if(wr.get() == null || wr.get() == who)
            {
                mObservers.remove(wr);
                i = 0;
                continue;
            }
            ++i;
        }
    }

    private static final int[] sCeilingColours = new int[]{
            0x1d, 0x1d, 0x1d, 0x1d, 0x1d, 0x1d, 0x1d, 0x1d, 0x1d, 0xbf,
            0x4e, 0x4e, 0x4e, 0x1d, 0x8d, 0x4e, 0x1d, 0x2d, 0x1d, 0x8d,
            0x1d, 0x1d, 0x1d, 0x1d, 0x1d, 0x2d, 0xdd, 0x1d, 0x1d, 0x98,

            0x1d, 0x9d, 0x2d, 0xdd, 0xdd, 0x9d, 0x2d, 0x4d, 0x1d, 0xdd,
            0x7d, 0x1d, 0x2d, 0x2d, 0xdd, 0xd7, 0x1d, 0x1d, 0x1d, 0x2d,
            0x1d, 0x1d, 0x1d, 0x1d, 0xdd, 0xdd, 0x7d, 0xdd, 0xdd, 0xdd
    };

    public static int getCeilingColour(int index)
    {
        return sCeilingColours[index];
    }

    public short getTile(int level, int plane, int x, int y)
    {
        return mLevels[level][plane][y * MAPSIZE + x];
    }

    public short getTile(int level, int plane, int i)
    {
        return mLevels[level][plane][i];
    }

    public void undo(int level)
    {
        if(!mUndoStacks.get(level).empty())
        {
            mCurrentStacks = mRedoStacks;
            mUndoStacks.get(level).pop().run();
            mCurrentStacks = mUndoStacks;
        }
    }

    public void redo(int level)
    {
        if(!mRedoStacks.get(level).empty())
        {
            mRedoStacks.get(level).pop().run();
        }
    }

    public boolean hasUndo(int level)
    {
        return !mUndoStacks.get(level).empty();
    }

    private void pushUndo(int level, Runnable command)
    {
        if(mCurrentStacks == mUndoStacks)
            mRedoStacks.get(level).clear();
        mCurrentStacks.get(level).push(command);
    }

    public void setTile(final int level, final int plane, final int i, short value)
    {
        final short current = mLevels[level][plane][i];
        if(current == value)
            return;
        pushUndo(level, new Runnable()
        {
            @Override
            public void run()
            {
                setTile(level, plane, i, current);
            }
        });
        mLevels[level][plane][i] = value;
        notifyObserversLocalChange(level, plane, i, value);
    }

    public String getLevelName(int n)
    {
        return mLevelNames[n];
    }

    public boolean loadFile(File mapHead, File gameMaps)
    {
        FileInputStream fis = null;
        RandomAccessFile raf = null;
        try
        {
            if(mapHead.isDirectory() || gameMaps.isDirectory())
                return false;
            if(mapHead.length() < 2 + 4 * NUMMAPS)
                return false;

            int newRlewTag;
            int[] headerOffsets = new int[NUMMAPS];

            fis = new FileInputStream(mapHead);
            newRlewTag = FileUtil.readUInt16(fis);
            for(int i = 0; i < NUMMAPS; ++i)
                headerOffsets[i] = FileUtil.readInt32(fis);

            fis.close();
            fis = null;

            raf = new RandomAccessFile(gameMaps, "r");
            int pos;
            MapHeader newHeader = new MapHeader();
            byte[] nameBuffer = new byte[16];

            short[][][] newLevels = new short[NUMMAPS][][];
            String[] newLevelNames = new String[NUMMAPS];
            for(int i = 0; i < NUMMAPS; ++i)
            {
                pos = headerOffsets[i];
                if(pos < 0)
                    continue;
                raf.seek(pos);
                newHeader = new MapHeader();
                newHeader.planeStart[0] = FileUtil.readInt32(raf);
                newHeader.planeStart[1] = FileUtil.readInt32(raf);
                newHeader.planeStart[2] = FileUtil.readInt32(raf);
                newHeader.planeLength[0] = FileUtil.readUInt16(raf);
                newHeader.planeLength[1] = FileUtil.readUInt16(raf);
                newHeader.planeLength[2] = FileUtil.readUInt16(raf);
                raf.skipBytes(2 + 2);
                raf.read(nameBuffer);
                newLevelNames[i] = new String(nameBuffer, "UTF-8");

                newLevels[i] = cacheMap(raf, newHeader, newRlewTag);
                if(newLevels[i] == null)
                    return false;
            }
            // All ok
            mLevels = newLevels;
            mLevelNames = newLevelNames;
            mUndoStacks = new ArrayList<Stack<Runnable>>(mLevels.length);
            mRedoStacks = new ArrayList<Stack<Runnable>>(mLevels.length);
            for(int i = 0; i < mLevels.length; ++i)
            {
                mUndoStacks.add(new Stack<Runnable>());
                mRedoStacks.add(new Stack<Runnable>());
            }
            mCurrentStacks = mUndoStacks;
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
            return false;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }
        finally
        {
            if(fis != null)
            {
                try
                {
                    fis.close();
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
            }
            if(raf != null)
            {
                try
                {
                    raf.close();
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    private short[][] cacheMap(RandomAccessFile raf, MapHeader newHeader,
                               int newRlewTag) throws IOException
    {
        short[][] ret = new short[MAPPLANES][];
        try
        {
            int pos, compressedLength, intermediaryLength, expandedLength;
            byte[] compressedData = null;
            byte[] intermediaryData;
            for(int plane = 0; plane < MAPPLANES; ++plane)
            {
                pos = newHeader.planeStart[plane];
                compressedLength = newHeader.planeLength[plane] - 2;
                raf.seek(pos);
                // First read the intermediary length, then the compressed data
                intermediaryLength = FileUtil.readUInt16(raf);
                if(compressedData == null || compressedData.length < compressedLength)
                    compressedData = new byte[compressedLength];
                raf.read(compressedData, 0, compressedLength);

                // Compressed data -> Carmack Expand -> Intermediary data
                intermediaryData = Compression.carmackExpand(compressedData, 0, intermediaryLength);
                expandedLength = (intermediaryData[0] & 0xff) + (intermediaryData[1] & 0xff) * 256;

                // Intermediary data -> RLEW Expand -> Final data
                ret[plane] = Compression.rlewExpandByteToShort(intermediaryData, 2,
                        expandedLength, newRlewTag);
            }
        }
        catch(NegativeArraySizeException e)
        {
            e.printStackTrace();
            return null;
        }
        catch(IndexOutOfBoundsException e)
        {
            e.printStackTrace();
            return null;
        }
        return ret;
    }

    private class MapHeader
    {
        int planeStart[] = new int[3];
        int planeLength[] = new int[3];
    }
}
