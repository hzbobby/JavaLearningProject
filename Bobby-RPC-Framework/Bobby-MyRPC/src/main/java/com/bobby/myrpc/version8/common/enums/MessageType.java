package com.bobby.myrpc.version8.common.enums;

/**
 * @author: Bobby
 * @email: vividbobby@163.com
 * @date: 2025/3/28
 */
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
