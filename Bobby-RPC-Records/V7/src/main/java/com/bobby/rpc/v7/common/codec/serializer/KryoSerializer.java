package com.bobby.rpc.v7.common.codec.serializer;


import com.bobby.rpc.v7.common.RpcRequest;
import com.bobby.rpc.v7.common.RpcResponse;
import com.bobby.rpc.v7.common.enums.MessageType;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Slf4j
public class KryoSerializer implements ISerializer {
    private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(RpcRequest.class); // 显式注册类（提高性能）
        kryo.register(RpcResponse.class);
        kryo.register(Object[].class);
        kryo.register(Class[].class);
        kryo.register(Class.class);
        kryo.setReferences(true); // 支持循环引用
        return kryo;
    });

//    private Kryo kryo;
//    public KryoSerializer() {
//        kryo = new Kryo();
//        kryo.setReferences(false);
//        kryo.setRegistrationRequired(false);
//    }


    @Override
    public byte[] serialize(Object obj){
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
             Output output = new Output(byteArrayOutputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            // 动态注册 RpcRequest 的参数类型

            if (obj instanceof RpcRequest) {
                Class<?>[] paramTypes = ((RpcRequest) obj).getParamsTypes();
                for (Class<?> type : paramTypes) {
                    kryo.register(type);
                }
            }else if (obj instanceof RpcResponse) {
                Class<?> dataType = ((RpcResponse) obj).getDataType();
                kryo.register(dataType);
            }else{
                kryo.register(obj.getClass());
            }

            kryo.writeObject(output, obj);
            output.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Kryo serialization failed", e);
        }
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("Cannot deserialize null or empty byte array");
        }
        if(MessageType.REQUEST.getCode()==messageType){
            return handleRequest(bytes);
        }else if(MessageType.RESPONSE.getCode()==messageType){
            return handleResponse(bytes);
        }else{
            log.error("暂不支持此种类型的消息: {}", messageType);
            throw new RuntimeException("暂不支持此种类型的消息");
        }
    }

    private Object handleResponse(byte[] bytes) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        Input input = new Input(byteArrayInputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            return kryo.readObject(input, RpcResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Kryo deserialization failed", e);
        }
    }

    private Object handleRequest(byte[] bytes) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             Input input = new Input(byteArrayInputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            return kryo.readObject(input, RpcRequest.class);
        } catch (Exception e) {
            throw new RuntimeException("Kryo deserialization failed", e);
        }
    }

    @Override
    public int getType() {
        return SerializerType.KRYO.getCode();
    }
}
