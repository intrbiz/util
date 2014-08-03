package com.intrbiz.crypto.cookie;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import com.intrbiz.crypto.SecretKey;
import com.intrbiz.crypto.cookie.CryptoCookie.Flag;

/**
 * A CookieBaker bakes CryptoCookies, mmm cookies.
 */
public class CookieBaker
{
    /**
     * Syntactic Sugar expiry time helper
     * 
     * Eg:
     *   baker.bake(Expires.never());
     *   baker.bake(Expires.after(1, TimeUnit.HOUR));
     *   baker.bake(Expires.after(120, TimeUnit.MINUTE));
     */
    public static final class Expires
    {
        /**
         * The cookie never expires
         */
        public static final long never()
        {
            return -1;
        }
        
        /**
         * The cookie expires after the given length of time
         */
        public static final long after(long value, TimeUnit unit)
        {
            return System.currentTimeMillis() + unit.toMillis(value);
        }
    }
    
    /**
     * Syntactic Sugar rebake limit helper
     */
    public static final class Rebake
    {
        public static final int unlimited()
        {
            return -1;
        }
        
        public static final int never()
        {
            return 0;
        }
        
        public static final int limit(int limit)
        {
            return limit;
        }
    }
    
    private final SecretKey key;
    
    private final long lifetime;
    
    private final Flag[] flags;
    
    private final int tokenLength;
    
    private final SecureRandom random = new SecureRandom();
    
    private final int rebakeLimit;
    
    public CookieBaker(SecretKey key, int tokenLength, long lifetime, int rebakeLimit, Flag... flags)
    {
        super();
        this.key = key;
        this.tokenLength = tokenLength;
        this.lifetime = lifetime;
        this.rebakeLimit = rebakeLimit;
        this.flags = flags;
    }
    
    public CookieBaker(SecretKey key, int tokenLength, long lifetime, TimeUnit unit, int rebakeLimit, Flag... flags)
    {
        this(key, tokenLength, unit.toMillis(lifetime), rebakeLimit, flags);
    }
    
    public CookieBaker(SecretKey key, int tokenLength)
    {
        this(key, tokenLength, 1L, TimeUnit.HOURS, Rebake.unlimited(), (Flag[]) null);
    }
    
    public CookieBaker(SecretKey key, int tokenLength, int rebakeLimit)
    {
        this(key, tokenLength, 1L, TimeUnit.HOURS, rebakeLimit, (Flag[]) null);
    }
    
    public CookieBaker(SecretKey key, int tokenLength, long lifetime, TimeUnit unit)
    {
        this(key, tokenLength, lifetime, unit, Rebake.unlimited(), (Flag[]) null);
    }
    
    public CookieBaker(SecretKey key, int tokenLength, long lifetime, TimeUnit unit, int rebakeLimit)
    {
        this(key, tokenLength, lifetime, unit, rebakeLimit, (Flag[]) null);
    }
    
    public CookieBaker(SecretKey key, int tokenLength, long lifetime, TimeUnit unit, Flag... flags)
    {
        this(key, tokenLength, lifetime, unit, Rebake.unlimited(), flags);
    }
    
    /**
     * The key used for signing
     */
    public SecretKey getKey()
    {
        return this.key;
    }
    
    /**
     * The default cookie lifetime in milliseconds
     */
    public long getLifetime()
    {
        return this.lifetime;
    }
    
    /**
     * The default cookie
     */
    public long getLifetime(TimeUnit unit)
    {
        return unit.convert(this.lifetime, TimeUnit.MILLISECONDS);
    }
    
    /**
     * The flags to set on the cookie
     */
    public Flag[] getFlags()
    {
        return this.flags;
    }
    
    /**
     * The rebake limit
     */
    public int getRebakeLimit()
    {
        return this.rebakeLimit;
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
    
    protected CryptoCookie bake(byte[] token, long expiresAt, int rebaked, long flags)
    {
        CryptoCookie cc = new CryptoCookie(expiresAt, rebaked, flags, token);
        cc.sign(this.key);
        return cc;
    }
    
    //
    
    /**
     * Bake a cookie with a random token, default expiry time and default flags.
     * @return a freshly baked cookie
     */
    public CryptoCookie bake()
    {
        return this.bake(generateToken(), expiresAt(), 0, flags(this.flags));
    }
    
    /**
     * Bake a cookie with a random token, the given expiry time and default flags.
     * @param expiryTime when the cookie expires in (System.currentTimeMillis())
     * @return a freshly baked cookie
     */
    public CryptoCookie bake(long expiryTime)
    {
        return this.bake(generateToken(), expiryTime, 0, flags(this.flags));
    }
    
    /**
     * Bake a cookie with a random token, default expiry time and the given flags.
     * @param flags the Flags to be set on the cookie
     * @return a freshly baked cookie
     */
    public CryptoCookie bake(Flag... flags)
    {
        return this.bake(generateToken(), expiresAt(), 0, flags(flags));
    }
    
    /**
     * Bake a cookie with a random token, the given expiry time and the given flags.
     * @param expiryTime when the cookie expires in (System.currentTimeMillis())
     * @param flags the Flags to be set on the cookie
     * @return a freshly baked cookie
     */
    public CryptoCookie bake(long expiryTime, Flag... flags)
    {
        return this.bake(generateToken(), expiryTime, 0, flags(flags));
    }
    
    /**
     * Bake a cookie with the given token, default expiry time and default flags.
     * @param token the token
     * @return a freshly baked cookie
     */
    public CryptoCookie bake(byte[] token)
    {
        return this.bake(token, expiresAt(), 0, flags(this.flags));
    }
    
    /**
     * Bake a cookie with the given token, the given expiry time and default flags.
     * @param token the token
     * @param expiryTime when the cookie expires in (System.currentTimeMillis())
     * @return a freshly baked cookie
     */
    public CryptoCookie bake(byte[] token, long expiresAt)
    {
        return this.bake(token, expiresAt, 0, flags(this.flags));
    }
    
    /**
     * Bake a cookie with the given token, default expiry time and the given flags.
     * @param token the token
     * @param flags the Flags to be set on the cookie
     * @return a freshly baked cookie
     */
    public CryptoCookie bake(byte[] token, Flag... flags)
    {
        return this.bake(token, expiresAt(), 0, flags(flags));
    }
    
    /**
     * Bake a cookie with the given token, the given expiry time and the given flags.
     * @param token the token
     * @param expiryTime when the cookie expires in (System.currentTimeMillis())
     * @param flags the Flags to be set on the cookie
     * @return a freshly baked cookie
     */
    public CryptoCookie bake(byte[] token, long expiryTime, Flag... flags)
    {
        return this.bake(token, expiryTime, 0, flags(flags));
    }
    
    /**
     * Bake a cookie with the given token, the given expiry time, the given rebake count and the given flags.
     * @param token the token
     * @param expiryTime when the cookie expires in (System.currentTimeMillis())
     * @param rebaked the rebake count
     * @param flags the Flags to be set on the cookie
     * @return a freshly baked cookie
     */
    public CryptoCookie bake(byte[] token, long expiryTime, int rebaked, Flag... flags)
    {
        return this.bake(token, expiryTime, rebaked, flags(flags));
    }
    
    //
    
    /**
     * Rebake the given cookie, this will keep the token and bake a cookie 
     * with a new expiry time.  The rebake count will also be incremented, 
     * should the rebake count be greater than or equal to the rebake limit 
     * null is returned.
     * @param cookie the cookie to rebake
     * @return a cookie or null if the rebake limit is exceeded
     */
    public CryptoCookie rebake(CryptoCookie cookie)
    {
        if (this.rebakeLimit >= 0 && cookie.getRebaked() >= this.rebakeLimit) return null;
        return this.bake(cookie.getToken(), expiresAt(), cookie.getRebaked() + 1, flags(this.flags));
    }
    
    /**
     * Rebake the given cookie, this will keep the token and bake a cookie 
     * with a new expiry time.  The rebake count will also be incremented, 
     * should the rebake count be greater than or equal to the rebake limit 
     * null is returned.
     * @param cookie the cookie to rebake
     * @param flags the Flags to be set on the cookie
     * @return a cookie or null if the rebake limit is exceeded
     */
    public CryptoCookie rebake(CryptoCookie cookie, Flag... flags)
    {
        if (this.rebakeLimit >= 0 && cookie.getRebaked() >= this.rebakeLimit) return null;
        return this.bake(cookie.getToken(), expiresAt(), cookie.getRebaked() + 1, flags(flags));
    }
    
    /**
     * Rebake the given cookie, this will keep the token and bake a cookie 
     * with a new expiry time.  The rebake count will also be incremented, 
     * should the rebake count be greater than or equal to the rebake limit 
     * null is returned.
     * @param cookie the cookie to rebake
     * @param expiryTime when the cookie expires in (System.currentTimeMillis())
     * @param flags the Flags to be set on the cookie
     * @return a cookie or null if the rebake limit is exceeded
     */
    public CryptoCookie rebake(CryptoCookie cookie, long expiryTime, Flag... flags)
    {
        if (this.rebakeLimit >= 0 && cookie.getRebaked() >= this.rebakeLimit) return null;
        return this.bake(cookie.getToken(), expiryTime, cookie.getRebaked() + 1, flags(flags));
    }
    
    //
    
    /**
     * Verify that the cookie was signed by this baker and that 
     * it is still active (it hasn't yet expired).
     * @param cookie the cookie to verify
     * @return true if the cookie is all good
     */
    public boolean verify(CryptoCookie cookie)
    {
        return cookie.verify(this.key);
    }
    
    /**
     * Verify that the cookie was signed by this baker.
     * Note this doesn't verify if the cookie is still active. 
     * @param cookie the cookie to verify
     * @return true if the cookie is all good
     */
    public boolean verifySignature(CryptoCookie cookie)
    {
        return cookie.verifySignature(this.key);
    }
}
