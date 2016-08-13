package com.intrbiz.util;

import com.intrbiz.util.CounterHOTP.CounterHOTPState;

public class CounterHOTP extends HOTP<CounterHOTPState>
{    
    public CounterHOTP()
    {
        super();
    }

    public CounterHOTP(long lookaheadLimit)
    {
        super(lookaheadLimit);
    }

    public int computeOTP(HOTPSecret secret, CounterHOTPState currentState)
    {
        return this.computeOTP(secret.getSecret(), currentState.getCounter());
    }
    
    private int computeOTP(byte[] key, long counter)
    {
        byte[] counterBytes = { 
            (byte) (counter >> 56), (byte) (counter >> 48), (byte) (counter >> 40), (byte) (counter >> 32),
            (byte) (counter >> 24), (byte) (counter >> 16), (byte) (counter >> 8), (byte) counter
        };
        // compute the hmac
        byte[] hmac = Hash.sha1HMAC(key, counterBytes);
        // truncate
        int offset = hmac[hmac.length - 1] & 0xF;
        int p = ((hmac[offset] & 0x7F) << 24) | ((hmac[offset + 1] & 0xFF) << 16) | ((hmac[offset + 2] & 0xFF) << 8) | (hmac[offset + 3] & 0xFF);
        return p % 1000000;
    }
    
    public VerificationResult<CounterHOTPState> verifyOTP(HOTPSecret secret, CounterHOTPState currentState, int otp)
    {
        long currentCounter = currentState == null ? 0L : currentState.getCounter();
        for (long counter = currentCounter; counter < (currentCounter + this.lookaheadLimit); counter++)
        {
            if (otp == this.computeOTP(secret.getSecret(), counter))
                return new VerificationResult<CounterHOTPState>(true, new CounterHOTPState(counter));
        }
        return new VerificationResult<CounterHOTPState>(false, null);
    }
    
    public static class CounterHOTPState extends HOTP.HOTPState
    {
        private final long counter;

        public CounterHOTPState(long counter)
        {
            super();
            this.counter = counter;
        }
        
        public long getCounter()
        {
            return this.counter;
        }
        
        public String toString()
        {
            return "counter=" + this.counter;
        }
    }
}
