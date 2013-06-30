package com.intrbiz.crypto;

import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base64;

public final class SecretKey
{
    private final byte[] key;
    
    private SecretKey(byte[] key)
    {
        this.key = key;
    }
    
    public byte[] asBytes()
    {
        return this.key;
    }
    
    public byte[] toBytes()
    {
        return this.key;
    }
    
    public String toString()
    {
        return Base64.encodeBase64String(this.key);
    }
    
    public static final SecretKey fromBytes(byte[] key)
    {
        return new SecretKey(key);
    }
    
    public static final SecretKey fromString(String s)
    {
        return new SecretKey(Base64.decodeBase64(s));
    }
    
    public static final SecretKey generate(int len)
    {
        byte[] b = new byte[len];
        new SecureRandom().nextBytes(b);
        return new SecretKey(b);
    }
    
    public static final SecretKey generate()
    {
        return generate(128);
    }
}
