https://www.yuque.com/bobby-ephwj/java/ya5u595epnq0oscx

线程的成熟方案：

+ 雪花算法
+ Leaf
+ 其他变体

# 雪花算法

wiki: [https://zh.wikipedia.org/wiki/%E9%9B%AA%E8%8A%B1%E7%AE%97%E6%B3%95](https://zh.wikipedia.org/wiki/%E9%9B%AA%E8%8A%B1%E7%AE%97%E6%B3%95)

[https://www.cnblogs.com/Damaer/p/15559201.html](https://www.cnblogs.com/Damaer/p/15559201.html)

![](https://cdn.nlark.com/yuque/0/2025/png/50582501/1742734912429-2cfca810-ea54-4cc6-a6e1-3f3875ea3ed5.png)

本质思想：时间是递增的，本算法就是利用时间的性质，解决在分布式服务下要求全局ID的唯一性。

雪花算法整体是一个 64 bit 的序列号。

由 4 部分组成

+ 符号位，不使用
+ 时间戳，41位，可以使用 69 年。一般以项目开始为基准
+ 机器号，也可以分为 机房号，和机房内的机器
+ 序列号，在 1ms 内高并发就递增该序列号。**用于解决某个机房某台机器在ms内的高并发问题。**

**<font style="color:rgb(34, 34, 34);">那么每台机器按照上面的逻辑去生成ID，就会是趋势递增的，因为时间在递增，而且不需要搞个分布式的，简单很多。</font>**

## <font style="color:rgb(34, 34, 34);">时钟回拨问题</font>

<font style="color:rgb(34, 34, 34);">可以看出 snowflake 是</font>**<font style="color:rgb(34, 34, 34);">强依赖于时间的</font>**<font style="color:rgb(34, 34, 34);">，因为时间理论上是不断往前的，所以这一部分的位数，也是趋势递增的。但是有一个问题，是时间回拨，也就是时间突然间倒退了，可能是故障，也可能是重启之后时间获取出问题了。那我们该如何解决时间回拨问题呢？</font>

+ <font style="color:rgb(34, 34, 34);">第一种方案：获取时间的时候判断，如</font>**<font style="color:rgb(34, 34, 34);">果小于上一次的时间戳，那么就不要分配，继续循环获取时间，直到时间符合条件</font>**<font style="color:rgb(34, 34, 34);">。（以下方案就是基于循环获取时间的）</font>
+ <font style="color:rgb(34, 34, 34);">第二种方案：上面的方案只适合时钟回拨较小的，如果间隔过大，阻塞等待，肯定是不可取的，因此要么超过一定大小的回拨直接报错，</font>**<font style="color:rgb(34, 34, 34);">拒绝服务</font>**<font style="color:rgb(34, 34, 34);">，或者有一种方案是</font>**<font style="color:rgb(34, 34, 34);">利用拓展位</font>**<font style="color:rgb(34, 34, 34);">，回拨之后在拓展位上加1就可以了，这样ID依然可以保持唯一。</font>**<font style="color:rgb(34, 34, 34);">时间正常的时候，该拓展位为0，当始终回拨的时候，拓展位+1，这样id就和之前不一样了。</font>**<font style="color:rgb(34, 34, 34);">（拿出一两个bit位，当作始终回拨时的标志位。）</font>
    - <font style="color:rgb(34, 34, 34);">你可以显式拿出 2 个 bit 位。初始时为 00，回拨一次，01，回拨两次 10，回拨三次 11，回拨四次 00...</font>

```java
public class SnowFlake {

    // 数据中心(机房) id
    private long datacenterId;
    // 机器ID
    private long workerId;
    // 同一时间的序列
    private long sequence;

    public SnowFlake(long workerId, long datacenterId) {
        this(workerId, datacenterId, 0);
    }

    public SnowFlake(long workerId, long datacenterId, long sequence) {
        // 合法判断
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        }
        System.out.printf("worker starting. timestamp left shift %d, datacenter id bits %d, worker id bits %d, sequence bits %d, workerid %d",
                          timestampLeftShift, datacenterIdBits, workerIdBits, sequenceBits, workerId);

        this.workerId = workerId;
        this.datacenterId = datacenterId;
        this.sequence = sequence;
    }

    // 开始时间戳
    private long twepoch = 1420041600000L;

    // 机房号，的ID所占的位数 5个bit 最大:11111(2进制)--> 31(10进制)
    private long datacenterIdBits = 5L;

    // 机器ID所占的位数 5个bit 最大:11111(2进制)--> 31(10进制)
    private long workerIdBits = 5L;

    // 5 bit最多只能有31个数字，就是说机器id最多只能是32以内
    private long maxWorkerId = -1L ^ (-1L << workerIdBits);

    // 5 bit最多只能有31个数字，机房id最多只能是32以内
    private long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

    // 同一时间的序列所占的位数 12个bit 111111111111 = 4095  最多就是同一毫秒生成4096个
    private long sequenceBits = 12L;

    // workerId的偏移量
    private long workerIdShift = sequenceBits;

    // datacenterId的偏移量
    private long datacenterIdShift = sequenceBits + workerIdBits;

    // timestampLeft的偏移量
    private long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

    // 序列号掩码 4095 (0b111111111111=0xfff=4095)
    // 用于序号的与运算，保证序号最大值在0-4095之间
    private long sequenceMask = -1L ^ (-1L << sequenceBits);

    // 最近一次时间戳
    private long lastTimestamp = -1L;


    // 获取机器ID
    public long getWorkerId() {
        return workerId;
    }


    // 获取机房ID
    public long getDatacenterId() {
        return datacenterId;
    }


    // 获取最新一次获取的时间戳
    public long getLastTimestamp() {
        return lastTimestamp;
    }

    // 加锁，或者以单例的方式获取
    // 获取下一个随机的ID
    public synchronized long nextId() {
        // 获取当前时间戳，单位毫秒
        long timestamp = timeGen();
        // 检查始终回拨
        if (timestamp < lastTimestamp) {
            System.err.printf("clock is moving backwards.  Rejecting requests until %d.", lastTimestamp);
            throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds",
                                                     lastTimestamp - timestamp));
        }

        // 去重
        if (lastTimestamp == timestamp) {

            sequence = (sequence + 1) & sequenceMask;

            // sequence序列大于4095
            if (sequence == 0) {
                // 调用到下一个时间戳的方法
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 如果是当前时间的第一次获取，那么就置为0
            sequence = 0;
        }

        // 记录上一次的时间戳
        lastTimestamp = timestamp;

        // 偏移计算
        return ((timestamp - twepoch) << timestampLeftShift) |
                (datacenterId << datacenterIdShift) |
                (workerId << workerIdShift) |
                sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        // 获取最新时间戳
        long timestamp = timeGen();
        // 如果发现最新的时间戳小于或者等于序列号已经超4095的那个时间戳
        while (timestamp <= lastTimestamp) {
            // 不符合则继续
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }

    public static void main(String[] args) {
        SnowFlake worker = new SnowFlake(1, 1);
        long timer = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            worker.nextId();
        }
        System.out.println(System.currentTimeMillis());
        System.out.println(System.currentTimeMillis() - timer);
    }

}
```



## 要点

+ 符号位，时间戳，机器号，序列号。分别是用来解决什么的
+ 时钟回拨问题要怎么处理：等待；拓展位
+ 要以单例的方式 / 加锁 进行获取 id

