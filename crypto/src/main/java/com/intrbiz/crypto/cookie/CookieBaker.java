package com.intrbiz.crypto.cookie;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import com.intrbiz.crypto.SecretKey;
import com.intrbiz.crypto.cookie.CryptoCookie.Flag;

public class CookieBaker
{
    private SecretKey key;
    
    private long lifetime = TimeUnit.MINUTES.toMillis(60);
    
    private Flag[] flags = null;
    
    private int tokenLength = 1024;
    
    private SecureRandom random = new SecureRandom();
    
    public CookieBaker(SecretKey key, int tokenLength)
    {
        super();
        this.key = key;
        this.tokenLength = tokenLength;
    }
    
    public CookieBaker(SecretKey key, int tokenLength, long lifetime, TimeUnit unit)
    {
        this(key, tokenLength);
        this.lifetime = unit.toMillis(lifetime);
    }
    
    public CookieBaker(SecretKey key, int tokenLength, long lifetime, TimeUnit unit, Flag... flags)
    {
        this(key, tokenLength, lifetime, unit);
        this.flags = flags;
    }
    
    public SecretKey getKey()
    {
        return this.key;
    }
    
    public long getLifetime()
    {
        return this.lifetime;
    }
    
    public long getLifetime(TimeUnit unit)
    {
        return unit.convert(this.lifetime, TimeUnit.MILLISECONDS);
    }
    
    public Flag[] getFlags()
    {
        return this.flags;
    }
    
    //
    
    protected byte[] generateToken()
    {
        byte[] token = new byte[this.tokenLength];
        this.random.nextBytes(token);
        return token;
    }
    
    protected long expiresAt()
    {
        return System.currentTimeMillis() + this.lifetime;
    }
    
    protected long flags(Flag... flags)
    {
        long f = 0;
        if (flags != null)
        {
            for (Flag flag : flags)
            {
                f |= flag.mask;
            }
        }
        return f;
    }
    
    protected CryptoCookie bake(byte[] token, long expiresAt, long flags)
    {
        CryptoCookie cc = new CryptoCookie(expiresAt, flags, token);
        cc.sign(this.key);
        return cc;
    }
    
    //
    
    public CryptoCookie bake()
    {
        return this.bake(generateToken(), expiresAt(), flags(this.flags));
    }
    
    public CryptoCookie bake(long expiryTime)
    {
        return this.bake(generateToken(), expiryTime, flags(this.flags));
    }
    
    public CryptoCookie bake(Flag... flags)
    {
        return this.bake(generateToken(), expiresAt(), flags(flags));
    }
    
    public CryptoCookie bake(long expiryTime, Flag... flags)
    {
        return this.bake(generateToken(), expiryTime, flags(flags));
    }
    
    public CryptoCookie bake(byte[] token)
    {
        return this.bake(token, expiresAt(), flags(this.flags));
    }
    
    public CryptoCookie bake(byte[] token, Flag... flags)
    {
        return this.bake(token, expiresAt(), flags(flags));
    }
    
    public CryptoCookie bake(byte[] token, long expiryTime, Flag... flags)
    {
        return this.bake(token, expiryTime, flags(flags));
    }
    
    //
    
    public CryptoCookie rebake(CryptoCookie cookie)
    {
        return this.bake(cookie.getToken(), expiresAt(), flags(this.flags));
    }
    
    public CryptoCookie rebake(CryptoCookie cookie, Flag... flags)
    {
        return this.bake(cookie.getToken(), expiresAt(), flags(flags));
    }
    
    public CryptoCookie rebake(CryptoCookie cookie, long expiryTime, Flag... flags)
    {
        return this.bake(cookie.getToken(), expiryTime, flags(flags));
    }
    
    //
    
    public boolean verify(CryptoCookie cookie)
    {
        return cookie.verify(this.key);
    }
}
