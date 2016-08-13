package com.intrbiz.util;

import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base32;

import com.intrbiz.util.HOTP.HOTPState;

/**
 * HMAC based 
 */
public abstract class HOTP<T extends HOTPState>
{
    protected long lookaheadLimit = 50;
    
    public HOTP()
    {
        super();
    }
    
    public HOTP(long lookaheadLimit)
    {
        super();
        this.lookaheadLimit = lookaheadLimit;
    }

    /**
     * How many iterations of OTP can we look forward and consider it a valid token
     */
    public long getLookaheadLimit()
    {
        return this.lookaheadLimit;
    }
    
    /**
     * Generate a new OTP secret
     */
    public HOTPSecret newOTPSecret()
    {
        byte[] secret = new byte[10];
        (new SecureRandom()).nextBytes(secret);
        return new HOTPSecret(secret);
    }
    
    /**
     * Compute a OTP code
     */
    public abstract int computeOTP(HOTPSecret secret, T currentState);
    
    /**
     * Verify a HOTP code
     */
    public abstract VerificationResult<T> verifyOTP(HOTPSecret secret, T currentState, int otp);
    
    
    /**
     * The state of a HOTP device
     */
    public static class HOTPState
    {
    }
    
    /**
     * A verification result with associated state updates
     */
    public static class VerificationResult<T extends HOTPState>
    {
        private final boolean valid;
        
        private final T nextState;
        
        public VerificationResult(boolean valid, T nextState)
        {
            this.valid = valid;
            this.nextState = nextState;
        }

        public boolean isValid()
        {
            return valid;
        }

        public T getNextState()
        {
            return nextState;
        }
    }
    
    public static class HOTPSecret
    {
        private final byte[] secret;
        
        public HOTPSecret(byte[] secret)
        {
            this.secret = secret;
        }
        
        public byte[] getSecret()
        {
            return this.secret;
        }
        
        public String toString()
        {
            return (new Base32()).encodeToString(this.secret);
        }
        
        public static final HOTPSecret fromString(String secret)
        {
            return new HOTPSecret((new Base32()).decode(secret));
        }
    }
}
