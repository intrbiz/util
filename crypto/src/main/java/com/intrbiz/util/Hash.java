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
    
    public static final byte[] sha256(byte[]... data)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance(SHA256);
            for (byte[] d : data)
            {
                md.digest(d);
            }
            return md.digest();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("No implementation for SHA-256", e);
        }
    }
    
    public static final byte[] sha512(byte[]... data)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance(SHA512);
            for (byte[] d : data)
            {
                md.digest(d);
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
                md.digest(d);
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
                md.digest(d);
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
}
