package com.bobby.biza.domain.enums;

public enum MsgStatus {
    SENDING(0),   // 发送中
    SUCCESS(1),   // 成功
    FAIL(2);      // 失败

    private final int status;

    // 构造函数
    MsgStatus(int status) {
        this.status = status;
    }

    // 获取状态值
    public int getStatus() {
        return status;
    }
}