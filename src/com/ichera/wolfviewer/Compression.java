package com.ichera.wolfviewer;

public class Compression 
{
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
			ch = FileUtil.readUInt16(source, inIndex);
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
					offset = FileUtil.readUInt16(source, inIndex);
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
			value = FileUtil.readUInt16(source, inIndex);
			inIndex += 2;
			if(value != rlewTag)
			{
				dest[outIndex++] = (short)(value & 0xffff);
			}
			else
			{
				count = FileUtil.readUInt16(source, inIndex);
				inIndex += 2;
				value = FileUtil.readUInt16(source, inIndex);
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
