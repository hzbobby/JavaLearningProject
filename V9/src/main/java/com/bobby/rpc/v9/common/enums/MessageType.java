package com.bobby.rpc.v9.common.enums;

public enum MessageType {
    REQUEST((short) 0),
    RESPONSE((short) 1);

    private final short code;

    MessageType(short code) {
        this.code = code;
    }

    public short getCode() {
        return code;
    }
}
