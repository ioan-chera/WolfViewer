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
 * 
 * NOTE: decompression algorithms were taken from Wolf4SDL/Wolf3D.
 */

package com.ichera.wolfviewer.document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Stack;

import com.ichera.wolfviewer.Global;

public class LevelContainer 
{
	public static final int NUMMAPS = 60;
	public static final int MAPPLANES = 2;
	public static final int MAPSIZE = 64;
	public static final int maparea = MAPSIZE * MAPSIZE;
	
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
			mObservers = new ArrayList<WeakReference<Observer>>();
		mObservers.add(new WeakReference<LevelContainer.Observer>(who));
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
			newRlewTag = Global.readUInt16(fis);
			for(int i = 0; i < NUMMAPS; ++i)
				headerOffsets[i] = Global.readInt32(fis);
			
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
				newHeader.planeStart[0] = Global.readInt32(raf);
				newHeader.planeStart[1] = Global.readInt32(raf);
				newHeader.planeStart[2] = Global.readInt32(raf);
				newHeader.planeLength[0] = Global.readUInt16(raf);
				newHeader.planeLength[1] = Global.readUInt16(raf);
				newHeader.planeLength[2] = Global.readUInt16(raf);
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
				intermediaryLength = Global.readUInt16(raf);
				if(compressedData == null || compressedData.length < compressedLength)
					compressedData = new byte[compressedLength];
				raf.read(compressedData, 0, compressedLength);
				
				// Compressed data -> Carmack Expand -> Intermediary data
				intermediaryData = Global.carmackExpand(compressedData, 0, intermediaryLength);
				expandedLength = (intermediaryData[0] & 0xff) + (intermediaryData[1] & 0xff) * 256;
				
				// Intermediary data -> RLEW Expand -> Final data
				ret[plane] = Global.rlewExpandByteToShort(intermediaryData, 2, 
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
