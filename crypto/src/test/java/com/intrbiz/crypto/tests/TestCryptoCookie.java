package com.intrbiz.crypto.tests;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.security.SecureRandom;

import org.junit.Test;

import com.intrbiz.crypto.SecretKey;
import com.intrbiz.crypto.cookie.CryptoCookie;

public class TestCryptoCookie
{
    public static final byte[] TOKEN = { 1, 2, 3, 4, 5, 6, 7, 8 };
    
    public static final SecretKey KEY = SecretKey.fromString("FnaDAfV/Zg43bL+2fRCX7B5Y5PB7/9jF4adr/Z9dd58gc9vyKb8niU1ZmI+DOs8FP+Oij25s5ZFGG8F+fK7hequZeJw/MpwoW8YLyiofL7pSyfdXTeFDye5UUI9X+aN4sWUcj7BbE2TxY5eaIYr0XHljJVDw6XTFmG4Pq1aHi9U=");
    
    public static final SecureRandom RANDOM = new SecureRandom();
    
    @Test
    public void createCookie()
    {
        long expiresAt = System.currentTimeMillis() + 3600000;
        //
        CryptoCookie cookie = new CryptoCookie(expiresAt, 0, TOKEN, RANDOM.nextInt() & 0x1F);
        //
        assertThat(cookie.getToken(), is(notNullValue()));
        assertThat(cookie.getSignatue(), is(nullValue()));
        assertThat(cookie.getFlags(), is(equalTo(0L)));
        assertThat(cookie.getExpiryTime(), is(equalTo(expiresAt)));
        assertThat(cookie.getRebaked(), is(equalTo(0)));
        assertThat(cookie.getToken(), is(equalTo(TOKEN)));
        assertThat(cookie.toBytes(), is(notNullValue()));
        assertThat(cookie.toString(), is(notNullValue()));
    }
    
    @Test
    public void signCookie()
    {
        long expiresAt = System.currentTimeMillis() + 3600000;
        //
        CryptoCookie cookie = new CryptoCookie(expiresAt, 0, TOKEN, RANDOM.nextInt() & 0x1F);
        //
        assertThat(cookie.getToken(), is(notNullValue()));
        assertThat(cookie.getSignatue(), is(nullValue()));
        assertThat(cookie.getFlags(), is(equalTo(0L)));
        assertThat(cookie.getExpiryTime(), is(equalTo(expiresAt)));
        assertThat(cookie.getRebaked(), is(equalTo(0)));
        assertThat(cookie.getToken(), is(equalTo(TOKEN)));
        assertThat(cookie.toBytes(), is(notNullValue()));
        assertThat(cookie.toString(), is(notNullValue()));
        //
        cookie.sign(KEY);
        //
        assertThat(cookie.getSignatue(), is(notNullValue()));
        assertThat(cookie.toBytes(), is(notNullValue()));
        assertThat(cookie.toString(), is(notNullValue()));
    }
    
    @Test
    public void encodeDecode()
    {
        long expiresAt = System.currentTimeMillis() + 3600000;
        //
        CryptoCookie cookie = new CryptoCookie(expiresAt, 0, TOKEN, RANDOM.nextInt() & 0x1F);
        //
        assertThat(cookie.getToken(), is(notNullValue()));
        assertThat(cookie.getSignatue(), is(nullValue()));
        assertThat(cookie.getFlags(), is(equalTo(0L)));
        assertThat(cookie.getExpiryTime(), is(equalTo(expiresAt)));
        assertThat(cookie.getRebaked(), is(equalTo(0)));
        assertThat(cookie.getToken(), is(equalTo(TOKEN)));
        assertThat(cookie.toBytes(), is(notNullValue()));
        assertThat(cookie.toString(), is(notNullValue()));
        //
        cookie.sign(KEY);
        //
        assertThat(cookie.getSignatue(), is(notNullValue()));
        assertThat(cookie.toBytes(), is(notNullValue()));
        assertThat(cookie.toString(), is(notNullValue()));
        //
        String encoded = cookie.toString();
        //
        assertThat(encoded, is(notNullValue()));
        //
        try
        {
            CryptoCookie decoded = CryptoCookie.fromString(encoded);
            //
            assertThat(decoded.getToken(), is(notNullValue()));
            assertThat(decoded.getSignatue(), is(notNullValue()));
            assertThat(decoded.getFlags(), is(equalTo(0L)));
            assertThat(decoded.getExpiryTime(), is(equalTo(expiresAt)));
            assertThat(cookie.getRebaked(), is(equalTo(0)));
            assertThat(decoded.getToken(), is(equalTo(TOKEN)));
            assertThat(decoded.toBytes(), is(notNullValue()));
            assertThat(decoded.toString(), is(notNullValue()));    
        }
        catch (IOException e)
        {
            fail("CryptoCookie could not be decoded: " + e.getMessage());
        }
    }
    
    @Test
    public void encodeDecodeUnsinged()
    {
        long expiresAt = System.currentTimeMillis() + 3600000;
        //
        CryptoCookie cookie = new CryptoCookie(expiresAt, 0, TOKEN, RANDOM.nextInt() & 0x1F);
        //
        assertThat(cookie.getToken(), is(notNullValue()));
        assertThat(cookie.getSignatue(), is(nullValue()));
        assertThat(cookie.getFlags(), is(equalTo(0L)));
        assertThat(cookie.getExpiryTime(), is(equalTo(expiresAt)));
        assertThat(cookie.getRebaked(), is(equalTo(0)));
        assertThat(cookie.getToken(), is(equalTo(TOKEN)));
        assertThat(cookie.toBytes(), is(notNullValue()));
        assertThat(cookie.toString(), is(notNullValue()));
        //
        String encoded = cookie.toString();
        //
        assertThat(encoded, is(notNullValue()));
        //
        try
        {
            CryptoCookie.fromString(encoded);
            fail("Decoded unsigned CryptoCookie");    
        }
        catch (IOException e)
        {
            assertTrue(true);
        }
    }
    
    @Test
    public void verifyCookie()
    {
        // the cookie
        long expiresAt = System.currentTimeMillis() + 3600000;
        CryptoCookie cookie = new CryptoCookie(expiresAt, 0, TOKEN, RANDOM.nextInt() & 0x1F);
        cookie.sign(KEY);
        // test verify
        assertThat(cookie.verifySignature(KEY), is(equalTo(true)));
        assertThat(cookie.verify(KEY), is(equalTo(true)));
        // test does not varify with random key
        assertThat(cookie.verifySignature(SecretKey.generate()), is(equalTo(false)));
        assertThat(cookie.verify(SecretKey.generate()), is(equalTo(false)));
    }
}
