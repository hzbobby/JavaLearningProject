package com.bobby.myrpc.version5;

/**
 * @author: Bobby
 * @email: vividbobby@163.com
 * @date: 2025/3/28
 */
public enum MessageType {
    REQUEST(0),
    RESPONSE(1);

    private final int code;

    MessageType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
