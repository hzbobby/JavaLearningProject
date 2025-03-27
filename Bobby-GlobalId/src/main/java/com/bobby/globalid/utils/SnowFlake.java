package com.bobby.globalid.utils;

import lombok.extern.slf4j.Slf4j;

public class SnowFlake {
    // 雪花算法以时间这个固有增长的性质，来解决分布式下ID冲突
    // 雪花算法有 4 个字段
    // 1. 符号位，不使用
    // 2. 时间戳 41位
    // 3. 机器号 10位
    // 4. 序列号 12位
    // 时间戳可以根据系统当前的时间获取，这部分存在 id 的高位字段
    // 机器号一般可以在划分位机房号和房内机器号，一般各 5 个字段

    // 雪花算法可能会遇到的问题：始终回拨
    // 如何解决？
    // 1. 短时间可以采取等待策略
    // 2. 再抽出字段来解决始终回拨


    // ok, 现在开始定义字段
    private long datacenterId;
    private long machineId;
    private long sequenceId;

    // 定义每个字段占用的 bits 数量
    private long datacenterIdBits = 5L;
    private long machineIdBits = 5L;
    private long sequenceIdBits = 12L;

    // 根据占用的 bits 数量，可以计算出机器号和序列号的最大id
    // -1L << datacenterIdBits == 1111 1111 1110 0000
    // 异或操作                     1111 1111 1111 1111
    //                             0000 0000 0001 1111
    private long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);
    private long maxMachineId = -1L ^ (-1L << machineIdBits);

    // sequence 掩码，用于限定范围
    private long sequenceIdMask = -1L ^ (-1L << sequenceIdBits);

    // 定义移位
    private long machineIdLeftShift = sequenceIdBits;
    private long datacenterIdLeftShift = sequenceIdBits + machineIdBits;
    private long timestampLeftShift = sequenceIdBits + machineIdBits + datacenterIdBits;

    // 上次生成 id 的时间戳
    private long lastTimestamp = -1L;
    // 基准时间。雪花算法可以保证基准时间往后 69 年产生不重复id
    private long twepoch = 1420041600000L;


    public SnowFlake(long datacenterId, long machineId) {
        this(datacenterId, machineId, 0L);
    }

    public SnowFlake(long datacenterId, long machineId, long sequenceId) {
        // 判断给定字段是否合法
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId: " + datacenterId + " is invalid");
        }
        if (machineId > maxMachineId || machineId < 0) {
            throw new IllegalArgumentException("machineId: " + machineId + " is invalid");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
        this.sequenceId = sequenceId;
    }

    // 加锁
    public synchronized long nextId() {
        // 获取下一个全局 ID

        long timestamp = timeGen();
        // 1. 检测是否始终回拨
        if (timestamp < lastTimestamp) {
//            log.error(String.format("Clock is moving backwards, refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
            throw new RuntimeException(String.format("Clock is moving backwards, refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        // 2. 同一毫秒内高并发
        if (timestamp == lastTimestamp) {
            // 采取 sequence 自增策略
            sequenceId = (sequenceId + 1) & sequenceIdMask;

            if (sequenceId == 0) {
                // 毫秒内耗尽，等到下一个毫秒
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 第一次获取
            sequenceId = 0L;
        }
        // 3. 记录上一次产生 ID 的时间戳
        lastTimestamp = timestamp;

        return ((timestamp - twepoch) << timestampLeftShift) |
                (datacenterId << datacenterIdLeftShift) |
                (machineId << machineIdLeftShift) |
                sequenceId;
    }

    private long tilNextMillis(long lastTimestamp) {
        // 自旋等待
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }

    public static void main(String[] args) {
        SnowFlake snowFlake = new SnowFlake(2, 5);
        for (int i = 0; i < 100; i++) {
            System.out.println(snowFlake.nextId());
        }
    }
}
