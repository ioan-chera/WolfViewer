package com.ichera.wolfviewer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.app.Activity;
import android.util.DisplayMetrics;

public class Global 
{
		
	public static final String[] s_wolfFileNames =
		{
		"AUDIOHED.WL6",
		"AUDIOT.WL6",
		"GAMEMAPS.WL6",
		"MAPHEAD.WL6",
		"VGADICT.WL6",
		"VGAGRAPH.WL6",
		"VGAHEAD.WL6",
		"VSWAP.WL6",
		};
	
	public static float 	s_scale;
	public static DisplayMetrics	s_metrics;
	
	private static boolean 	s_initialized;
	
	/**
	 * Initializes the global vars
	 * @param context the relevant context
	 */
	public static void initialize(Activity context)
	{
		if(s_initialized)
			return;
		s_initialized = true;
		
		s_metrics = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(s_metrics);
		
		s_scale = context.getResources().getDisplayMetrics().density;
	}
	
	private static int byteToUnsigned(byte b)
	{
		return (int)b & 0xff;
	}
	
	/**
	 * Reads an unsigned short
	 * @param fis file input stream
	 * @return the read value
	 * @throws IOException 
	 */
	public static int readUInt16(FileInputStream fis) throws IOException
	{
		byte[] read = new byte[2];
		fis.read(read, 0, 2);
		return byteToUnsigned(read[0]) + (byteToUnsigned(read[1]) << 8); 
	}
	public static int readUInt16(RandomAccessFile raf) throws IOException
	{
		byte[] read = new byte[2];
		raf.read(read, 0, 2);
		return byteToUnsigned(read[0]) + (byteToUnsigned(read[1]) << 8); 
	}
	
	/**
	 * Reads a signed int
	 * @param fis file input stream
	 * @return the read value
	 * @throws IOException 
	 */
	public static int readInt32(FileInputStream fis) throws IOException
	{
		byte[] read = new byte[4];
		fis.read(read, 0, 4);
		return byteToUnsigned(read[0]) + (byteToUnsigned(read[1]) << 8) 
				+ (byteToUnsigned(read[2]) << 16) 
				+ (byteToUnsigned(read[3]) << 24);
	}
	public static int readInt32(RandomAccessFile raf) throws IOException
	{
		byte[] read = new byte[4];
		raf.read(read, 0, 4);
		return byteToUnsigned(read[0]) + (byteToUnsigned(read[1]) << 8) 
				+ (byteToUnsigned(read[2]) << 16) 
				+ (byteToUnsigned(read[3]) << 24);
	}
}
