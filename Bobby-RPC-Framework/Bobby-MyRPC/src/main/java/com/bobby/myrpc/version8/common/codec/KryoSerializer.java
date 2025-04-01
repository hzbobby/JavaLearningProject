package com.bobby.myrpc.version8.common.codec;

import com.fasterxml.jackson.core.JsonProcessingException;

public class KryoSerializer implements ISerializer {
    @Override
    public byte[] serialize(Object obj) throws JsonProcessingException {
        return new byte[0];
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        return null;
    }

    @Override
    public int getType() {
        return 0;
    }
}
