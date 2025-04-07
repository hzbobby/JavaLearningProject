package com.bobby.rpc.v4.common.codec.serializer;


import com.bobby.rpc.v4.common.RpcRequest;
import com.bobby.rpc.v4.common.RpcResponse;
import com.bobby.rpc.v4.common.enums.MessageType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author: Bobby
 * @email: vividbobby@163.com
 * @date: 2025/3/28
 */
@Slf4j
public class JacksonSerializer implements ISerializer {
    private ObjectMapper objectMapper;

    public JacksonSerializer() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public byte[] serialize(Object obj) {
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            log.error("Json 序列化发生错误: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("Cannot deserialize null or empty byte array");
        }
        // 传输的消息分为request与response
        if (MessageType.REQUEST.getCode() == messageType) {
            return handleRequest(bytes);
        } else if (MessageType.RESPONSE.getCode() == messageType) {
            return handleResponse(bytes);
        } else {
            System.out.println("暂时不支持此种消息");
            throw new RuntimeException("暂不支持此种类型的消息");
        }
    }

    private Object handleRequest(byte[] bytes) {
        // 序列化反序列化后，类型擦除了
        RpcRequest request = null;
        try {
            request = objectMapper.readValue(bytes, RpcRequest.class);
            // Convert JSON strings to corresponding objects
            for (int i = 0; i < request.getParamsTypes().length; i++) {
                Class<?> paramsType = request.getParamsTypes()[i];
                if (!paramsType.isAssignableFrom(request.getParams()[i].getClass())) {
                    byte[] tmpBytes = objectMapper.writeValueAsBytes(request.getParams()[i]);
                    request.getParams()[i] = objectMapper.readValue(tmpBytes, paramsType);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return request;
    }
    private Object handleResponse(byte[] bytes) {
        RpcResponse response = null;
        try {
            response = objectMapper.readValue(bytes, RpcResponse.class);
            Class<?> dataType = response.getDataType();
            // data 可能为空
            if (response.getData()!=null && !dataType.isAssignableFrom(response.getData().getClass())) {
                byte[] tmpBytes = objectMapper.writeValueAsBytes(response.getData());
                response.setData(objectMapper.readValue(tmpBytes, dataType));
//                response.setData(objectMapper.convertValue(response.getData(), dataType));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    @Override
    public int getType() {
        return SerializerType.JSON.getCode();
    }
}
