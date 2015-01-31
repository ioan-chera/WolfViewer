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
import org.i_chera.wolfensteineditor.Global;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Stack;

public class LevelContainer implements DefinedSizeObject{
    public static final int NUMMAPS = 60;
    public static final int MAPPLANES = 2;
    public static final int MAPSIZE = 64;
//    public static final int maparea = MAPSIZE * MAPSIZE;
    private static final int OUTPUT_RLEW_TAG = 0xabcd;
    private static final byte[] MAPS_MARKER = new byte[] { 'T', 'E', 'D', '5', 'v', '1', '.', '0' };
    private static final int LEVEL_NAME_LENGTH = 16;
    private static final int MAP_HEADER_SIZE = 3 * 4 + 3 * 2 + 2 + 2 + LEVEL_NAME_LENGTH;

    // data to be saved
    private short[][][] mLevels;
    private String[] mLevelNames;

    private ArrayList<Stack<UndoOperation>> mUndoStacks;
    private ArrayList<Stack<UndoOperation>> mRedoStacks;

    // dynamic states
    private ArrayList<Stack<UndoOperation>> mCurrentStacks;
    private boolean mRedoing;

    private ArrayList<WeakReference<Observer>> mObservers;

    public interface Observer
    {
        void observeLocalChange(int level, int plane, int i, short value);
    }

    @Override
    public synchronized int getSizeInBytes()
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
        size += calculateUndoStacksSize(mUndoStacks);
        size += calculateUndoStacksSize(mRedoStacks);

        return size;
    }

    private synchronized int calculateUndoStacksSize(ArrayList<Stack<UndoOperation>> stacks)
    {
        int size = 0;
        if(stacks != null)
        {
            for (Stack<UndoOperation> stack : stacks)
            {
                if (stack != null)
                {
                    for (UndoOperation operation : stack)
                        size += operation.getSizeInBytes();
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
        for(WeakReference<Observer> reference : mObservers)
        {
            if(reference.get() == who)
            {
                return; // already added
            }
        }

        mObservers.add(new WeakReference<>(who));
        Log.i("LevelContainer", "Observers: " + mObservers.size());
    }

    public void removeObserver(Observer who)
    {
        if(mObservers == null)
            return;
        int i = 0;

        WeakReference<LevelContainer.Observer> reference;
        while(i < mObservers.size())
        {
            reference = mObservers.get(i);
            if(reference.get() == null || reference.get() == who)
            {
                mObservers.remove(reference);
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

    public synchronized short getTile(int level, int plane, int x, int y)
    {
        return mLevels[level][plane][y * MAPSIZE + x];
    }

//    public short getTile(int level, int plane, int i)
//    {
//        return mLevels[level][plane][i];
//    }

    public synchronized void undo(int level)
    {
        if(!mUndoStacks.get(level).empty())
        {
            mCurrentStacks = mRedoStacks;
            mUndoStacks.get(level).pop().executeForLevels(this);
            mCurrentStacks = mUndoStacks;
        }
    }

    public synchronized void redo(int level)
    {
        if(!mRedoStacks.get(level).empty())
        {
            mRedoing = true;
            mRedoStacks.get(level).pop().executeForLevels(this);
            mRedoing = false;
        }
    }

    public synchronized boolean hasUndo(int level)
    {
        return !mUndoStacks.get(level).empty();
    }
    public synchronized boolean hasRedo(int level)
    {
        return !mRedoStacks.get(level).empty();
    }


    private synchronized void pushUndo(int level, UndoOperation operation)
    {
        if(mCurrentStacks == mUndoStacks && !mRedoing)
            mRedoStacks.get(level).clear();
        mCurrentStacks.get(level).push(operation);
    }

    public synchronized void setTile(final int level, final int plane, final int i, short value)
    {
        final short current = mLevels[level][plane][i];
        if(current == value)
            return;
        pushUndo(level, new UndoOperation(UndoOperation.SET_TILE, level, plane, i, current));
        mLevels[level][plane][i] = value;
        notifyObserversLocalChange(level, plane, i, value);
    }

    public synchronized String getLevelName(int n)
    {
        return mLevelNames[n];
    }

    public synchronized boolean loadFile(File mapHead, File gameMaps)
    {
        FileInputStream fis = null;
        RandomAccessFile raf = null;
        try
        {
            if(mapHead.isDirectory() || gameMaps.isDirectory())
            {
                Log.e("LevelContainer", "either file is a directory");
                return false;
            }
            if(mapHead.length() < 2 + 4 * NUMMAPS)
            {
                Log.e("LevelContainer", "invalid maphead size " + mapHead.length());
                return false;
            }

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
            MapHeader newHeader;
            byte[] nameBuffer = new byte[LEVEL_NAME_LENGTH];

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
                newLevelNames[i] = Global.nullTerminatedString(nameBuffer);

                newLevels[i] = cacheMap(raf, newHeader, newRlewTag);
                if(newLevels[i] == null)
                {
                    Log.e("LevelContainer", "couldn't load level " + i);
                    return false;
                }
            }
            // All ok
            mLevels = newLevels;
            mLevelNames = newLevelNames;
            mUndoStacks = new ArrayList<>(mLevels.length);
            mRedoStacks = new ArrayList<>(mLevels.length);
            for(short[][] level : mLevels)
            {
                mUndoStacks.add(new Stack<UndoOperation>());
                mRedoStacks.add(new Stack<UndoOperation>());
            }
            mCurrentStacks = mUndoStacks;
        }
        catch(FileNotFoundException e)
        {
            Log.e("LevelContainer", "file not found " + e.getMessage());
            return false;
        }
        catch(IOException e)
        {
            Log.e("LevelContainer", "file error " + e.getMessage());
            return false;
        }
        finally
        {
            FileUtil.close(fis);
            FileUtil.close(raf);
        }
        return true;
    }

    private synchronized short[][] cacheMap(RandomAccessFile raf, MapHeader newHeader,
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

    public synchronized boolean writeFile(File mapHead, File gameMaps)
    {
        // TODO: make the writing atomic. Also delete the temporary files if an error occurs
        OutputStream headStream = null;
        OutputStream mapsStream = null;
        try
        {
            headStream = new BufferedOutputStream(new FileOutputStream(mapHead));

            // write rlew tag
            FileUtil.writeInt16(headStream, OUTPUT_RLEW_TAG);

            mapsStream = new BufferedOutputStream(new FileOutputStream(gameMaps));
            mapsStream.write(MAPS_MARKER);

            MapHeader[] headers = new MapHeader[60];

            int position = MAPS_MARKER.length;

            ArrayList<Short> rlew;
            ArrayList<Byte> carmack;

            for(int i = 0; i < NUMMAPS; ++i)
            {
                headers[i] = new MapHeader();
                for(int j = 0; j < MAPPLANES; ++j)
                {
                    short[] plane = mLevels[i][j];
                    rlew = Compression.rlewCompress(plane, (short)OUTPUT_RLEW_TAG, 1);
                    rlew.set(0, (short)(plane.length * 2));
                    carmack = Compression.carmackCompress(rlew, 2);
                    carmack.set(0, (byte)((2 * rlew.size()) & 0xff));
                    carmack.set(1, (byte)((2 * rlew.size()) >>> 8));
                    headers[i].planeStart[j] = position;
                    headers[i].planeLength[j] = carmack.size();
                    position += carmack.size();

                    byte[] carmackRaw = new byte[carmack.size()];
                    for(int k = 0; k < carmackRaw.length; ++k)
                    {
                        carmackRaw[k] = carmack.get(k);
                    }
                    mapsStream.write(carmackRaw);
                }
            }

            for(int i = 0; i < NUMMAPS; ++i)
            {
                FileUtil.writeInt32(headStream, position);
                position += MAP_HEADER_SIZE;

                FileUtil.writeInt32(mapsStream, headers[i].planeStart[0]);
                FileUtil.writeInt32(mapsStream, headers[i].planeStart[1]);
                FileUtil.writeInt32(mapsStream, headers[i].planeStart[2]);
                FileUtil.writeInt16(mapsStream, headers[i].planeLength[0]);
                FileUtil.writeInt16(mapsStream, headers[i].planeLength[1]);
                FileUtil.writeInt16(mapsStream, headers[i].planeLength[2]);
                FileUtil.writeInt16(mapsStream, 64);
                FileUtil.writeInt16(mapsStream, 64);
                byte[] levelNameBytes = mLevelNames[i].getBytes("UTF-8");
                for(int j = 0; j < LEVEL_NAME_LENGTH; ++j)
                {
                    mapsStream.write(j < levelNameBytes.length ? levelNameBytes[j] : 0);
                }
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }
        finally
        {
            FileUtil.close(headStream);
            FileUtil.close(mapsStream);
        }
        return true;
    }

    public synchronized boolean loadUndoRedo(File undoFile, File redoFile)
    {
        InputStream undoStream = null;
        InputStream redoStream = null;
        try
        {
            int num;
            if(undoFile != null)
            {
                undoStream = new BufferedInputStream(new FileInputStream(undoFile));
                for(int i = 0; i < NUMMAPS; ++i)
                {
                    num = FileUtil.readInt32(undoStream);
                    for(int j = 0; j < num; ++j)
                        mUndoStacks.get(i).push(new UndoOperation(undoStream));
                }
            }
            if(redoFile != null)
            {
                redoStream = new BufferedInputStream(new FileInputStream(redoFile));
                for(int i = 0; i < NUMMAPS; ++i)
                {
                    num = FileUtil.readInt32(redoStream);
                    for(int j = 0; j < num; ++j)
                        mRedoStacks.get(i).push(new UndoOperation(redoStream));
                }
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }
        finally
        {
            FileUtil.close(undoStream);
            FileUtil.close(redoStream);
        }
        return true;
    }

    private synchronized OutputStream writeWhichStack(ArrayList<Stack<UndoOperation>> stacks, File file) throws IOException
    {
        OutputStream stream = null;
        if(stacks != null && stacks.size() > 0)
        {
            stream = new BufferedOutputStream(new FileOutputStream(file));
            for(Stack<UndoOperation> stack : stacks)
            {
                FileUtil.writeInt32(stream, stack != null ? stack.size() : 0);
                if(stack != null && stack.size() > 0)
                {
                    for(UndoOperation operation : stack)
                    {
                        stream.write(operation.getByteRepresentation());
                    }
                }
            }
        }
        return stream;
    }

    public synchronized boolean writeUndoRedo(File undoFile, File redoFile)
    {
        OutputStream undoStream = null;
        OutputStream redoStream = null;
        try
        {
            undoStream = writeWhichStack(mUndoStacks, undoFile);
            redoStream = writeWhichStack(mRedoStacks, redoFile);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }
        finally
        {
            FileUtil.close(undoStream);
            FileUtil.close(redoStream);
        }
        return true;
    }

    private class MapHeader
    {
        int planeStart[] = new int[3];
        int planeLength[] = new int[3];
    }
}
