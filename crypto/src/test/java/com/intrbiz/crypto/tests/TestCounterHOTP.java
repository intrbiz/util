package com.intrbiz.crypto.tests;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import org.junit.Test;

import com.intrbiz.util.CounterHOTP;
import com.intrbiz.util.CounterHOTP.CounterHOTPState;
import com.intrbiz.util.HOTP.HOTPSecret;
import com.intrbiz.util.HOTP.VerificationResult;

public class TestCounterHOTP
{
    @Test
    public void testNewOTPSecret()
    {
        CounterHOTP otp = new CounterHOTP();
        HOTPSecret secret = otp.newOTPSecret();
        assertThat(secret, is(notNullValue()));
        assertThat(secret.getSecret(), is(notNullValue()));
        assertThat(secret.toString(), is(notNullValue()));
        assertThat(secret.getSecret().length, is(equalTo(10)));
    }
    
    @Test
    public void testVerifyOTP19()
    {
        CounterHOTP otp = new CounterHOTP();
        HOTPSecret secret = HOTPSecret.fromString("PVFCXA3LC6K37RKI");
        // verify
        VerificationResult<CounterHOTPState> result = otp.verifyOTP(secret, new CounterHOTPState(0L), 80925);
        assertThat(result, is(notNullValue()));
        assertThat(result.isValid(), is(equalTo(true)));
        assertThat(result.getNextState(), is(notNullValue()));
        assertThat(result.getNextState().getCounter(), is(equalTo(19L)));
    }
    
    @Test
    public void testVerifyOTP20()
    {
        CounterHOTP otp = new CounterHOTP();
        HOTPSecret secret = HOTPSecret.fromString("PVFCXA3LC6K37RKI");
        // verify
        VerificationResult<CounterHOTPState> result = otp.verifyOTP(secret, new CounterHOTPState(0L), 822394);
        assertThat(result, is(notNullValue()));
        assertThat(result.isValid(), is(equalTo(true)));
        assertThat(result.getNextState(), is(notNullValue()));
        assertThat(result.getNextState().getCounter(), is(equalTo(20L)));
    }
    
    @Test
    public void testVerifyOTP21()
    {
        CounterHOTP otp = new CounterHOTP();
        HOTPSecret secret = HOTPSecret.fromString("PVFCXA3LC6K37RKI");
        // verify
        VerificationResult<CounterHOTPState> result = otp.verifyOTP(secret, new CounterHOTPState(0L), 565888);
        assertThat(result, is(notNullValue()));
        assertThat(result.isValid(), is(equalTo(true)));
        assertThat(result.getNextState(), is(notNullValue()));
        assertThat(result.getNextState().getCounter(), is(equalTo(21L)));
    }
    
    @Test
    public void testVerifyOTPReplay()
    {
        CounterHOTP otp = new CounterHOTP();
        HOTPSecret secret = HOTPSecret.fromString("PVFCXA3LC6K37RKI");
        // verify
        VerificationResult<CounterHOTPState> result = otp.verifyOTP(secret, new CounterHOTPState(50L), 565888);
        assertThat(result, is(notNullValue()));
        assertThat(result.isValid(), is(equalTo(false)));
        assertThat(result.getNextState(), is(nullValue()));
    }
    
    @Test
    public void testVerifyOTPOutOfSync()
    {
        CounterHOTP otp = new CounterHOTP(5);
        HOTPSecret secret = HOTPSecret.fromString("PVFCXA3LC6K37RKI");
        // verify
        VerificationResult<CounterHOTPState> result = otp.verifyOTP(secret, new CounterHOTPState(0L), 565888);
        assertThat(result, is(notNullValue()));
        assertThat(result.isValid(), is(equalTo(false)));
        assertThat(result.getNextState(), is(nullValue()));
    }
    
    @Test
    public void testVerifyOTPBadCode()
    {
        CounterHOTP otp = new CounterHOTP();
        HOTPSecret secret = HOTPSecret.fromString("PVFCXA3LC6K37RKI");
        // verify
        VerificationResult<CounterHOTPState> result = otp.verifyOTP(secret, new CounterHOTPState(0L), 123456);
        assertThat(result, is(notNullValue()));
        assertThat(result.isValid(), is(equalTo(false)));
        assertThat(result.getNextState(), is(nullValue()));
    }
}
