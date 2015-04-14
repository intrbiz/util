package com.intrbiz.crypto.tests;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.intrbiz.util.Hash;

public class TestHash
{
    @Test
    public void testToHex()
    {
        assertThat(Hash.toHex(new byte[] { -1 }), is(equalTo("ff")));
        assertThat(Hash.toHex(new byte[] { -1, 0, -1}), is(equalTo("ff00ff")));
        assertThat(Hash.toHex(new byte[] { (byte) 175, 8, 127}), is(equalTo("af087f")));
        assertThat(Hash.toHex(new byte[] { 1, 2, 3}), is(equalTo("010203")));
        assertThat(Hash.toHex(new byte[] { (byte) 245, (byte) 239, (byte) 208}), is(equalTo("f5efd0")));
    }
    
    @Test
    public void testSHA256()
    {
        byte[] hash = Hash.sha256(Hash.asUTF8("blah blah"));
        assertThat(hash, is(notNullValue()));
        assertThat(Hash.toHex(hash), is(equalTo("b975637caf9be61ad6ad27c1cbb1f5ad82fcdea92c18389137877b4c44a426e4")));
    }
    
    @Test
    public void testSHA1()
    {
        byte[] hash = Hash.sha1(Hash.asUTF8("blah blah"));
        assertThat(hash, is(notNullValue()));
        assertThat(Hash.toHex(hash), is(equalTo("135a1e01bae742c4a576b20fd41a683f6483ca43")));
    }
    
    @Test
    public void testMD5()
    {
        byte[] hash = Hash.md5(Hash.asUTF8("blah blah"));
        assertThat(hash, is(notNullValue()));
        assertThat(Hash.toHex(hash), is(equalTo("ae661d08d1ca1576a6efcb82b7bc502f")));
    }
    
    @Test
    public void testSHA512()
    {
        byte[] hash = Hash.sha512(Hash.asUTF8("blah blah"));
        assertThat(hash, is(notNullValue()));
        assertThat(Hash.toHex(hash), is(equalTo("0196b566757d3a0dcdbb716b024cfeb256be22715af93942b392a126a3ce93b2c6c60dd45ee39540b5494dbfdce58678c41a69ba9ff155ce691ce093755bfe46")));
    }
}
