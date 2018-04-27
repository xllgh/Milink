package com.bandlink.air.jpush;

public interface IRateLimiting {

    public int getRateLimitQuota();
    
    public int getRateLimitRemaining();
    
    public int getRateLimitReset();
    
}

