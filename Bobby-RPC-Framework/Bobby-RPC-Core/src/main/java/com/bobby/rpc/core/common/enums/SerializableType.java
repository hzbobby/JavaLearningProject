package com.bobby.rpc.core.common.enums;

import lombok.Getter;

@Getter
public enum SerializableType {
    JDK(0),
    JSON(1),
    KRYO(2)
    ;

    private final int code;

    SerializableType(int code) {
        this.code = code;
    }
}
