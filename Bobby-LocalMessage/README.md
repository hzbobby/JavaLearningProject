# 侵入小，易拓展的本地消息框架

相关代码在：

[https://github.com/hzbobby/JavaLearningProject/tree/master/Bobby-LocalMessage](https://github.com/hzbobby/JavaLearningProject/tree/master/Bobby-LocalMessage)



参考：

[https://www.bilibili.com/video/BV1h3Q8YBEpb/?spm_id_from=333.1387.homepage.video_card.click&vd_source=4ab22e04dc78ada80a026ce380e242e6](https://www.bilibili.com/video/BV1h3Q8YBEpb/?spm_id_from=333.1387.homepage.video_card.click&vd_source=4ab22e04dc78ada80a026ce380e242e6)





消息表的一条记录对应一个方法

```java
CREATE TABLE `local_message_aop` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` tinyint NOT NULL DEFAULT 0,
    `req_snapshot` TEXT NOT NULL COMMENT '请求快照参数json',
    `status` int NOT NULL DEFAULT 0 COMMENT '状态 INIT, FAIL, SUCCESS',
    `next_retry_time` BIGINT NOT NULL COMMENT '下一次重试的时间',
    `retry_times` int NOT NULL DEFAULT 0 COMMENT '已经重试的次数',
    `max_retry_times` int NOT NULL COMMENT '最大重试次数',
    `fail_reason` text COMMENT '执行失败的信息',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='本地消息表';
```

思路流程

![](https://cdn.nlark.com/yuque/0/2025/png/50582501/1743044521715-3525d8ee-9aa9-4865-81f0-52f9f0756709.png)



利用 AOP 切面获取到被注解方法。通过反射调用被注解方法执行三方调用逻辑。



只要将该注解用到 三方调用方法上，同时，在业务中需要调用代理对象的该方法，才可以使切面生效。



我们将三方调用单独放在一个服务类里，本地业务调用时，就不需要再做代理处理了。



代码在仓库里，下面看一下 demo 演示的结果

```java
@Slf4j
@Service
public class ThirdPartyInvokeServiceImpl implements IThirdPartyInvokeService {

    @LocalMessage(maxRetryTimes = 5, async = true)
    @Override
    public void thirdPartyInvoke() {
        log.info("I'm third party invoke");
    }
}
```

```java
@RequiredArgsConstructor
@Slf4j
@Service
public class LocalBizServiceImpl implements ILocalBizService {

    private final IThirdPartyInvokeService thirdPartyInvokeService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void doBiz() {
        log.info("now, we do local Biz");
        // 三方调用
        thirdPartyInvokeService.thirdPartyInvoke();
        log.info("local biz done");
    }
}
```

```java

@ActiveProfiles("local")
@SpringBootTest
public class TestDemo {

    @Resource
    private ILocalBizService localBizService;

    @Resource
    private ILocalMessageDOService localMessageDOService;

    @Test
    public void testLocalMsg() {
        localBizService.doBiz();
    }

    @Test
    public void writeLocalMsgTest(){
        LocalMessageDO demo = LocalMessageDO.of("demo", 5, 2);
        localMessageDOService.save(demo);
    }

}
```

结果如下：

```java
2025-03-27T12:22:31.570+08:00  INFO 22884 --- [bobby-localmessage] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2025-03-27T12:22:31.848+08:00  INFO 22884 --- [bobby-localmessage] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection com.mysql.cj.jdbc.ConnectionImpl@5ced0537
2025-03-27T12:22:31.850+08:00  INFO 22884 --- [bobby-localmessage] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2025-03-27T12:22:31.855+08:00  INFO 22884 --- [bobby-localmessage] [           main] c.b.l.service.impl.LocalBizServiceImpl   : now, we do local Biz
2025-03-27T12:22:31.860+08:00  INFO 22884 --- [bobby-localmessage] [           main] c.b.l.aspect.LocalMessageAspect          : LocalMessageAspect.doAspect
2025-03-27T12:22:32.024+08:00 DEBUG 22884 --- [bobby-localmessage] [           main] c.b.l.aspect.LocalMessageAspect          : record local message, record:{"argsJson":"[]","className":"com.bobby.localmessage.service.impl.ThirdPartyInvokeServiceImpl","methodName":"thirdPartyInvoke","paramTypesJson":"[]"}, async:true
2025-03-27T12:22:32.102+08:00 DEBUG 22884 --- [bobby-localmessage] [           main] c.b.l.m.LocalMessageDOMapper.insert      : ==>  Preparing: INSERT INTO local_message ( req_snapshot, status, next_retry_time, retry_times, max_retry_times ) VALUES ( ?, ?, ?, ?, ? )
2025-03-27T12:22:32.187+08:00 DEBUG 22884 --- [bobby-localmessage] [           main] c.b.l.m.LocalMessageDOMapper.insert      : ==> Parameters: {"argsJson":"[]","className":"com.bobby.localmessage.service.impl.ThirdPartyInvokeServiceImpl","methodName":"thirdPartyInvoke","paramTypesJson":"[]"}(String), 0(Integer), 1743049412023(Long), 1(Integer), 5(Integer)
2025-03-27T12:22:32.197+08:00 DEBUG 22884 --- [bobby-localmessage] [           main] c.b.l.m.LocalMessageDOMapper.insert      : <==    Updates: 1
2025-03-27T12:22:32.218+08:00  INFO 22884 --- [bobby-localmessage] [           main] c.b.l.service.impl.LocalBizServiceImpl   : local biz done
2025-03-27T12:22:32.225+08:00 DEBUG 22884 --- [bobby-localmessage] [           main] c.b.l.s.impl.LocalMessageDOServiceImpl   : afterCompletion committed, 开始执行三方调用
2025-03-27T12:22:32.254+08:00  INFO 22884 --- [bobby-localmessage] [pool-2-thread-1] c.b.l.aspect.LocalMessageAspect          : LocalMessageAspect.doAspect
2025-03-27T12:22:32.254+08:00 DEBUG 22884 --- [bobby-localmessage] [pool-2-thread-1] c.b.l.aspect.LocalMessageAspect          : 被切方法已经在 invoke
2025-03-27T12:22:32.254+08:00  INFO 22884 --- [bobby-localmessage] [pool-2-thread-1] c.b.l.s.i.ThirdPartyInvokeServiceImpl    : I'm third party invoke
2025-03-27T12:22:32.261+08:00  INFO 22884 --- [bobby-localmessage] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2025-03-27T12:22:32.278+08:00  INFO 22884 --- [bobby-localmessage] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
```

这里可以看到三方调用是在本地事务结束后才进行调用的。同时本地消息是在事务内进行持久化的



我们开启了定时任务来捞哪些重试的消息。我们去数据库中，把消息改为 RETRY

![](https://cdn.nlark.com/yuque/0/2025/png/50582501/1743049518761-2f0d5006-1ec8-4566-8b03-7ffb7623ba9b.png)

```java
2025-03-27T12:27:00.212+08:00  INFO 1756 --- [bobby-localmessage] [pool-2-thread-5] c.b.l.aspect.LocalMessageAspect          : LocalMessageAspect.doAspect
2025-03-27T12:27:00.212+08:00 DEBUG 1756 --- [bobby-localmessage] [pool-2-thread-4] c.b.l.aspect.LocalMessageAspect          : 被切方法已经在 invoke
2025-03-27T12:27:00.212+08:00 DEBUG 1756 --- [bobby-localmessage] [pool-2-thread-5] c.b.l.aspect.LocalMessageAspect          : 被切方法已经在 invoke
2025-03-27T12:27:00.212+08:00  INFO 1756 --- [bobby-localmessage] [pool-2-thread-4] c.b.l.s.i.ThirdPartyInvokeServiceImpl    : I'm third party invoke
2025-03-27T12:27:00.212+08:00  INFO 1756 --- [bobby-localmessage] [pool-2-thread-5] c.b.l.s.i.ThirdPartyInvokeServiceImpl    : I'm third party invoke
2025-03-27T12:27:00.213+08:00 DEBUG 1756 --- [bobby-localmessage] [pool-2-thread-5] c.b.l.m.LocalMessageDOMapper.update      : ==>  Preparing: UPDATE local_message SET create_time=?, update_time=?, deleted=?, req_snapshot=?, status=?, next_retry_time=?, retry_times=?, max_retry_times=?
2025-03-27T12:27:00.213+08:00 DEBUG 1756 --- [bobby-localmessage] [pool-2-thread-5] c.b.l.m.LocalMessageDOMapper.update      : ==> Parameters: 2025-03-27T12:18:52(LocalDateTime), 2025-03-27T12:26:58(LocalDateTime), 0(Integer), {"argsJson":"[]","className":"com.bobby.localmessage.service.impl.ThirdPartyInvokeServiceImpl","methodName":"thirdPartyInvoke","paramTypesJson":"[]"}(String), 1(Integer), 1743049192331(Long), 1(Integer), 5(Integer)
2025-03-27T12:27:00.213+08:00 DEBUG 1756 --- [bobby-localmessage] [pool-2-thread-4] c.b.l.m.LocalMessageDOMapper.update      : ==>  Preparing: UPDATE local_message SET create_time=?, update_time=?, deleted=?, req_snapshot=?, status=?, next_retry_time=?, retry_times=?, max_retry_times=?
2025-03-27T12:27:00.213+08:00 DEBUG 1756 --- [bobby-localmessage] [pool-2-thread-4] c.b.l.m.LocalMessageDOMapper.update      : ==> Parameters: 2025-03-27T12:18:52(LocalDateTime), 2025-03-27T12:27(LocalDateTime), 0(Integer), {"argsJson":"[]","className":"com.bobby.localmessage.service.impl.ThirdPartyInvokeServiceImpl","methodName":"thirdPartyInvoke","paramTypesJson":"[]"}(String), 1(Integer), 1743049192331(Long), 1(Integer), 5(Integer)
2025-03-27T12:27:00.222+08:00 DEBUG 1756 --- [bobby-localmessage] [pool-2-thread-5] c.b.l.m.LocalMessageDOMapper.update      : <==    Updates: 5
2025-03-27T12:27:00.224+08:00 DEBUG 1756 --- [bobby-localmessage] [pool-2-thread-4] c.b.l.m.LocalMessageDOMapper.update      : <==    Updates: 5
2025-03-27T12:27:05.214+08:00 DEBUG 1756 --- [bobby-localmessage] [   scheduling-1] c.b.l.s.impl.LocalMessageDOServiceImpl   : retryLocalMsg
2025-03-27T12:27:05.215+08:00 DEBUG 1756 --- [bobby-localmessage] [   scheduling-1] c.b.l.m.LocalMessageDOMapper.selectList  : ==>  Preparing: SELECT id,create_time,update_time,deleted,req_snapshot,status,next_retry_time,retry_times,max_retry_times,fail_reason FROM local_message WHERE (next_retry_time <= ? AND status = ?)
2025-03-27T12:27:05.215+08:00 DEBUG 1756 --- [bobby-localmessage] [   scheduling-1] c.b.l.m.LocalMessageDOMapper.selectList  : ==> Parameters: 1743049625214(Long), 2(Integer)
2025-03-27T12:27:05.218+08:00 DEBUG 1756 --- [bobby-localmessage] [   scheduling-1] c.b.l.m.LocalMessageDOMapper.selectList  : <==      Total: 1
2025-03-27T12:27:05.218+08:00  INFO 1756 --- [bobby-localmessage] [pool-2-thread-6] c.b.l.aspect.LocalMessageAspect          : LocalMessageAspect.doAspect
2025-03-27T12:27:05.218+08:00 DEBUG 1756 --- [bobby-localmessage] [pool-2-thread-6] c.b.l.aspect.LocalMessageAspect          : 被切方法已经在 invoke
2025-03-27T12:27:05.218+08:00  INFO 1756 --- [bobby-localmessage] [pool-2-thread-6] c.b.l.s.i.ThirdPartyInvokeServiceImpl    : I'm third party invoke
2025-03-27T12:27:05.219+08:00 DEBUG 1756 --- [bobby-localmessage] [pool-2-thread-6] c.b.l.m.LocalMessageDOMapper.update      : ==>  Preparing: UPDATE local_message SET create_time=?, update_time=?, deleted=?, req_snapshot=?, status=?, next_retry_time=?, retry_times=?, max_retry_times=?
2025-03-27T12:27:05.219+08:00 DEBUG 1756 --- [bobby-localmessage] [pool-2-thread-6] c.b.l.m.LocalMessageDOMapper.update      : ==> Parameters: 2025-03-27T12:18:52(LocalDateTime), 2025-03-27T12:27:01(LocalDateTime), 0(Integer), {"argsJson":"[]","className":"com.bobby.localmessage.service.impl.ThirdPartyInvokeServiceImpl","methodName":"thirdPartyInvoke","paramTypesJson":"[]"}(String), 1(Integer), 1743049192331(Long), 1(Integer), 5(Integer)
2025-03-27T12:27:05.227+08:00 DEBUG 1756 --- [bobby-localmessage] [pool-2-thread-6] c.b.l.m.LocalMessageDOMapper.update      : <==    Updates: 5
2025-03-27T12:27:10.213+08:00 DEBUG 1756 --- [bobby-localmessage] [   scheduling-1] c.b.l.s.impl.LocalMessageDOServiceImpl   : retryLocalMsg
2025-03-27T12:27:10.218+08:00 DEBUG 1756 --- [bobby-localmessage] [   scheduling-1] c.b.l.m.LocalMessageDOMapper.selectList  : ==>  Preparing: SELECT id,create_time,update_time,deleted,req_snapshot,status,next_retry_time,retry_times,max_retry_times,fail_reason FROM local_message WHERE (next_retry_time <= ? AND status = ?)
2025-03-27T12:27:10.219+08:00 DEBUG 1756 --- [bobby-localmessage] [   scheduling-1] c.b.l.m.LocalMessageDOMapper.selectList  : ==> Parameters: 1743049630214(Long), 2(Integer)
2025-03-27T12:27:10.221+08:00 DEBUG 1756 --- [bobby-localmessage] [   scheduling-1] c.b.l.m.LocalMessageDOMapper.selectList  : <==      Total: 0
```

可以看到，消息成功被监听到了。



## 存在问题

事务回滚怎么办？

因为本地消息是在事务里面进行的，因此事务失败，发生回滚，本地消息的存储也是不会被提交的。



调用时，没有做幂等检查



```java
private void doInvoke(LocalMessageDO localMessageDO) {
        // 1. 获取三方调用方法的信息，进行反射，构造出该方法
        String reqSnapshot = localMessageDO.getReqSnapshot();
        if (StrUtil.isBlank(reqSnapshot)) {
            log.warn("Request snapshot is empty, record: {}", localMessageDO.getId());
            invokeFail(localMessageDO, "Request snapshot is empty.");
            return;
        }
        // 消息幂等性检查
        // 防止重试的消息
        // TODO idempontant

        // 2. 构造调用信息
        InvokeCtx ctx = JSON.parseObject(reqSnapshot, InvokeCtx.class);

        // 3. 在这里进行反射调用
        try {
            // 4. 记录一下调用状态，方便快速失败，防止多次调用
            InvokeStatusHolder.startInvoke();
            // 5. 下面就是反射获取原方法了。注意，这里三方调用是获取了该方法的代理对象 bean
            Class<?> target = Class.forName(ctx.getClassName());
            Object bean = applicationContext.getBean(target);

            List<Class<?>> paramTypes = getParamTypes(JSON.parseArray(ctx.getParamTypesJson(), String.class));
            Method method = target.getMethod(ctx.getMethodName(), paramTypes.toArray(new Class[0]));
            Object[] args = getArgs(paramTypes, ctx.getArgsJson());

            method.invoke(bean, args);
            // 6. 更新本地消息表的状态
            invokeSuccess(localMessageDO);
        } catch (ClassNotFoundException e) {
            // 7. 更新失败信息
            invokeFail(localMessageDO, "ClassNotFoundException: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            invokeFail(localMessageDO, "NoSuchMethodException: " + e.getMessage());

            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            invokeFail(localMessageDO, "InvocationTargetException: " + e.getMessage());

            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            invokeFail(localMessageDO, "IllegalAccessException: " + e.getMessage());

            throw new RuntimeException(e);
        } finally {
            // 8. 结束调用
            InvokeStatusHolder.endInvoke();
        }
    }
```



# 
