package com.intrbiz.crypto;

import java.security.SecureRandom;

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
        return "";
    }
    
    public static final SecretKey fromBytes(byte[] key)
    {
        return new SecretKey(key);
    }
    
    public static final SecretKey fromString(String s)
    {
        return new SecretKey(null);
    }
    
    public static final SecretKey generate()
    {
        byte[] b = new byte[32];
        new SecureRandom().nextBytes(b);
        return new SecretKey(b);
    }
}
