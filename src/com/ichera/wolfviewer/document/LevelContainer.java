package com.ichera.wolfviewer.document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.ichera.wolfviewer.Global;

public class LevelContainer 
{
	public static final int NUMMAPS = 60;
	public static final int MAPPLANES = 2;
	public static final int MAPSIZE = 64;
	public static final int maparea = MAPSIZE * MAPSIZE;
	
	private short[][][] mLevels;
	private String[] mLevelNames;
	
	private static final int[] sCeilingColours = new int[]{
		   0x1d, 0x1d, 0x1d, 0x1d, 0x1d, 0x1d, 0x1d, 0x1d, 0x1d, 0xbf,
		   0x4e, 0x4e, 0x4e, 0x1d, 0x8d, 0x4e, 0x1d, 0x2d, 0x1d, 0x8d,
		   0x1d, 0x1d, 0x1d, 0x1d, 0x1d, 0x2d, 0xdd, 0x1d, 0x1d, 0x98,

		   0x1d, 0x9d, 0x2d, 0xdd, 0xdd, 0x9d, 0x2d, 0x4d, 0x1d, 0xdd,
		   0x7d, 0x1d, 0x2d, 0x2d, 0xdd, 0xd7, 0x1d, 0x1d, 0x1d, 0x2d,
		   0x1d, 0x1d, 0x1d, 0x1d, 0xdd, 0xdd, 0x7d, 0xdd, 0xdd, 0xdd
	};
	
	public static int[] getCeilingColours()
	{
		return sCeilingColours;
	}
	
	public short getTile(int level, int plane, int x, int y)
	{
		return mLevels[level][plane][y * MAPSIZE + x];
	}
	
	public short getTile(int level, int plane, int i)
	{
		return mLevels[level][plane][i];
	}
	
	public short[] getPlane(int level, int plane)
	{
		return mLevels[level][plane];
	}
	
	public short[][] getLevel(int level)
	{
		return mLevels[level];
	}
	
	public short[][][] getLevels()
	{
		return mLevels;
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
		String name;
	}
}
