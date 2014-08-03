package com.intrbiz.crypto.tests;

import java.io.IOException;

import org.junit.Test;

import com.intrbiz.crypto.SecretKey;
import com.intrbiz.crypto.cookie.CryptoCookie;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import static org.junit.Assert.*;

public class CryptoCookieTests
{
    public static final byte[] TOKEN = { 1, 2, 3, 4, 5, 6, 7, 8 };
    
    public static final SecretKey KEY = SecretKey.fromString("FnaDAfV/Zg43bL+2fRCX7B5Y5PB7/9jF4adr/Z9dd58gc9vyKb8niU1ZmI+DOs8FP+Oij25s5ZFGG8F+fK7hequZeJw/MpwoW8YLyiofL7pSyfdXTeFDye5UUI9X+aN4sWUcj7BbE2TxY5eaIYr0XHljJVDw6XTFmG4Pq1aHi9U=");
    
    @Test
    public void createCookie()
    {
        long expiresAt = System.currentTimeMillis() + 3600000;
        //
        CryptoCookie cookie = new CryptoCookie(expiresAt, 0, TOKEN);
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
        CryptoCookie cookie = new CryptoCookie(expiresAt, 0, TOKEN);
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
        CryptoCookie cookie = new CryptoCookie(expiresAt, 0, TOKEN);
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
        CryptoCookie cookie = new CryptoCookie(expiresAt, 0, TOKEN);
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
}
