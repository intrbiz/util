package com.intrbiz.crypto.cookie;

import static com.intrbiz.util.Hash.sha256;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;

import com.intrbiz.crypto.SecretKey;

public class CryptoCookie
{
    private final byte[] token;

    private final long expiryTime;

    private final long flags;

    private byte[] signatue;

    public CryptoCookie(long expiryTime, long flags, byte[] token)
    {
        super();
        this.expiryTime = expiryTime;
        this.flags = flags;
        this.token = token;
    }

    public CryptoCookie(long expiryTime, long flags, byte[] token, byte[] signature)
    {
        super();
        this.expiryTime = expiryTime;
        this.flags = flags;
        this.token = token;
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

    public boolean isExpired()
    {
        return this.expiryTime < System.currentTimeMillis();
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

    public void sign(SecretKey key)
    {
        if (this.signatue == null)
        {
            this.signatue = sha256(this.packData(), key.asBytes());
        }
    }

    public boolean verify(SecretKey key)
    {
        byte[] sig = sha256(this.packData(), key.asBytes());
        return Arrays.equals(this.signatue, sig);
    }

    /*
     * Layout
     * 
     * expiry + flags + length(token) + token + + signature
     */

    public byte[] toBytes()
    {
        int len = this.length();
        byte[] data = new byte[len];
        //
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.putLong(this.expiryTime);
        buffer.putLong(this.flags);
        buffer.putInt(this.token.length);
        buffer.put(this.token);
        buffer.put(this.signatue);
        //
        return data;
    }
    
    public String toString()
    {
        return Base64.encodeBase64String(this.toBytes());
    }

    public static CryptoCookie fromBytes(byte[] data) throws IOException
    {
        try
        {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            //
            long expiry = buffer.getLong();
            long flags = buffer.getLong();
            int tknLen = buffer.getInt();
            if (tknLen > buffer.remaining()) throw new IOException("Malformed CryptoCookie");
            if (tknLen <= 0) throw new IOException("Malformed CryptoCookie");
            int sigLen = buffer.remaining() - tknLen;
            if (sigLen <= 0) throw new IOException("Malformed CryptoCookie");
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
        return this.dataLength() + this.signatue.length;
    }

    private int dataLength()
    {
        return 8 + 8 + 4 + this.token.length;
    }

    private byte[] packData()
    {
        int len = this.dataLength();
        byte[] data = new byte[len];
        //
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.putLong(this.expiryTime);
        buffer.putLong(this.flags);
        buffer.putInt(this.token.length);
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
