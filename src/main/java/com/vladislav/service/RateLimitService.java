package com.vladislav.service;

public interface RateLimitService {

    boolean isRateLimited(String ip);
}
