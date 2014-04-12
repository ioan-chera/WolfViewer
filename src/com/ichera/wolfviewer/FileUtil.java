package com.ichera.wolfviewer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileUtil 
{
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
	
}
