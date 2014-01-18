package com.ichera.wolfviewer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class VSwapContainer 
{
	int					m_numChunks;
	int					m_spriteStart;
	int					m_soundStart;
	boolean				m_soundInfoPagePadded;
	ArrayList<byte[]>	m_pages;
	int					m_pageDataSize;
	
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
	
	public boolean isSoundInfoPagePadded()
	{
		return m_soundInfoPagePadded;
	}
	
	public byte[] getPage(int n)
	{
		return m_pages.get(n);
	}
	
	public boolean loadFile(File file)
	{
		boolean errorOccurred = false;
		
		RandomAccessFile raf = null;
		try 
		{
			raf = new RandomAccessFile(file, "r");
		} 
		catch (FileNotFoundException e) 
		{
			
			e.printStackTrace();
			return false;
		}
		try
		{
			int newNumChunks = Global.readUInt16(raf);
			int newSpriteStart = Global.readUInt16(raf);
			int newSoundStart = Global.readUInt16(raf);
			
			int[] pageOffsets = new int[newNumChunks + 1];
			for(int i = 0; i < newNumChunks; ++i)
				pageOffsets[i] = Global.readInt32(raf);
			
			int[] pageLengths = new int[newNumChunks];
			for(int i = 0; i < newNumChunks; ++i)
				pageLengths[i] = Global.readUInt16(raf);
			
			long fileSize = file.length();
			
			long unpaddedDataSize = fileSize - pageOffsets[0];
			
			pageOffsets[newNumChunks] = (int)fileSize;
			
			int dataStart = pageOffsets[0];
			
			int alignPadding = 0;
			int offs;
			for(int i = newSpriteStart; i < newSoundStart; ++i)
			{
				if(pageOffsets[i] == 0)
					continue;
				offs = pageOffsets[i] - dataStart + alignPadding;
				if((offs & 1) == 1)
					++alignPadding;
			}
			if(((pageOffsets[newNumChunks - 1] - dataStart + alignPadding) & 1)
					== 1)
				++alignPadding;
			
			int newPageDataSize = (int)unpaddedDataSize + alignPadding;
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
			m_soundInfoPagePadded = false;
			m_pages = newPages;
			m_pageDataSize = newPageDataSize;
			
		}
		catch(Exception e)
		{
			errorOccurred = true;
			e.printStackTrace();
		}
		try
		{
			if(raf != null)
				raf.close();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
			return false;
		}
		return !errorOccurred;
	}
}
