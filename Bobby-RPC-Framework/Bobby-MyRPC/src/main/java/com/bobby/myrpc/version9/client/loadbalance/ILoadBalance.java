package com.bobby.myrpc.version9.client.loadbalance;

import java.util.List;

/**
 * 给服务器地址列表，根据不同的负载均衡策略选择一个
 */
public interface ILoadBalance {
    String balance(List<String> addressList);
}