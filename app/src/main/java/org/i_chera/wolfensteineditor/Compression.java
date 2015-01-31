package org.i_chera.wolfensteineditor;

import java.util.ArrayList;

public class Compression {
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

    //
    // Compression
    //

    public static ArrayList<Short> rlewCompress(short[] source, short rlewTag, int prefixPad)
    {
        ArrayList<Short> dest = new ArrayList<>(source.length);
        for(int i = 0; i < prefixPad; ++i)
        {
            dest.add((short)0);
        }

        short value, count, i;

        int sourceIndex = 0;

        do
        {
            count = 1;
            value = source[sourceIndex++];
            while(sourceIndex < source.length && source[sourceIndex] == value)
            {
                ++count;
                ++sourceIndex;
            }
            if(count > 3 || value == rlewTag)
            {
                dest.add(rlewTag);
                dest.add(count);
                dest.add(value);
            }
            else
            {
                for(i = 1; i <= count; ++i)
                {
                    dest.add(value);
                }
            }
        }while(sourceIndex < source.length);

        return dest;
    }

    public static ArrayList<Byte> carmackCompress(ArrayList<Short> source, int prefixPad)
    {
        ArrayList<Byte> out = new ArrayList<>(2 * source.size());

        for(int i = 0; i < prefixPad; ++i)
        {
            out.add((byte)0);
        }

        int outshort;

        int instart = 0;
        int inptr = 0;

        int ch;
        int chhigh;

        int length = source.size();

        int beststring;
        int inscan;
        int maxstring;
        int string;
        int bestscan = 0;

        do
        {
            ch = source.get(inptr) & 0xffff;
            beststring = 0;
            for(inscan = instart; inscan < inptr; ++inscan)
            {
                if((source.get(inscan) & 0xffff) != ch)
                    continue;

                maxstring = inptr - inscan;
                if(maxstring > length)
                    maxstring = length;
                if(maxstring > 255)
                    maxstring = 255;

                string = 1;
                while(string < maxstring && source.get(inscan + string).equals(source.get(inptr + string)))
                {
                    ++string;
                }
                if(string >= beststring)
                {
                    beststring = string;
                    bestscan = inscan;
                }
            }
            if(beststring > 1 && inptr - bestscan <= 255)
            {
                outshort = beststring + NEARTAG * 256;
                out.add((byte)(outshort & 0xff));
                out.add((byte)(outshort >>> 8));
                out.add((byte)(inptr - bestscan));
                inptr += beststring;
                length -= beststring;
            }
            else if(beststring > 2)
            {
                outshort = beststring + FARTAG * 256;
                out.add((byte)(outshort & 0xff));
                out.add((byte)(outshort >>> 8));
                outshort = bestscan - instart;
                out.add((byte)(outshort & 0xff));
                out.add((byte)(outshort >>> 8));
                inptr += beststring;
                length -= beststring;
            }
            else
            {
                chhigh = ch >>> 8;
                if(chhigh == NEARTAG || chhigh == FARTAG)
                {
                    outshort = ch & 0xff00;
                    out.add((byte)(outshort & 0xff));
                    out.add((byte)(outshort >>> 8));
                    out.add((byte)(ch & 0xff));
                }
                else
                {
                    out.add((byte)(ch & 0xff));
                    out.add((byte)(ch >>> 8));
                }
                ++inptr;
                --length;
            }
            if(length < 0)
                throw new IllegalStateException("Length < 0!");
        } while(length > 0);
        return out;
    }
}
