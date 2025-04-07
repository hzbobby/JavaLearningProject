package com.bobby.rpc.v9.common.codec.serializer;


import java.util.HashMap;
import java.util.Map;

/**
 * @author: Bobby
 * @email: vividbobby@163.com
 * @date: 2025/3/28
 */

public interface ISerializer {
    // 把对象序列化成字节数组
    byte[] serialize(Object obj);

    // 从字节数组反序列化成消息, 使用java自带序列化方式不用messageType也能得到相应的对象（序列化字节数组里包含类信息）
    // 其它方式需指定消息格式，再根据message转化成相应的对象
    Object deserialize(byte[] bytes, int messageType);

    // 返回使用的序列器，是哪个
    // 0：java自带序列化方式, 1: json序列化方式
    int getType();


    // 定义静态常量 serializerMap
    // 这个主要用于获取序列化器的实例
    static final Map<Integer, ISerializer> serializerMap = new HashMap<>();

    // 根据序号取出序列化器，暂时有两种实现方式，需要其它方式，实现这个接口即可
    static ISerializer getSerializerByCode(int code) {
        ISerializer iSerializer = serializerMap.get(code);
        if (iSerializer == null) {
            // 尝试创建
            if(code == SerializerType.JDK.getCode()) {
                serializerMap.put(code, new ObjectSerializer());
            }else if(code == SerializerType.JSON.getCode()) {
                serializerMap.put(code, new JacksonSerializer());
            }
            iSerializer = serializerMap.get(code);
            if (iSerializer == null) {
                throw new RuntimeException("No serializer registered for code " + code);
            }
        }
        return iSerializer;
    }

    static void registerSerializer(int code, ISerializer serializer) {
        registerSerializer(code, serializer, false);
    }

    static void registerSerializer(int code, ISerializer serializer, boolean replace) {
        if (replace) {
            serializerMap.put(code, serializer);
        }else{
            serializerMap.putIfAbsent(code, serializer);
        }
    }

    static boolean containsSerializer(int code) {
        return serializerMap.containsKey(code);
    }

    public static enum SerializerType {
        JDK(0),
        JSON(1),
        KRYO(2)
        ;

        private final int code;
        SerializerType(int code) {
            this.code = code;
        }
        public int getCode() {
            return code;
        }
    }

}
