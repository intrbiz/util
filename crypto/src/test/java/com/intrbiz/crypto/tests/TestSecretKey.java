package com.intrbiz.crypto.tests;

import org.junit.Test;

import com.intrbiz.crypto.SecretKey;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class TestSecretKey
{
    @Test
    public void defaultGenerateKey()
    {
        SecretKey key = SecretKey.generate();
        //
        assertThat(key, is(notNullValue()));
        assertThat(key.asBytes(), is(notNullValue()));
        assertThat(key.toBytes(), is(notNullValue()));
        assertThat(key.toString(), is(notNullValue()));
        assertThat(key.asBytes().length, is(equalTo(128)));
    }
    
    @Test
    public void generateKey()
    {
        SecretKey key = SecretKey.generate(1024);
        //
        assertThat(key, is(notNullValue()));
        assertThat(key.asBytes(), is(notNullValue()));
        assertThat(key.toBytes(), is(notNullValue()));
        assertThat(key.toString(), is(notNullValue()));
        assertThat(key.asBytes().length, is(equalTo(1024)));
    }
    
    @Test
    public void keyFromString()
    {
        String encodedKey = "FnaDAfV/Zg43bL+2fRCX7B5Y5PB7/9jF4adr/Z9dd58gc9vyKb8niU1ZmI+DOs8FP+Oij25s5ZFGG8F+fK7hequZeJw/MpwoW8YLyiofL7pSyfdXTeFDye5UUI9X+aN4sWUcj7BbE2TxY5eaIYr0XHljJVDw6XTFmG4Pq1aHi9U=";
        SecretKey key = SecretKey.fromString(encodedKey);
        //
        assertThat(key, is(notNullValue()));
        assertThat(key.asBytes(), is(notNullValue()));
        assertThat(key.toBytes(), is(notNullValue()));
        assertThat(key.toString(), is(notNullValue()));
        assertThat(key.asBytes().length, is(equalTo(128)));
        assertThat(key.toString(), is(equalTo(encodedKey)));
    }
    
    @Test
    public void encodeDecode()
    {
        SecretKey key = SecretKey.generate();
        //
        assertThat(key, is(notNullValue()));
        assertThat(key.asBytes(), is(notNullValue()));
        assertThat(key.toBytes(), is(notNullValue()));
        assertThat(key.toString(), is(notNullValue()));
        //
        String encoded = key.toString();
        //
        assertThat(encoded, is(notNullValue()));
        //
        SecretKey decoded = SecretKey.fromString(encoded);
        //
        assertThat(decoded, is(notNullValue()));
        assertThat(decoded.asBytes(), is(notNullValue()));
        assertThat(decoded.toBytes(), is(notNullValue()));
        assertThat(decoded.toString(), is(notNullValue()));
        //
        assertThat(key.asBytes(), is(equalTo(decoded.asBytes())));
        assertThat(key.toBytes(), is(equalTo(decoded.toBytes())));
        assertThat(key.toString(), is(equalTo(decoded.toString())));
    }
}
