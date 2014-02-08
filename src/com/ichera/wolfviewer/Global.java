package com.ichera.wolfviewer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

public class Global 
{
	private static final String TAG = "Global";
	private static final String[] s_wolfFileNames =
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
	
	public static final int	SOUND_SAMPLE_RATE_HZ = 6896;
	
	private static float 	s_scale;
	
	private static boolean 	s_initialized;
	
	/**
	 * Initializes the global vars
	 * @param context the relevant context
	 */
	public static void initialize(Activity context)
	{
		if(s_initialized)
			return;
		Log.i(TAG, "Initializing...");
		s_initialized = true;
				
		s_scale = context.getResources().getDisplayMetrics().density;
	}
	
	public static String[] getWolfFileNames()
	{
		return s_wolfFileNames;
	}
	
	public static float getScale()
	{
		return s_scale;
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
		return (read[0] & 0xff) + ((read[1] & 0xff) << 8); 
	}
	public static int readUInt16(RandomAccessFile raf) throws IOException
	{
		byte[] read = new byte[2];
		raf.read(read, 0, 2);
		return (read[0] & 0xff) + ((read[1] & 0xff) << 8); 
	}
	public static int readUInt16(byte[] data, int offset)
	{
		return (data[offset] & 0xff) + ((data[offset + 1] & 0xff) << 8);
	}
	public static int readInt16(byte[] data, int offset)
	{
		return (data[offset] & 0xff) + (data[offset + 1] << 8);
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
		return (read[0] & 0xff) + ((read[1] & 0xff) << 8) 
				+ ((read[2] & 0xff) << 16) 
				+ ((read[3] & 0xff) << 24);
	}
	public static int readInt32(RandomAccessFile raf) throws IOException
	{
		byte[] read = new byte[4];
		raf.read(read, 0, 4);
		return (read[0] & 0xff) + ((read[1] & 0xff) << 8) 
				+ ((read[2] & 0xff) << 16) 
				+ ((read[3] & 0xff) << 24);
	}
	
	public static void showErrorAlert(Context context, String title, String message)
	{
		new AlertDialog.Builder(context).setTitle(title)
		.setMessage(message).setNeutralButton("OK", null).show();
	}
	
	private static final int NEARTAG = 0xa7;
	private static final int FARTAG = 0xa8;
	public static byte[] carmackExpand(byte[] source, int inOffset, int outLength)
	{
		byte[] dest = new byte[outLength];
		int inIndex = inOffset;
		int outIndex = 0;
		int count;
		int offset;
		int copyIndex;
		
		int ch, chhigh;
		
		outLength /= 2;
		while(outLength > 0)
		{
			ch = Global.readUInt16(source, inIndex);
			inIndex += 2;
			chhigh = ch / 256;
			if(chhigh == NEARTAG)
			{
				count = ch & 0xff;
				if(count == 0)
				{
					ch |= source[inIndex++] & 0xff;
					dest[2 * outIndex] = (byte)(ch & 0xff);
					dest[2 * outIndex + 1] = (byte)(ch / 256);
					outIndex++;
					outLength--;
				}
				else
				{
					offset = source[inIndex++] & 0xff;
					copyIndex = outIndex - offset;
					outLength -= count;
					if(outLength < 0)
						return dest;
					while(count > 0)
					{
						dest[2 * outIndex] = dest[2 * copyIndex];
						dest[2 * outIndex + 1] = dest[2 * copyIndex + 1];
						outIndex++;
						copyIndex++;
						count--;
					}
				}
			}
			else if(chhigh == FARTAG)
			{
				count = ch & 0xff;
				if(count == 0)
				{
					ch |= source[inIndex++] & 0xff;
					dest[2 * outIndex] = (byte)(ch & 0xff);
					dest[2 * outIndex + 1] = (byte)(ch / 256);
					outIndex++;
					outLength--;
				}
				else
				{
					offset = Global.readUInt16(source, inIndex);
					inIndex += 2;
					copyIndex = offset;
					outLength -= count;
					if(outLength < 0)
						return dest;
					while(count > 0)
					{
						dest[2 * outIndex] = dest[2 * copyIndex];
						dest[2 * outIndex + 1] = dest[2 * copyIndex + 1];
						outIndex++;
						copyIndex++;
						count--;
					}
				}
			}
			else
			{
				dest[2 * outIndex] = (byte)(ch & 0xff);
				dest[2 * outIndex + 1] = (byte)(ch / 256);
				outIndex++;
				outLength--;
			}
		}
		
		return dest;
	}
	
	public static short[] rlewExpandByteToShort(byte[] source, int inOffset, 
			int outLength, int rlewTag)
	{
		short[] dest = new short[outLength / 2];
		int inIndex = inOffset;
		int outIndex = 0;
		int value, count, i;
		
		do
		{
			value = readUInt16(source, inIndex);
			inIndex += 2;
			if(value != rlewTag)
			{
				dest[outIndex++] = (short)(value & 0xffff);
			}
			else
			{
				count = readUInt16(source, inIndex);
				inIndex += 2;
				value = readUInt16(source, inIndex);
				inIndex += 2;
				for(i = 0; i < count; ++i)
				{
					dest[outIndex++] = (short)(value & 0xffff);
				}
			}
		}while(outIndex < dest.length);
		
		return dest;
	}
}
