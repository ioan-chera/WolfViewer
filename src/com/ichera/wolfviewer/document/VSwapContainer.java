package com.ichera.wolfviewer.document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import com.ichera.wolfviewer.Global;
import com.ichera.wolfviewer.Palette;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * Container for VSWAP.WL6 data (walls, sprites, wave sound chunks)
 */
public class VSwapContainer 
{
	private int							mNumChunks;
	private int							mSpriteStart;
	private int							mSoundStart;
	private ArrayList<byte[]>			mPages;
	
	private LruCache<Integer, Bitmap>	mBitmapCache;
	
	/**
	 * Gets a bitmap from a given wall texture
	 * @param n Index of wall texture
	 * @return Null if invalid index or not a wall texture, the bitmap otherwise
	 */
	public Bitmap getTextureBitmap(int n)
	{
		if(n < 0 || n >= mSpriteStart)
			return null;
		
		if(mPages.get(n).length < 64 * 64)
			return null;
		
		if(mBitmapCache == null)
		{
			mBitmapCache = new LruCache<Integer, Bitmap>(mSpriteStart);
		}
		
		int[] ret = new int[64 * 64];
		for(int i = 0; i < ret.length; ++i)
			ret[64 * (i % 64) + i / 64] = Palette.WL6[mPages.get(n)[i] & 0xff];
		
		Bitmap bmp = mBitmapCache.get(n);
		if(bmp == null)
		{
			bmp = Bitmap.createBitmap(ret, 64, 64, Bitmap.Config.ARGB_8888);
			mBitmapCache.put(n, bmp);
		}
		
		return bmp;
	}
	
	/**
	 * Gets a 64x64 bitmap from a given sprite 
	 * @param n Index of sprite
	 * @return Null if invalid or out of bounds, the bitmap otherwise
	 */
	public Bitmap getSpriteBitmap(int n)
	{
		if(n < mSpriteStart || n >= mSoundStart)
			return null;
		
		Bitmap bmp = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888);
		
		byte[] data = mPages.get(n);
		int leftPixel = Global.readUInt16(data, 0);
		int rightPixel = Global.readUInt16(data, 2);
		if(leftPixel < 0 || leftPixel >= 64 || rightPixel < 0 || 
				rightPixel >= 64 || rightPixel < leftPixel)
			return null;
		int directoryOffset;
		int x, y, i, j, topPixel, bottomPixel, postStart;
		for(x = leftPixel, i = 4; x <= rightPixel; ++x, i += 2)
		{
			directoryOffset = Global.readUInt16(data, i);
			j = directoryOffset;
			for(;;)
			{
				bottomPixel = Global.readUInt16(data, j) / 2;
				j += 2;
				if(bottomPixel == 0)
					break;
				postStart = Global.readInt16(data, j);
				j += 2;
				topPixel = Global.readUInt16(data, j) / 2;
				j += 2;
				if(bottomPixel < 0 || bottomPixel > 64 || topPixel < 0 || 
						topPixel >= 64 || bottomPixel <= topPixel)
					return null;
				for(y = topPixel; y < bottomPixel; ++y)
				{
					bmp.setPixel(x, y, Palette.WL6[data[postStart + y] & 
					                               0xff]);
				}
			}
		}
		
		return bmp;
	}
	
	/**
	 * Number of chunks
	 * @return Total number of chunks
	 */
	public int getNumChunks()
	{
		return mNumChunks;
	}
	
	/**
	 * Index of first sprite
	 * @return
	 */
	public int getSpriteStart()
	{
		return mSpriteStart;
	}
	
	/**
	 * Index of first sound chunk
	 * @return
	 */
	public int getSoundStart()
	{
		return mSoundStart;
	}
	
	/**
	 * Raw data of given chunk
	 * @param n Index of chunk
	 * @return a byte array
	 */
	public byte[] getPage(int n)
	{
		return mPages.get(n);
	}
	
	/**
	 * Loads data from a file. Only finishes it on success
	 * @param file the VSWAP.WL6 file to load
	 * @return true on success
	 */
	public boolean loadFile(File file)
	{
		RandomAccessFile raf = null;
		try 
		{
			if(file.isDirectory())
				return false;
			raf = new RandomAccessFile(file, "r");

			int newNumChunks = Global.readUInt16(raf);
			int newSpriteStart = Global.readUInt16(raf);
			int newSoundStart = Global.readUInt16(raf);
			
			int[] pageOffsets = new int[newNumChunks + 1];
			for(int i = 0; i < newNumChunks; ++i)
				pageOffsets[i] = Global.readInt32(raf);
			
			int[] pageLengths = new int[newNumChunks];
			for(int i = 0; i < newNumChunks; ++i)
				pageLengths[i] = Global.readUInt16(raf);
			
			pageOffsets[newNumChunks] = (int)file.length();
						
			ArrayList<byte[]> newPages = new ArrayList<byte[]>();
			
			byte[] reading;
			for(int i = 0; i < newNumChunks; ++i)
			{				
				if(pageOffsets[i] == 0)
				{
					reading = new byte[0];
					newPages.add(reading);
					continue;
				}
				int size;
				if(pageOffsets[i + 1] == 0)
					size = pageLengths[i];
				else
					size = pageOffsets[i + 1] - pageOffsets[i];
				
				raf.seek(pageOffsets[i]);
				reading = new byte[size];
				raf.read(reading, 0, size);
				newPages.add(reading);
			}
			
			// okay
			mNumChunks = newNumChunks;
			mSpriteStart = newSpriteStart;
			mSoundStart = newSoundStart;
			mPages = newPages;
			
		}
		catch(FileNotFoundException fnfe)
		{
			fnfe.printStackTrace();
			return false;
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
			return false;
		}
		finally
		{
			try
			{
				if(raf != null)
					raf.close();
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		return true;
	}
}
