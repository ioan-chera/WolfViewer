package org.i_chera.wolfensteineditor.document;

/*
 * Wolfenstein 3D editor for Android
 * Copyright (C) 2015  Ioan Chera
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

import org.i_chera.wolfensteineditor.DefinedSizeObject;
import org.i_chera.wolfensteineditor.FileUtil;
import org.i_chera.wolfensteineditor.Palette;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class VSwapContainer implements DefinedSizeObject{
//    private int							mNumChunks;
    private int mSpriteStart;
    private int mSoundStart;
    private ArrayList<byte[]> mPages;

    private LruCache<Integer, Bitmap> mWallBitmapCache;
    private LruCache<Integer, Bitmap> mSpriteBitmapCache;

    @Override
    public int getSizeInBytes()
    {
        int size = 0;
        if(mPages != null)
        {
            for(byte[] page : mPages)
            {
                size += page.length;
            }
        }

        Log.i("VSwapContainer", "Pages size: " + size);

        size += getLruCacheSize(mWallBitmapCache);
        Log.i("VSwapContainer", "Pages+wall size: " + size);

        size += getLruCacheSize(mSpriteBitmapCache);

        Log.i("VSwapContainer", "Size: " + size);

        return size;
    }

    private int getLruCacheSize(LruCache<Integer, Bitmap> cache)
    {
        int size = 0;
        if(cache != null)
        {
            for(Bitmap bitmap : cache.snapshot().values())
            {
                if(bitmap != null)
                    size += bitmap.getWidth() * bitmap.getHeight() * 4;
            }
        }
        return size;
    }

    /**
     * Gets a bitmap from a given wall texture
     * @param n Index of wall texture
     * @return Null if invalid index or not a wall texture, the bitmap otherwise
     */
    public Bitmap getWallBitmap(int n)
    {
        if(n < 0 || n >= mSpriteStart)
            return null;

        if(mPages.get(n).length < 64 * 64)
            return null;

        if(mWallBitmapCache == null)
        {
            mWallBitmapCache = new LruCache<>(mSpriteStart);
        }

        Bitmap bmp = mWallBitmapCache.get(n);
        if(bmp == null)
        {
            int[] ret = new int[64 * 64];
            for(int i = 0; i < ret.length; ++i)
                ret[64 * (i % 64) + i / 64] = Palette.WL6[mPages.get(n)[i] & 0xff];
            bmp = Bitmap.createBitmap(ret, 64, 64, Bitmap.Config.ARGB_8888);
            mWallBitmapCache.put(n, bmp);
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
        if(n < 0 || n >= mSoundStart - mSpriteStart)
            return null;

        if(mSpriteBitmapCache == null)
        {
            mSpriteBitmapCache = new LruCache<>(mSoundStart - mSpriteStart);
        }

        Bitmap bmp = mSpriteBitmapCache.get(n);

        if(bmp == null)
        {
            bmp = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888);

            byte[] data = mPages.get(mSpriteStart + n);
            int leftPixel = FileUtil.readUInt16(data, 0);
            int rightPixel = FileUtil.readUInt16(data, 2);
            if(leftPixel < 0 || leftPixel >= 64 || rightPixel < 0 ||
                    rightPixel >= 64 || rightPixel < leftPixel)
                return null;
            int directoryOffset;
            int x, y, i, j, topPixel, bottomPixel, postStart;
            for(x = leftPixel, i = 4; x <= rightPixel; ++x, i += 2)
            {
                directoryOffset = FileUtil.readUInt16(data, i);
                j = directoryOffset;
                for(;;)
                {
                    bottomPixel = FileUtil.readUInt16(data, j) / 2;
                    j += 2;
                    if(bottomPixel == 0)
                        break;
                    postStart = FileUtil.readInt16(data, j);
                    j += 2;
                    topPixel = FileUtil.readUInt16(data, j) / 2;
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
            mSpriteBitmapCache.put(n, bmp);
        }

        return bmp;
    }

    /**
     * Number of chunks
     * @return Total number of chunks
     */
//    public int getNumChunks()
//    {
//        return mNumChunks;
//    }

    /**
     *
     * @return Index of first sprite
     */
    public int getSpriteStart()
    {
        return mSpriteStart;
    }

    /**
     *
     * @return Index of first sound chunk
     */
//    public int getSoundStart()
//    {
//        return mSoundStart;
//    }

    /**
     * Raw data of given chunk
     * @param n Index of chunk
     * @return a byte array
     */
//    public byte[] getPage(int n)
//    {
//        return mPages.get(n);
//    }

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

            int newNumChunks = FileUtil.readUInt16(raf);
            int newSpriteStart = FileUtil.readUInt16(raf);
            int newSoundStart = FileUtil.readUInt16(raf);

            int[] pageOffsets = new int[newNumChunks + 1];
            for(int i = 0; i < newNumChunks; ++i)
                pageOffsets[i] = FileUtil.readInt32(raf);

            int[] pageLengths = new int[newNumChunks];
            for(int i = 0; i < newNumChunks; ++i)
                pageLengths[i] = FileUtil.readUInt16(raf);

            pageOffsets[newNumChunks] = (int)file.length();

            ArrayList<byte[]> newPages = new ArrayList<>();

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
//            mNumChunks = newNumChunks;
            mSpriteStart = newSpriteStart;
            mSoundStart = newSoundStart;
            mPages = newPages;

        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
            return false;
        }
        finally
        {
            FileUtil.close(raf);
        }
        return true;
    }

    public boolean writeFile(File file)
    {
        // TODO: write atomically and delete on error
        OutputStream stream = null;
        try
        {
            stream = new BufferedOutputStream(new FileOutputStream(file));
            int position = 6 + (mPages.size() + 1) * 4 + mPages.size() * 2;
            FileUtil.writeInt16(stream, mPages.size());
            FileUtil.writeInt16(stream, mSpriteStart);
            FileUtil.writeInt16(stream, mSoundStart);

            for(int i = 0; i < mPages.size(); ++i)
            {
                FileUtil.writeInt32(stream, position);
                position += mPages.get(i).length;
            }
            FileUtil.writeInt32(stream, position);

            for(int i = 0; i < mPages.size(); ++i)
            {
                FileUtil.writeInt16(stream, mPages.get(i).length);
            }

            for(int i = 0; i < mPages.size(); ++i)
            {
                stream.write(mPages.get(i));
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }
        finally
        {
            FileUtil.close(stream);
        }
        return true;
    }

}
