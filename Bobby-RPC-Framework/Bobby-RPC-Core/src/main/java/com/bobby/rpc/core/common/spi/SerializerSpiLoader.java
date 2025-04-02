package com.bobby.rpc.core.common.spi;


import cn.hutool.core.io.resource.ResourceUtil;
import com.bobby.rpc.core.common.codec.ISerializer;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SerializerSpiLoader {

    // 存储已加载的 SPI 实现类的映射
    private static final Map<String, Class<? extends ISerializer>> loadedClassMap = new ConcurrentHashMap<>();
    private static final Map<String, Integer> serializerCodeMap = new ConcurrentHashMap<>();

    // SPI 配置文件的路径
    private static final String SPI_CONFIG_DIR = "META-INF/serializer/";

    public static void loadSpi(Class<?> serviceInterface) {
        String interfaceName = serviceInterface.getName();

        /**
         * 一个配置类样例
         * json=com.bobby.rpc.core.common.codec.JsonSerializer#2
         *
         * 这里多加了一个标识：序列化标识
         */

        // 读取配置文件，获取所有实现类
        List<URL> resources = ResourceUtil.getResources(SPI_CONFIG_DIR + interfaceName);
        for (URL resource : resources) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty() && !line.startsWith("#")) {
                        String[] parts = line.split("=");
                        if (parts.length == 2) {
                            String key = parts[0].trim();
                            String[] classParts = parts[1].trim().split("#");
                            String className = classParts[0].trim();

                            int serializerCode = Integer.parseInt(classParts[1].trim());

                            Class<?> implClass = Class.forName(className);
                            if (serviceInterface.isAssignableFrom(implClass)) {
                                loadedClassMap.put(key, (Class<? extends ISerializer>) implClass);
                                serializerCodeMap.put(key, serializerCode);
//                                 必须有无参构造
//                                 把该标识，注册进 ISerializer, 并实例化
//                                ISerializer.registerSerializer(serializerCode, (ISerializer) implClass.getDeclaredConstructor().newInstance());
                            }
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                log.error("Failed to load SPI resource: " + resource, e);
            }catch (NumberFormatException e){
                log.error("序列化标识有误! {}", e.getMessage());
                throw e;
            }
        }

    }


    public static ISerializer getInstance(String key) {
        Integer code = serializerCodeMap.get(key);
        if (code == null) {
            throw new RuntimeException("No serializer found for key: " + key);
        }
        if(!ISerializer.containsSerializer(code)){
            // 延迟实例化
            Class<? extends ISerializer> implClass = loadedClassMap.get(key);
            try {
                ISerializer.registerSerializer(code, (ISerializer) implClass.getDeclaredConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                log.error("Failed to load serializer for key: " + key, e);
                throw new RuntimeException(e);
            }
        }
        ISerializer serializer = ISerializer.getSerializerByCode(code);
        return serializer;
    }


}
