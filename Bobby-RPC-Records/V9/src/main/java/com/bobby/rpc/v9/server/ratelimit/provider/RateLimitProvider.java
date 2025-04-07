package com.bobby.rpc.v9.server.ratelimit.provider;


import com.bobby.rpc.v9.server.ratelimit.IRateLimit;
import com.bobby.rpc.v9.server.ratelimit.impl.TokenBucketRateLimitImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * 针对每个服务，都可以设定限流器
 * 限流器一般设置在服务提供者
 */
public class RateLimitProvider {
    private final Map<String, IRateLimit> rateLimitMap;
    public RateLimitProvider() {
        rateLimitMap = new HashMap<>();
    }

    public IRateLimit getRateLimit(String interfaceName) {
        if( !rateLimitMap.containsKey(interfaceName)){
            rateLimitMap.put(interfaceName, new TokenBucketRateLimitImpl(100, 10));
        }
        return rateLimitMap.get(interfaceName);
    }
}
