package com.intrbiz.util;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash
{
    private static final Charset UTF8 = Charset.forName("UTF8");
    
    public static final String MD5    = "MD5";
    public static final String SHA1   = "SHA1";
    public static final String SHA256 = "SHA-256";
    public static final String SHA512 = "SHA-512";
    
    public static final int SHA1_LENGTH = 20;
    public static final int SHA256_LENGTH = 32;
    
    public static final int HMAC_SHA1_BLOCKSIZE = 64;
    
    private static final byte HMAC_OPAD = 0x5C;
    private static final byte HMAC_IPAD = 0x36;
    
    public static final byte[] sha256HMAC(byte[] key, BufferSlice... slices)
    {
        // sort out the key
        if (key.length > SHA256_LENGTH)
        {
            key = sha256(key);
        }
        if (key.length < SHA256_LENGTH)
        {
            byte[] tmp = new byte[SHA256_LENGTH];
            System.arraycopy(key, 0, tmp, 0, key.length);
            key = tmp;
        }
        return sha256(xor(key, HMAC_OPAD), sha256Keyed(xor(key, HMAC_IPAD), slices));
    }
    
    public static final byte[] sha1HMAC(byte[] key, BufferSlice... slices)
    {
        // sort out the key
        if (key.length > HMAC_SHA1_BLOCKSIZE)
        {
            key = sha1(key);
        }
        if (key.length < HMAC_SHA1_BLOCKSIZE)
        {
            byte[] tmp = new byte[HMAC_SHA1_BLOCKSIZE];
            System.arraycopy(key, 0, tmp, 0, key.length);
            key = tmp;
        }
        return sha1(xor(key, HMAC_OPAD), sha1Keyed(xor(key, HMAC_IPAD), slices));
    }
    
    private static final byte[] sha256Keyed(byte[] key, BufferSlice... slices)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance(SHA256);
            md.update(key);
            for (BufferSlice slice : slices)
            {
                md.update(slice.buffer, slice.offset, slice.length);
            }
            return md.digest();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("No implementation for SHA-256", e);
        }
    }
    
    private static final byte[] sha1Keyed(byte[] key, BufferSlice... slices)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance(SHA1);
            md.update(key);
            for (BufferSlice slice : slices)
            {
                md.update(slice.buffer, slice.offset, slice.length);
            }
            return md.digest();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("No implementation for SHA-256", e);
        }
    }
    
    
    public static final byte[] sha256HMAC(byte[] key, byte[]... data)
    {
        // sort out the key
        if (key.length > SHA256_LENGTH)
        {
            key = sha256(key);
        }
        if (key.length < SHA256_LENGTH)
        {
            byte[] tmp = new byte[SHA256_LENGTH];
            System.arraycopy(key, 0, tmp, 0, key.length);
            key = tmp;
        }
        return sha256(xor(key, HMAC_OPAD), sha256Keyed(xor(key, HMAC_IPAD), data));
    }
    
    public static final byte[] sha1HMAC(byte[] key, byte[]... data)
    {
        // sort out the key
        if (key.length > HMAC_SHA1_BLOCKSIZE)
        {
            key = sha1(key);
        }
        if (key.length < HMAC_SHA1_BLOCKSIZE)
        {
            byte[] tmp = new byte[HMAC_SHA1_BLOCKSIZE];
            System.arraycopy(key, 0, tmp, 0, key.length);
            key = tmp;
        }
        return sha1(xor(key, HMAC_OPAD), sha1Keyed(xor(key, HMAC_IPAD), data));
    }
    
    private static final byte[] xor(byte[] data, byte val)
    {
        byte[] out = new byte[data.length];
        for (int i = 0; i < data.length; i++)
        {
            out[i] = (byte) (data[i] ^ val);
        }
        return out;
    }
    
    private static final byte[] sha256Keyed(byte[] key, byte[]... data)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance(SHA256);
            md.update(key);
            for (byte[] d : data)
            {
                md.update(d);
            }
            return md.digest();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("No implementation for SHA-256", e);
        }
    }
    
    private static final byte[] sha1Keyed(byte[] key, byte[]... data)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance(SHA1);
            md.update(key);
            for (byte[] d : data)
            {
                md.update(d);
            }
            return md.digest();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("No implementation for SHA-1", e);
        }
    }
    
    public static final byte[] sha256(BufferSlice... slices)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance(SHA256);
            for (BufferSlice slice : slices)
            {
                md.update(slice.buffer, slice.offset, slice.length);
            }
            return md.digest();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("No implementation for SHA-256", e);
        }
    }
    
    public static final byte[] sha256(byte[]... data)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance(SHA256);
            for (byte[] d : data)
            {
                md.update(d);
            }
            return md.digest();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("No implementation for SHA-256", e);
        }
    }
    
    public static final byte[] sha512(BufferSlice... slices)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance(SHA512);
            for (BufferSlice slice : slices)
            {
                md.update(slice.buffer, slice.offset, slice.length);
            }
            return md.digest();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("No implementation for SHA-512", e);
        }
    }
    
    public static final byte[] sha512(byte[]... data)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance(SHA512);
            for (byte[] d : data)
            {
                md.update(d);
            }
            return md.digest();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("No implementation for SHA-512", e);
        }
    }
    
    public static final byte[] md5(byte[]... data)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance(MD5);
            for (byte[] d : data)
            {
                md.update(d);
            }
            return md.digest();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("No implementation for MD5", e);
        }
    }
    
    public static final byte[] sha1(byte[]... data)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance(SHA1);
            for (byte[] d : data)
            {
                md.update(d);
            }
            return md.digest();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("No implementation for SHA1", e);
        }
    }
    
    public static final byte[] asUTF8(String str)
    {
        return str.getBytes(UTF8);
    }
    
    private static final char[] HEX_NIBBLE = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    
    public static final String toHex(byte[] b)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; i++)
        {
            sb.append(HEX_NIBBLE[(b[i] >> 4) & 0xF]);
            sb.append(HEX_NIBBLE[b[i] & 0xF]);
        }
        return sb.toString();
    }
    
    public static final class BufferSlice
    {
        public final byte[] buffer;
        
        public final int offset;
        
        public final int length;

        public BufferSlice(byte[] buffer, int offset, int length)
        {
            super();
            this.buffer = buffer;
            this.offset = offset;
            this.length = length;
        }
        
        public BufferSlice(byte[] buffer)
        {
            super();
            this.buffer = buffer;
            this.offset = 0;
            this.length = buffer.length;
        }
    }
}
