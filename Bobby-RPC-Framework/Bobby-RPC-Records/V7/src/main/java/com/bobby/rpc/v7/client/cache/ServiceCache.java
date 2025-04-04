package com.bobby.rpc.v7.client.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class ServiceCache {
    private final Map<String, Set<String>> serviceCache = new HashMap<>();

    /**
     * 获取服务列表
     * @param serviceName 服务名称
     * @return 返回服务列表
     */
    public List<String> getServiceList(String serviceName) {
        if (serviceCache.containsKey(serviceName)) {
            return new ArrayList<>(serviceCache.get(serviceName));
        }
        return null;
    }

    /**
     * 缓存服务地址
     * @param serviceName
     * @param address
     */
    public void addServiceAddress(String serviceName, String address){
        serviceCache.putIfAbsent(serviceName, new HashSet<>());
        serviceCache.get(serviceName).add(address);
        log.info("缓存服务: {}, 地址: {}", serviceName, address);
    }


    /**
     * 缓存服务地址列表
     * @param serviceName
     * @param addressList
     */
    public void addServiceList(String serviceName, List<String> addressList){
        // 如果存在就不缓存
        serviceCache.putIfAbsent(serviceName, new HashSet<>());
        serviceCache.get(serviceName).addAll(addressList);
        log.info("缓存服务: {}, 地址列表: {}", serviceName,Arrays.toString(addressList.toArray()));
    }

    /**
     * 修改服务地址
     * @param serviceName 服务名称
     * @param oldAddress 旧服务地址
     * @param newAddress 新服务地址
     */
    public void replaceServiceAddress(String serviceName, String oldAddress, String newAddress) {
        if(serviceCache.containsKey(serviceName)) {
            Set<String> serviceStrings = serviceCache.get(serviceName);
            serviceStrings.remove(oldAddress);
            serviceStrings.add(newAddress);
            log.info("替换服务: {}, 旧地址: {}, 新地址: {}", serviceName, oldAddress, newAddress);
        }else{
            log.info("服务名称: {} 服务不存在", serviceName);
        }
    }

    /**
     * 删除服务地址
     * @param serviceName
     * @param address
     */
    public void deleteServiceAddress(String serviceName,String address){
        if(!serviceCache.containsKey(serviceName)) {
            log.info("服务不存在: {}", serviceName);
            return;
        }
        serviceCache.get(serviceName).remove(address);
        log.info("删除服务: {}, 地址: {} ", serviceName, address);
    }

}
