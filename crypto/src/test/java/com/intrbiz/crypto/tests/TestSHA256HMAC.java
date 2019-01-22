package com.intrbiz.crypto.tests;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.intrbiz.util.Hash;

public class TestSHA256HMAC
{
    @Test
    public void RFC4231TestCase1()
    {
        byte[] key = Hash.fromHex("0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b");
        byte[] data = Hash.fromHex("4869205468657265");
        byte[] hmac = Hash.sha256HMAC(key, data);
        assertThat(hmac, is(notNullValue()));
        assertThat(Hash.toHex(hmac), is(equalTo("b0344c61d8db38535ca8afceaf0bf12b881dc200c9833da726e9376c2e32cff7")));
    }
    
    @Test
    public void RFC4231TestCase2()
    {
        byte[] key = { 0x4a, 0x65, 0x66, 0x65 };
        byte[] data = { 0x77, 0x68, 0x61, 0x74, 0x20, 0x64, 0x6f, 0x20, 0x79, 0x61, 0x20, 0x77, 0x61, 0x6e, 0x74, 0x20, 0x66, 0x6f, 0x72, 0x20, 0x6e, 0x6f, 0x74, 0x68, 0x69, 0x6e, 0x67, 0x3f  };
        byte[] hmac = Hash.sha256HMAC(key, data);
        assertThat(hmac, is(notNullValue()));
        assertThat(Hash.toHex(hmac), is(equalTo("5bdcc146bf60754e6a042426089575c75a003f089d2739839dec58b964ec3843")));
    }
    
    @Test
    public void RFC4231TestCase3()
    {
        byte[] key = Hash.fromHex("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        byte[] data = Hash.fromHex("dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd");
        byte[] hmac = Hash.sha256HMAC(key, data);
        assertThat(hmac, is(notNullValue()));
        assertThat(Hash.toHex(hmac), is(equalTo("773ea91e36800e46854db8ebd09181a72959098b3ef8c122d9635514ced565fe")));
    }
    
    @Test
    public void RFC4231TestCase6()
    {
        byte[] key = Hash.fromHex("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        byte[] data = Hash.fromHex("54657374205573696e67204c6172676572205468616e20426c6f636b2d53697a65204b6579202d2048617368204b6579204669727374");
        byte[] hmac = Hash.sha256HMAC(key, data);
        assertThat(hmac, is(notNullValue()));
        assertThat(Hash.toHex(hmac), is(equalTo("60e431591ee0b67f0d8a26aacbf5b77f8e0bc6213728c5140546040f0ee37f54")));
    }
    
    @Test
    public void testCase()
    {
        byte[] key = Hash.fromHex("3b5fa880d92028847117a85965242f5e");
        byte[] data = Hash.fromHex("188036d68a10c62c7d38f90a246748c5b33f005b79f6e3b588d08037636a6f8c");
        byte[] hmac = Hash.sha256HMAC(key, data);
        assertThat(hmac, is(notNullValue()));
        assertThat(Hash.toHex(hmac), is(equalTo("bf03b923c9273b4db26c06b0da247a9940eb336ccfacba327e5802c76686420a")));
    }
    
    @Test
    public void testCaseSplit()
    {
        byte[] key   = Hash.fromHex("3b5fa880d92028847117a85965242f5e");
        byte[] data1 = Hash.fromHex("188036d68a10c62c7d38f90a246748c5");
        byte[] data2 = Hash.fromHex("b33f005b79f6e3b588d08037636a6f8c");
        byte[] hmac = Hash.sha256HMAC(key, data1, data2);
        assertThat(hmac, is(notNullValue()));
        assertThat(Hash.toHex(hmac), is(equalTo("bf03b923c9273b4db26c06b0da247a9940eb336ccfacba327e5802c76686420a")));
    }
}
