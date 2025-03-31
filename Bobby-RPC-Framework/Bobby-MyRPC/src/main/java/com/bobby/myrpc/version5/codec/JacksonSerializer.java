package com.bobby.myrpc.version5.codec;


import com.bobby.myrpc.version5.MessageType;
import com.bobby.myrpc.version5.RPCRequest;
import com.bobby.myrpc.version5.RPCResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

import java.io.IOException;

/**
 * @author: Bobby
 * @email: vividbobby@163.com
 * @date: 2025/3/28
 */
public class JacksonSerializer implements ISerializer {
    private ObjectMapper objectMapper;

    public JacksonSerializer() {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.bobby.myrpc.version4")
                .build();

        this.objectMapper = new ObjectMapper();
        // Enable polymorphic type handling
        this.objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
    }

    @Override
    public byte[] serialize(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsBytes(obj);
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        try {
            // 传输的消息分为request与response
            if (MessageType.REQUEST.getCode() == messageType) {
                RPCRequest request = objectMapper.readValue(bytes, RPCRequest.class);
                Object[] objects = new Object[request.getParams().length];
                // Convert JSON strings to corresponding objects
                for (int i = 0; i < objects.length; i++) {
                    Class<?> paramsType = request.getParamsTypes()[i];
                    if (!paramsType.isAssignableFrom(request.getParams()[i].getClass())) {
                        objects[i] = objectMapper.convertValue(request.getParams()[i], paramsType);
                    } else {
                        objects[i] = request.getParams()[i];
                    }
                }
                request.setParams(objects);
                return request;
            } else if (MessageType.RESPONSE.getCode() == messageType) {
                RPCResponse response = objectMapper.readValue(bytes, RPCResponse.class);
                Class<?> dataType = response.getDataType();
                if (!dataType.isAssignableFrom(response.getData().getClass())) {
                    response.setData(objectMapper.convertValue(response.getData(), dataType));
                }
                return response;
            } else {
                System.out.println("暂时不支持此种消息");
                throw new RuntimeException();
            }
        } catch (IOException e) {
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    @Override
    public int getType() {
        return 3;
    }
}
