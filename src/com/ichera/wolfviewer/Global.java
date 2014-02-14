package com.ichera.wolfviewer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.util.SparseIntArray;

public class Global 
{
	private static final String TAG = "Global";
	private static final String[] sWolfFileNames =
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
	
	private static float 	sScale;
	
	private static boolean 	sInitialized;
	
	private static SparseIntArray sActorSpriteMap;
	
	static
	{
		sActorSpriteMap = new SparseIntArray(200);
		
		// BJ
		sActorSpriteMap.put(19, 408);
		sActorSpriteMap.put(20, 409);
		sActorSpriteMap.put(21, 410);
		sActorSpriteMap.put(22, 411);
		
		// statics
		for(int i = 23; i <= 72; ++i)
			sActorSpriteMap.put(i, i - 21);
		
		// guard
		for(int i = 180; i <= 183; ++i)
			sActorSpriteMap.put(i, 56 - (i - 180) * 2);
		for(int i = 144; i <= 147; ++i)
			sActorSpriteMap.put(i, 56 - (i - 144) * 2);
		for(int i = 108; i <= 111; ++i)
			sActorSpriteMap.put(i, 56 - (i - 108) * 2);
		for(int i = 184; i <= 187; ++i)
			sActorSpriteMap.put(i, 64 - (i - 184) * 2);
		for(int i = 148; i <= 151; ++i)
			sActorSpriteMap.put(i, 64 - (i - 148) * 2);
		for(int i = 112; i <= 115; ++i)
			sActorSpriteMap.put(i, 64 - (i - 112) * 2);
		
		// officer
		for(int i = 188; i <= 191; ++i)
			sActorSpriteMap.put(i, 244 - (i - 188) * 2);
		for(int i = 152; i <= 155; ++i)
			sActorSpriteMap.put(i, 244 - (i - 152) * 2);
		for(int i = 116; i <= 119; ++i)
			sActorSpriteMap.put(i, 244 - (i - 116) * 2);
		for(int i = 192; i <= 195; ++i)
			sActorSpriteMap.put(i, 252 - (i - 192) * 2);
		for(int i = 156; i <= 159; ++i)
			sActorSpriteMap.put(i, 252 - (i - 156) * 2);
		for(int i = 120; i <= 123; ++i)
			sActorSpriteMap.put(i, 252 - (i - 120) * 2);
		
		// ss
		for(int i = 198; i <= 201; ++i)
			sActorSpriteMap.put(i, 144 - (i - 198) * 2);
		for(int i = 162; i <= 165; ++i)
			sActorSpriteMap.put(i, 144 - (i - 162) * 2);
		for(int i = 126; i <= 129; ++i)
			sActorSpriteMap.put(i, 144 - (i - 126) * 2);
		for(int i = 202; i <= 205; ++i)
			sActorSpriteMap.put(i, 152 - (i - 202) * 2);
		for(int i = 166; i <= 169; ++i)
			sActorSpriteMap.put(i, 152 - (i - 166) * 2);
		for(int i = 130; i <= 133; ++i)
			sActorSpriteMap.put(i, 152 - (i - 130) * 2);
		
		// dogs
		for(int i = 206; i <= 209; ++i)
			sActorSpriteMap.put(i, 105 - (i - 206) * 2);
		for(int i = 170; i <= 173; ++i)
			sActorSpriteMap.put(i, 105 - (i - 170) * 2);
		for(int i = 134; i <= 137; ++i)
			sActorSpriteMap.put(i, 105 - (i - 134) * 2);
		for(int i = 210; i <= 213; ++i)
			sActorSpriteMap.put(i, 113 - (i - 210) * 2);
		for(int i = 174; i <= 177; ++i)
			sActorSpriteMap.put(i, 113 - (i - 174) * 2);
		for(int i = 138; i <= 141; ++i)
			sActorSpriteMap.put(i, 113 - (i - 138) * 2);
		
		sActorSpriteMap.put(214, 296);	// hans
		sActorSpriteMap.put(197, 385);	// gretel
		sActorSpriteMap.put(215, 360);	// gift
		sActorSpriteMap.put(179, 396);	// fat
		sActorSpriteMap.put(196, 307);	// doctor
		sActorSpriteMap.put(160, 321);	// fake
		sActorSpriteMap.put(178, 334);	// hitler
		
		// mutants
		for(int i = 252; i <= 255; ++i)
			sActorSpriteMap.put(i, 193 - (i - 252) * 2);
		for(int i = 234; i <= 237; ++i)
			sActorSpriteMap.put(i, 193 - (i - 234) * 2);
		for(int i = 216; i <= 219; ++i)
			sActorSpriteMap.put(i, 193 - (i - 216) * 2);
		for(int i = 256; i <= 259; ++i)
			sActorSpriteMap.put(i, 201 - (i - 256) * 2);
		for(int i = 238; i <= 241; ++i)
			sActorSpriteMap.put(i, 201 - (i - 238) * 2);
		for(int i = 220; i <= 223; ++i)
			sActorSpriteMap.put(i, 201 - (i - 220) * 2);
		
		// evil ghosts
		sActorSpriteMap.put(224, 288);
		sActorSpriteMap.put(225, 292);
		sActorSpriteMap.put(226, 290);
		sActorSpriteMap.put(227, 294);
	}
	
	/**
	 * Initializes the global vars
	 * @param context the relevant context
	 */
	public static void initialize(Activity context)
	{
		if(sInitialized)
			return;
		Log.i(TAG, "Initializing...");
		sInitialized = true;
				
		sScale = context.getResources().getDisplayMetrics().density;
	}
	
	public static String[] getWolfFileNames()
	{
		return sWolfFileNames;
	}
	
	public static SparseIntArray getActorSpriteMap()
	{
		return sActorSpriteMap;
	}
	
	public static float getScale()
	{
		return sScale;
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
	
	public static int boundValue(int val, int min, int max)
	{
		if(val < min)
			val = min;
		else if(val > max)
			val = max;
		return val;
	}
	
	public static boolean inBounds(int val, int min, int max)
	{
		return val >= min && val <= max;
	}
}
