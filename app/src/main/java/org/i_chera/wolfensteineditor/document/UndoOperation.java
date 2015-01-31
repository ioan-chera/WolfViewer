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

import android.os.Parcel;
import android.os.Parcelable;

import org.i_chera.wolfensteineditor.DefinedSizeObject;
import org.i_chera.wolfensteineditor.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class UndoOperation implements Parcelable, DefinedSizeObject
{
    // Important: these must be in sequence, from 0
    public static final int SET_TILE = 0;

    // Important: array must match values in opcode types above
    private static final String[] sSignature = new String[]
            {
                    "iiis", // SET_TILE
            };

    private final int mOpcode;
    private final ArrayList<Object> mArguments;

    public UndoOperation(int opcode, Object... arguments)
    {
        mOpcode = opcode;
        if(mOpcode < 0 || mOpcode >= sSignature.length)
            throw new IllegalArgumentException("Bad opcode " + opcode);
        if(arguments.length != sSignature[opcode].length())
            throw new IllegalArgumentException("Opcode " + opcode + " has wrong number of arguments ("
                    + arguments.length + " instead of " + sSignature[opcode].length());

        mArguments = new ArrayList<>(arguments.length);

        char c;
        Object object;
        Class<?> requiredClass;
        for(int i = 0; i < arguments.length; ++i)
        {
            c = sSignature[opcode].charAt(i);
            object = arguments[i];
            switch(c)
            {
                case 'i':
                    requiredClass = Integer.class;
                    break;
                case 's':
                    requiredClass = Short.class;
                    break;
                default:
                    throw new IllegalArgumentException("Bad signature " + sSignature[opcode]
                            + " for opcode " + opcode);
            }
            if(object.getClass() != requiredClass)
                throw new IllegalArgumentException("Bad type for " + object + ", required " + requiredClass);

            mArguments.add(object);
        }
    }

    public UndoOperation(InputStream stream) throws IOException
    {
        mOpcode = FileUtil.readInt32(stream);
        mArguments = new ArrayList<>(5);
        char c;
        for(int i = 0; i < sSignature[mOpcode].length(); ++i)
        {
            c = sSignature[mOpcode].charAt(i);
            switch(c)
            {
                case 'i':
                    mArguments.add(FileUtil.readInt32(stream));
                    break;
                case 's':
                    mArguments.add((short)FileUtil.readUInt16(stream));
                    break;
                default:
                    throw new IllegalArgumentException("Bad signature " + sSignature[mOpcode]
                            + " for opcode " + mOpcode);
            }
        }
    }

    public void executeForLevels(LevelContainer levels)
    {
        switch(mOpcode)
        {
            case SET_TILE:
                levels.setTile((int)mArguments.get(0), (int)mArguments.get(1),
                        (int)mArguments.get(2), (short)mArguments.get(3));
                break;
            default:
                throw new IllegalArgumentException("Bad opcode " + mOpcode + " for levels");
        }
    }

    @Override
    public int getSizeInBytes()
    {
        int size = 4;
        char c;
        for(int i = 0; i < sSignature[mOpcode].length(); ++i)
        {
            c = sSignature[mOpcode].charAt(i);
            switch(c)
            {
                case 'i':
                    size += 4;
                    break;
                case 's':
                    size += 2;
                    break;
                default:
                    throw new IllegalArgumentException("Bad signature " + sSignature[mOpcode]
                            + " for opcode " + mOpcode);
            }
        }
        return size;
    }

    public byte[] getByteRepresentation()
    {
        byte[] ret = new byte[getSizeInBytes()];
        ret[0] = (byte)mOpcode;
        ret[1] = (byte)(mOpcode >>> 8);
        ret[2] = (byte)(mOpcode >>> 16);
        ret[3] = (byte)(mOpcode >>> 24);

        char c;
        int index = 4;
        int intRep;
        short shortRep;
        for(int i = 0; i < sSignature[mOpcode].length(); ++i)
        {
            c = sSignature[mOpcode].charAt(i);
            switch(c)
            {
                case 'i':
                    intRep = (int)mArguments.get(i);
                    ret[index++] = (byte)intRep;
                    ret[index++] = (byte)(intRep >>> 8);
                    ret[index++] = (byte)(intRep >>> 16);
                    ret[index++] = (byte)(intRep >>> 24);
                    break;
                case 's':
                    shortRep = (short)mArguments.get(i);
                    ret[index++] = (byte)shortRep;
                    ret[index++] = (byte)(shortRep >>> 8);
                    break;
                default:
                    throw new IllegalArgumentException("Bad signature " + sSignature[mOpcode]
                            + " for opcode " + mOpcode);
            }
        }
        return ret;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Boilerplate
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(mOpcode);
        char c;
        for(int i = 0; i < sSignature[mOpcode].length(); ++i)
        {
            c = sSignature[mOpcode].charAt(i);
            switch(c)
            {
                case 'i':
                case 's':
                    dest.writeInt((Integer)mArguments.get(i));
                    break;
                default:
                    throw new IllegalArgumentException("Bad signature " + sSignature[mOpcode] + " for " + mOpcode);
            }
        }
    }

    private UndoOperation(Parcel in)
    {
        mOpcode = in.readInt();
        mArguments = new ArrayList<>(5);
        char c;
        for(int i = 0; i < sSignature[mOpcode].length(); ++i)
        {
            c = sSignature[mOpcode].charAt(i);
            switch(c)
            {
                case 'i':
                case 's':
                    mArguments.add(in.readInt());
                    break;
                default:
                    throw new IllegalArgumentException("Bad signature " + sSignature[mOpcode] + " for " + mOpcode);
            }
        }
    }

    public static final Parcelable.Creator<UndoOperation> CREATOR = new Creator<UndoOperation>()
    {

        @Override
        public UndoOperation createFromParcel(Parcel source)
        {
            return new UndoOperation(source);
        }

        @Override
        public UndoOperation[] newArray(int size)
        {
            return new UndoOperation[size];
        }
    };
}
