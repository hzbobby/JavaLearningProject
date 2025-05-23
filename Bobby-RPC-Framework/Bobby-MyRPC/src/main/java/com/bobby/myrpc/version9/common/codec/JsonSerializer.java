package com.bobby.myrpc.version9.common.codec;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.bobby.rpc.core.common.RpcRequest;
import com.bobby.rpc.core.common.RpcResponse;
import com.bobby.rpc.core.common.enums.MessageType;
import com.bobby.rpc.core.common.enums.SerializableType;

/**
 * 由于json序列化的方式是通过把对象转化成字符串，丢失了Data对象的类信息，所以deserialize需要
 * 了解对象对象的类信息，根据类信息把JSON -> 对应的对象
 */
public class JsonSerializer implements ISerializer {
    @Override
    public byte[] serialize(Object obj) {
        return JSON.toJSONBytes(obj);
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        Object obj = null;
        // 传输的消息分为request与response
        if (MessageType.REQUEST.getCode() == messageType) {
            RpcRequest request = parseObject(bytes, RpcRequest.class);
            Object[] objects = new Object[request.getParams().length];
            // Convert JSON strings to corresponding objects
            for (int i = 0; i < objects.length; i++) {
                Class<?> paramsType = request.getParamsTypes()[i];
                if (!paramsType.isAssignableFrom(request.getParams()[i].getClass())) {
                    objects[i] = JSON.parseObject(JSON.toJSONString(request.getParams()[i]), request.getParamsTypes()[i]);
                } else {
                    objects[i] = request.getParams()[i];
                }
            }
            request.setParams(objects);
            obj = request;
        } else if (MessageType.RESPONSE.getCode() == messageType) {
            RpcResponse response = parseObject(bytes, RpcResponse.class);
            Class<?> dataType = response.getDataType();
            if (!dataType.isAssignableFrom(response.getData().getClass())) {
                response.setData(JSON.parseObject(JSON.toJSONString(response.getData()), dataType));
            }
            obj = response;
        } else {
            System.out.println("暂时不支持此种消息");
            throw new RuntimeException();
        }
        return obj;
    }

    private <T> T parseObject(byte[] bytes, Class<T> clazz) {
        return JSON.parseObject(bytes, clazz, JSONReader.Feature.SupportClassForName);
    }

    // 1 代表着json序列化方式
    @Override
    public int getType() {
        return SerializableType.JSON.getCode();
    }
}