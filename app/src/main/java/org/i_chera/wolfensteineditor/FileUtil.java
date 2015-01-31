package org.i_chera.wolfensteineditor;

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

import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public class FileUtil {
    /**
     * Reads an unsigned short
     * @param fis file input stream
     * @return the read value
     * @throws IOException
     */
    public static int readUInt16(InputStream fis) throws IOException
    {
        byte[] read = new byte[2];
        if(fis.read(read, 0, 2) != 2)
            throw new EOFException("Tried to read 2 bytes past EOF");
        return (read[0] & 0xff) + ((read[1] & 0xff) << 8);
    }
    public static int readUInt16(RandomAccessFile raf) throws IOException
    {
        byte[] read = new byte[2];
        raf.readFully(read);
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
    public static int readInt32(InputStream fis) throws IOException
    {
        byte[] read = new byte[4];
        if(fis.read(read, 0, 4) != 4)
            throw new EOFException("Tried to read 4 bytes past EOF");
        return (read[0] & 0xff) + ((read[1] & 0xff) << 8)
                + ((read[2] & 0xff) << 16)
                + ((read[3] & 0xff) << 24);
    }
    public static int readInt32(RandomAccessFile raf) throws IOException
    {
        byte[] read = new byte[4];
        raf.readFully(read);
        return (read[0] & 0xff) + ((read[1] & 0xff) << 8)
                + ((read[2] & 0xff) << 16)
                + ((read[3] & 0xff) << 24);
    }

    public static void writeInt16(OutputStream stream, int n) throws IOException
    {
        stream.write(n);
        stream.write(n >>> 8);
    }

    public static void writeInt32(OutputStream stream, int n) throws IOException
    {
        stream.write(n);
        stream.write(n >>> 8);
        stream.write(n >>> 16);
        stream.write(n >>> 24);
    }

    public static void close(Closeable closeable)
    {
        if(closeable != null)
        {
            try
            {
                closeable.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static boolean deleteRecursively(File file)
    {
        File[] subfiles = file.listFiles();
        boolean allDelete = true;
        if(subfiles != null)
            for (File child : subfiles)
                allDelete &= deleteRecursively(child);

        return allDelete && file.delete();
    }
}
