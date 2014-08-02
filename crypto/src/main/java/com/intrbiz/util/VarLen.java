package com.intrbiz.util;

import java.nio.ByteBuffer;

public class VarLen
{   
    public static final int INT32_MAX_LENGTH = 5;
    
    public static final int INT64_MAX_LENGTH = 10;
    
    public static final int readInt32(ByteBuffer buffer)
    {
        int v = 0;
        for (int c = 0; c < 32; c += 7)
        {
            int b = (int) buffer.get();
            v = v | ((b & 0x7F) << c);
            if ((b & 0x80) == 0) break;
        }
        return v;
    }
    
    public static final void writeInt32(int value, ByteBuffer buffer)
    {
        do
        {
            int b = (int) (value & 0x7F);
            value = value >>> 7;
            if (value > 0) b = b | 0x80;
            buffer.put((byte) (b & 0xFF));
        }
        while (value > 0);
    }
    
    public static final long readInt64(ByteBuffer buffer)
    {
        long v = 0;
        for (int c = 0; c < 64; c += 7)
        {
            long b = (long) buffer.get();
            v = v | ((b & 0x7FL) << c);
            if ((b & 0x80) == 0) break;
        }
        return v;
    }
    
    public static final void writeInt64(long value, ByteBuffer buffer)
    {
        do
        {
            int b = (int) (value & 0x7F);
            value = value >>> 7;
            if (value > 0) b = b | 0x80;
            buffer.put((byte) (b & 0xFF));
        }
        while (value > 0);
    }
    
    public static final int lenInt32(int value)
    {
        int len = 0 ;
        do
        {
            int b = (int) (value & 0x7F);
            value = value >>> 7;
            if (value > 0) b = b | 0x80;
            len++;
        }
        while (value > 0);
        return len;
    }
    
    public static final int lenInt64(long value)
    {
        int len = 0;
        do
        {
            int b = (int) (value & 0x7F);
            value = value >>> 7;
            if (value > 0) b = b | 0x80;
            len++;
        }
        while (value > 0);
        return len;
    }
}
