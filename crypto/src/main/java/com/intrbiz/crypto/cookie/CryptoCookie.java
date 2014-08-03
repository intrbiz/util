package com.intrbiz.crypto.cookie;

import static com.intrbiz.util.Hash.sha256;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;

import com.intrbiz.crypto.SecretKey;
import com.intrbiz.util.VarLen;

public class CryptoCookie
{    
    private final byte[] token;

    private final long expiryTime;
    
    private final int rebaked;

    private final long flags;

    private byte[] signatue;

    public CryptoCookie(long expiryTime, int rebaked, long flags, byte[] token)
    {
        super();
        //
        if (token == null) token = new byte[0];
        //
        this.expiryTime = expiryTime;
        this.rebaked = rebaked;
        this.flags = flags;
        this.token = token;
    }

    public CryptoCookie(long expiryTime, int rebaked, long flags, byte[] token, byte[] signature)
    {
        this(expiryTime, rebaked, flags, token);
        this.signatue = signature;
    }
    
    public CryptoCookie(long expiryTime, long flags, byte[] token, byte[] signature)
    {
        this(expiryTime, 0, flags, token);
        this.signatue = signature;
    }
    
    public CryptoCookie(long expiryTime, long flags, byte[] token)
    {
        this(expiryTime, 0, flags, token);
    }

    /**
     * The token
     */
    public byte[] getToken()
    {
        return token;
    }

    /**
     * When does this cookie expire (in System.currentTimeMillis())
     */
    public long getExpiryTime()
    {
        return expiryTime;
    }
    
    /**
     * How many times was this cookie rebaked
     */
    public int getRebaked()
    {
        return this.rebaked;
    }

    /**
     * Has the cookie expired?
     * 
     * The cookie expires if the encoded expiry time is before the current system time (in Unix time).
     * 
     * A cookie with a negative expiry time never expires.
     * 
     * Note:
     *   isActive() != isExpired()
     */
    public boolean isExpired()
    {
        return this.expiryTime < System.currentTimeMillis() && this.expiryTime >= 0;
    }
    
    /**
     * Is the cookie still active?
     * 
     * The cookie is active if the encoded expiry time is after the current system time (in Unix time).
     * 
     * A cookie with a negative expiry time is always active
     * 
     * Note:
     *   isActive() != isExpired()
     */
    public boolean isActive()
    {
        return this.expiryTime < 0 || this.expiryTime >= System.currentTimeMillis();
    }

    /**
     * Flags
     */
    public long getFlags()
    {
        return flags;
    }

    /**
     * Is the given flag set
     * @param flag the flag to test for
     * @return true if the flag is set
     */
    public boolean isFlagSet(Flag flag)
    {
        return (this.flags & flag.mask) != 0;
    }

    /**
     * Are all the given flags set
     * @param flags the flags to test for
     * @return true if all flags are set
     */
    public boolean isFlagsSet(Flag... flags)
    {
        for (Flag flag : flags)
        {
            if (!this.isFlagSet(flag)) return false;
        }
        return true;
    }

    /**
     * Get the signature
     */
    public byte[] getSignatue()
    {
        return signatue;
    }

    //

    /**
     * Sign this cookie with the given key
     * @param key - the key to sign with
     */
    public void sign(SecretKey key)
    {
        if (this.signatue == null)
        {
            this.signatue = sha256(this.packData(), key.asBytes());
        }
    }

    /**
     * Verify that this cookie was signed by the given key
     * @param key - the key to verify against
     * @return
     */
    public boolean verifySignature(SecretKey key)
    {
        byte[] sig = sha256(this.packData(), key.asBytes());
        return Arrays.equals(this.signatue, sig);
    }
    
    /**
     * Verify that this cookie was signed by the given key and that this cookie is active
     * 
     * This is the same as:
     *   cookie.verifySignature(key) && cookie.isActive();
     * 
     * @param key - the key to verify against
     * @return
     */
    public boolean verify(SecretKey key)
    {
        return this.verifySignature(key) && this.isActive();
    }

    /*
     * Layout: data + signature
     */
    public byte[] toBytes()
    {
        byte[] data = this.packData();
        ByteBuffer buffer = ByteBuffer.allocate(data.length + (this.signatue == null ? 0 : this.signatue.length));
        buffer.put(data);
        if (this.signatue != null) buffer.put(this.signatue);
        buffer.flip();
        byte[] cookie = new byte[buffer.limit()];
        buffer.get(cookie);
        return cookie;
    }
    
    public String toString()
    {
        return Base64.encodeBase64URLSafeString(this.toBytes());
    }

    public static CryptoCookie fromBytes(byte[] data) throws IOException
    {
        try
        {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            // read header
            long expiry  = VarLen.readInt64(buffer);
            int rebaked = VarLen.readInt32(buffer);
            long flags   = VarLen.readInt64(buffer);
            int tknLen   = VarLen.readInt32(buffer);
            // validate token length
            if (tknLen > buffer.remaining()) throw new IOException("Malformed CryptoCookie");
            if (tknLen < 0) throw new IOException("Malformed CryptoCookie");
            // compute signature length
            int sigLen = buffer.remaining() - tknLen;
            // validate signature length
            if (sigLen < 0) throw new IOException("Malformed CryptoCookie");
            if (sigLen == 0) throw new IOException("Unsigned CryptoCookie");
            // read token
            byte[] token = new byte[tknLen];
            buffer.get(token);
            // read signature
            byte[] sig = new byte[sigLen];
            buffer.get(sig);
            // create
            return new CryptoCookie(expiry, rebaked, flags, token, sig);
        }
        catch (IOException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new IOException("Malformed CryptoCookie", e);
        }
    }
    
    public static CryptoCookie fromString(String data) throws IOException
    {
        return fromBytes(Base64.decodeBase64(data));
    }

    /*
     * Layout: varLen64(expiry) + varLen32(extended) + varLen64(flags) + varLen32(token.length) + token
     */
    private byte[] packData()
    {
        ByteBuffer buffer = ByteBuffer.allocate(24 + this.token.length);
        VarLen.writeInt64(this.expiryTime, buffer);
        VarLen.writeInt32(this.rebaked, buffer);
        VarLen.writeInt64(this.flags, buffer);
        VarLen.writeInt32(this.token.length, buffer);
        buffer.put(this.token);
        buffer.flip();
        byte[] data = new byte[buffer.limit()];
        buffer.get(data);
        return data;
    }

    /**
     * Flags that can be set on a cookie
     */
    public static class Flag
    {
        public final long mask;

        public Flag(long mask)
        {
            this.mask = mask;
        }
    }
}
