package com.ichera.wolfviewer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class VSwapContainer 
{
	private int					m_numChunks;
	private int					m_spriteStart;
	private int					m_soundStart;
	private ArrayList<byte[]>	m_pages;
	
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
