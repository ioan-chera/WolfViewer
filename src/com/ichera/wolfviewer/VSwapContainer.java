package com.ichera.wolfviewer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import android.graphics.Bitmap;

public class VSwapContainer 
{
	private int					m_numChunks;
	private int					m_spriteStart;
	private int					m_soundStart;
	private ArrayList<byte[]>	m_pages;
	
	public Bitmap getTextureBitmap(int n)
	{
		if(n < 0 || n >= m_spriteStart)
			return null;
		
		if(m_pages.get(n).length < 64 * 64)
			return null;
		
		int[] ret = new int[64 * 64];
		for(int i = 0; i < ret.length; ++i)
			ret[64 * (i % 64) + i / 64] = Palette.WL6[m_pages.get(n)[i] & 0xff];
		
		return Bitmap.createBitmap(ret, 64, 64, Bitmap.Config.ARGB_8888);
	}
	
	public Bitmap getSpriteBitmap(int n)
	{
		if(n < m_spriteStart || n >= m_soundStart)
			return null;
		
		Bitmap bmp = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888);
		
		byte[] data = m_pages.get(n);
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
	
	public int getNumChunks()
	{
		return m_numChunks;
	}
	
	public int getSpriteStart()
	{
		return m_spriteStart;
	}
	
	public int getSoundStart()
	{
		return m_soundStart;
	}
	
	public byte[] getPage(int n)
	{
		return m_pages.get(n);
	}
	
	public boolean loadFile(File file)
	{
		RandomAccessFile raf = null;
		try 
		{
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
			m_numChunks = newNumChunks;
			m_spriteStart = newSpriteStart;
			m_soundStart = newSoundStart;
			m_pages = newPages;
			
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
