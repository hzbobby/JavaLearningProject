package com.bobby.rpc.v5.client.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class ServiceCache {
    private final Map<String, List<String>> serviceCache = new HashMap<>();

    /**
     * 获取服务列表
     * @param serviceName 服务名称
     * @return 返回服务列表
     */
    public List<String> getServiceList(String serviceName) {
        return serviceCache.get(serviceName);
    }

    /**
     * 添加服务地址
     * @param serviceName
     * @param address
     */
    public void addServiceAddress(String serviceName, String address){
        serviceCache.putIfAbsent(serviceName, new ArrayList<String>());
        List<String> addressList = serviceCache.get(serviceName);
        addressList.add(address);
        log.debug("添加服务: {}, 地址: {}", serviceName, address);
    }


    /**
     * 添加服务地址列表
     * @param serviceName
     * @param addressList
     */
    public void addServiceList(String serviceName, List<String> addressList){
        serviceCache.putIfAbsent(serviceName, new ArrayList<String>());
        serviceCache.get(serviceName).addAll(addressList);
        log.debug("添加服务: {}, 地址列表: {}", serviceName,Arrays.toString(addressList.toArray()));
    }

    /**
     * 修改服务地址
     * @param serviceName 服务名称
     * @param oldAddress 旧服务地址
     * @param newAddress 新服务地址
     */
    public void replaceServiceAddress(String serviceName, String oldAddress, String newAddress) {
        if(serviceCache.containsKey(serviceName)) {
            List<String> serviceStrings = serviceCache.get(serviceName);
            serviceStrings.remove(oldAddress);
            serviceStrings.add(newAddress);
            log.debug("替换服务: {}, 旧地址: {}, 新地址: {}", serviceName, oldAddress, newAddress);
        }else{
            log.debug("服务名称: {} 服务不存在", serviceName);
        }
    }

    /**
     * 删除服务地址
     * @param serviceName
     * @param address
     */
    public void deleteServiceAddress(String serviceName,String address){
        List<String> addressList = serviceCache.get(serviceName);
        addressList.remove(address);
        log.debug("删除服务: {}, 地址: {} ", serviceName, address);
    }

}
