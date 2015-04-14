package com.intrbiz.crypto.tests;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.intrbiz.crypto.SecretKey;
import com.intrbiz.crypto.cookie.CookieBaker;
import com.intrbiz.crypto.cookie.CookieBaker.Expires;
import com.intrbiz.crypto.cookie.CookieBaker.Rebake;
import com.intrbiz.crypto.cookie.CryptoCookie;


public class TestCookieBaker
{
    public static final byte[] TOKEN = { 1, 2, 3, 4, 5, 6, 7, 8 };
    
    public static final SecretKey KEY = SecretKey.fromString("FnaDAfV/Zg43bL+2fRCX7B5Y5PB7/9jF4adr/Z9dd58gc9vyKb8niU1ZmI+DOs8FP+Oij25s5ZFGG8F+fK7hequZeJw/MpwoW8YLyiofL7pSyfdXTeFDye5UUI9X+aN4sWUcj7BbE2TxY5eaIYr0XHljJVDw6XTFmG4Pq1aHi9U=");
    
    @Test
    public void testBake()
    {
        CookieBaker baker = new CookieBaker(KEY, 8);
        CryptoCookie cookie = baker.bake();
        assertThat(cookie, is(notNullValue()));
        assertThat(cookie.getToken(), is(notNullValue()));
        assertThat(cookie.getSignatue(), is(notNullValue()));
        assertThat(cookie.getToken().length, is(equalTo(8)));
        assertThat(cookie.getExpiryTime(), is(greaterThan(0L)));
        assertThat(cookie.getFlags(), is(equalTo(0L)));
        assertThat(cookie.getRebaked(), is(equalTo(0)));
        assertThat(cookie.toString(), is(notNullValue()));
    }
    
    @Test
    public void testBakeExpires()
    {
        long expires = Expires.after(10, TimeUnit.MINUTES);
        //
        CookieBaker baker = new CookieBaker(KEY, 8);
        CryptoCookie cookie = baker.bake(expires);
        assertThat(cookie, is(notNullValue()));
        assertThat(cookie.getToken(), is(notNullValue()));
        assertThat(cookie.getSignatue(), is(notNullValue()));
        assertThat(cookie.getToken().length, is(equalTo(8)));
        assertThat(cookie.getExpiryTime(), is(equalTo(expires)));
        assertThat(cookie.getFlags(), is(equalTo(0L)));
        assertThat(cookie.getRebaked(), is(equalTo(0)));
        assertThat(cookie.toString(), is(notNullValue()));
    }
    
    @Test
    public void testRebake()
    {
        CookieBaker baker = new CookieBaker(KEY, 8);
        CryptoCookie cookie = baker.bake();
        assertThat(cookie, is(notNullValue()));
        assertThat(cookie.getRebaked(), is(equalTo(0)));
        //
        CryptoCookie rebaked = baker.rebake(cookie);
        assertThat(rebaked, is(notNullValue()));
        assertThat(rebaked.getRebaked(), is(equalTo(1)));
        //
        CryptoCookie rebaked2 = baker.rebake(rebaked);
        assertThat(rebaked2, is(notNullValue()));
        assertThat(rebaked2.getRebaked(), is(equalTo(2)));
        //
        CryptoCookie rebaked3 = baker.rebake(rebaked2);
        assertThat(rebaked3, is(notNullValue()));
        assertThat(rebaked3.getRebaked(), is(equalTo(3)));
    }
    
    @Test
    public void testRebakeLimit()
    {
        CookieBaker baker = new CookieBaker(KEY, 8, Rebake.limit(1));
        CryptoCookie cookie = baker.bake();
        assertThat(cookie, is(notNullValue()));
        assertThat(cookie.getRebaked(), is(equalTo(0)));
        //
        CryptoCookie rebaked = baker.rebake(cookie);
        assertThat(rebaked, is(notNullValue()));
        assertThat(rebaked.getRebaked(), is(equalTo(1)));
        //
        CryptoCookie rebakedAgain = baker.rebake(rebaked);
        assertThat(rebakedAgain, is(nullValue()));
    }
    
    @Test
    public void testNeverRebake()
    {
        CookieBaker baker = new CookieBaker(KEY, 8, Rebake.never());
        CryptoCookie cookie = baker.bake();
        assertThat(cookie, is(notNullValue()));
        assertThat(cookie.getRebaked(), is(equalTo(0)));
        //
        CryptoCookie rebaked = baker.rebake(cookie);
        assertThat(rebaked, is(nullValue()));
    }
}
