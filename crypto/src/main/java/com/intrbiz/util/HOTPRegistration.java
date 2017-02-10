package com.intrbiz.util;

import com.intrbiz.util.HOTP.HOTPSecret;
import com.intrbiz.util.HOTP.HOTPState;

public interface HOTPRegistration
{
    /**
     * The secret for this HOTP registration
     */
    HOTPSecret getHOTPSecret();
    
    /**
     * The current HTOP state
     */
    <T extends HOTPState> T getHOTPState();
}
