package com.bobby.rpc.core.common.codec;

import com.bobby.rpc.core.common.RpcRequest;
import com.bobby.rpc.core.common.enums.MessageType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class KyroSerializerTest {

    private final KryoSerializer serializer = new KryoSerializer();

    @Test
    void testRpcRequestSerialization() {
        RpcRequest request = RpcRequest.builder()
                .interfaceName("com.bobby.rpc.core.sample.IDemoService")
                .methodName("sayHello")
                .params(new Object[]{"Alice"})
                .paramsTypes(new Class[]{String.class})
                .build();

        byte[] bytes = serializer.serialize(request);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        RpcRequest deserialized = (RpcRequest) serializer.deserialize(bytes, MessageType.REQUEST.getCode());
        assertEquals(request.getInterfaceName(), deserialized.getInterfaceName());
        assertArrayEquals(request.getParams(), deserialized.getParams());
    }
}
