package com.bobby.rpc.v9.server.ratelimit.impl;


import com.bobby.rpc.v9.server.ratelimit.IRateLimit;

/**
 * 令牌桶限流器实现
 * 介绍：
 * 令牌桶算法是一种基于令牌的限流算法，它维护一个固定容量的令牌桶，按照固定速率往桶中添加令牌，
 * 每当有请求到来时，消耗一个令牌，如果桶中没有足够的令牌，则拒绝该请求。
 *
 * 主要是用来限制 单位时间内通过的请求数量
 *
 * 特点：
 * 1. 固定时间间隔会添加令牌。
 * 2. 桶满了，就不继续增加令牌
 * 3. 当令牌消费完后，就拒绝请求
 *
 * 原理：
 *
 *
 *
 * 参考：
 * https://www.cnblogs.com/DTinsight/p/18221858
 *
 *
 *
 */
public class TokenBucketRateLimitImpl implements IRateLimit {

    // 令牌产生速率 (ms)
    private static int RATE;
    // 桶容量
    private static  int CAPACITY;
    //当前桶容量
    private volatile int curCapcity;

    //时间戳
    private volatile long lastTimestamp;


    public TokenBucketRateLimitImpl(int rate,int capacity){
        RATE=rate;
        CAPACITY=capacity;
        curCapcity=capacity;
        lastTimestamp=System.currentTimeMillis();
    }


    @Override
    public boolean getToken() {
        // 如果当前桶中还有令牌，则可以访问
        if(curCapcity > 0){
            curCapcity--;
            return true;
        }

        // 桶中没有令牌，
        // 则添加令牌：按照时间差内能生成多少令牌
        // 当前时刻 - 上一时刻。在这段时间内能生成多少令牌
        long now = System.currentTimeMillis();
        long delta_timestamp = now - lastTimestamp;
        if(delta_timestamp > RATE){
            // 生成了至少一个令牌
            // 计算是不是有生成更多令牌
            if(delta_timestamp/RATE > 2){
                // 至少生成 2 个，才可以给桶中加上令牌
                // 因为这次请求要消耗一个
                curCapcity += (int)(delta_timestamp/RATE) - 1;
            }

            if(curCapcity > CAPACITY){
                curCapcity = CAPACITY; // 不能超过桶的容量
            }

            lastTimestamp = now;
            return true;
        }
        // 请求太快啦，令牌还没生成呢
        return false;
    }
}
