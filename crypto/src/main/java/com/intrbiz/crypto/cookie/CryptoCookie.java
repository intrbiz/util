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

    private final long flags;

    private byte[] signatue;

    public CryptoCookie(long expiryTime, long flags, byte[] token)
    {
        super();
        //
        if (token == null) token = new byte[0];
        //
        this.expiryTime = expiryTime;
        this.flags = flags;
        this.token = token;
    }

    public CryptoCookie(long expiryTime, long flags, byte[] token, byte[] signature)
    {
        this(expiryTime, flags, token);
        this.signatue = signature;
    }

    public byte[] getToken()
    {
        return token;
    }

    public long getExpiryTime()
    {
        return expiryTime;
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

    public long getFlags()
    {
        return flags;
    }

    public boolean isFlagSet(Flag flag)
    {
        return (this.flags & flag.mask) != 0;
    }

    public boolean isFlagsSet(Flag... flags)
    {
        for (Flag flag : flags)
        {
            if (!this.isFlagSet(flag)) return false;
        }
        return true;
    }

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
     * Layout
     * 
     * expiry + flags + length(token) + token + signature
     */

    public byte[] toBytes()
    {
        int len = this.length();
        byte[] data = new byte[len];
        //
        ByteBuffer buffer = ByteBuffer.wrap(data);
        VarLen.writeInt64(this.expiryTime, buffer);
        VarLen.writeInt64(this.flags, buffer);
        VarLen.writeInt32(this.token.length, buffer);
        buffer.put(this.token);
        if (this.signatue != null) buffer.put(this.signatue);
        //
        return data;
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
            //
            long expiry = VarLen.readInt64(buffer);
            long flags = VarLen.readInt64(buffer);
            int tknLen = VarLen.readInt32(buffer);
            if (tknLen > buffer.remaining()) throw new IOException("Malformed CryptoCookie");
            if (tknLen < 0) throw new IOException("Malformed CryptoCookie");
            int sigLen = buffer.remaining() - tknLen;
            if (sigLen < 0) throw new IOException("Malformed CryptoCookie");
            if (sigLen == 0) throw new IOException("Unsigned CryptoCookie");
            //
            byte[] token = new byte[tknLen];
            buffer.get(token);
            byte[] sig = new byte[sigLen];
            buffer.get(sig);
            //
            return new CryptoCookie(expiry, flags, token, sig);
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

    // util

    private int length()
    {
        return this.dataLength() + (this.signatue == null ? 0 : this.signatue.length);
    }

    private int dataLength()
    {
        return VarLen.lenInt64(this.expiryTime) + VarLen.lenInt64(this.flags) + VarLen.lenInt32(this.token.length) + this.token.length;
    }

    private byte[] packData()
    {
        int len = this.dataLength();
        byte[] data = new byte[len];
        //
        ByteBuffer buffer = ByteBuffer.wrap(data);
        VarLen.writeInt64(this.expiryTime, buffer);
        VarLen.writeInt64(this.flags, buffer);
        VarLen.writeInt32(this.token.length, buffer);
        buffer.put(this.token);
        //
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
