什么是 AOP ？

面向切面编程，AOP 它能够做什么？

下面以一个例子，来说名AOP的流程及优点。



以下代码在 [https://github.com/hzbobby/JavaLearningProject/tree/master/Bobby-Aspect](https://github.com/hzbobby/JavaLearningProject/tree/master/Bobby-Aspect)

---

# 故事背景

> 你正在筹办一场表演/演唱会，你请了著名歌手 —— JayChou 来演唱一首歌曲。JayChou 只需要关注在演出开始时，专心唱这首歌就好了。而你需要在演出前置办好场地，布置舞台，演出结束时收拾舞台，针对演出的突发状况进行合理处理等。这些事情是 JayChou 不用管理的。

首先看，我们怎么处理演出部分的。

现在我们有一个演出通用接口

```java
public interface IPerformance {
    void perform();
}
```

JayChou 来表演了

```java
public class JayChouPerformance implements IPerformance {
    @Override
    public void perform() {
        System.out.println("JayChou: oh, 能不能给我一首歌的时间...");
    }
}
```

你作为话事人，需要在 JayChou 演出前后做一些事情，你可以这样。通过依赖注入的方式，注入你需要的演出。

```java
@Data
public class ShowManager implements IPerformance{
    private IPerformance whoPerformance;

    @Override
    public void perform() {
        System.out.println("话事人: 布置舞台...");
        whoPerformance.perform();
        System.out.println("话事人: 收拾舞台...");
    }
}

```

我们在单元测试中，进行调用

```java
@SpringBootTest
public class AspectUnitTest {

    @Resource
    JayChouPerformance jayChouPerformance;

    @Test
    public void performShows() {
        ShowManager showManager = new ShowManager();
        showManager.setWhoPerformance(jayChouPerformance);
        showManager.perform();
    }
}

// 话事人: 布置舞台...
// JayChou: oh, 能不能给我一首歌的时间...
// 话事人: 收拾舞台...
```

如果现在要求你置办 Eason 的演出，且前置和后置步骤都是一样的，你依然可以这样做。只需要将 Eason 的演出注入即可。这个系统依然能满足要求

现在，你已经对置办演出有着非常丰富的经验，你已经是一个成熟的置办演出的系统了。

下面有新的要求过来了。

> 要求二：这次演出是一次公益演出，在演出开始之前，我们要进行一场公益慈善演出。我们要求，你不能侵入已有的演出系统。并且在演出开始前后，需要进行公益活动的准备。
>
> 要求三：Boss 觉得你的能力非常突出，现在把发布会组织，会议组织...活动都交给你来做。同样，在不侵入原有代码下，你该怎么做呢？

上面的要求是，不侵入原有代码，从而实现在各种活动**前后执行一些操作**。如果我们有很多活动都要执行一些固定的操作，或者部分活动需要操作1，部分活动需要操作2，该如何处理。示意图，如下。

![](https://cdn.nlark.com/yuque/0/2025/png/50582501/1742813531330-b4a39a22-6d56-4fdc-b26e-81a73d7ca51e.png)



OK，下面我们正式介绍如何利用 SpringAOP 来实现上述操作。

---

# Spring AOP

参考：

[https://www.baeldung.com/spring-aop](https://www.baeldung.com/spring-aop)

[https://juejin.cn/post/6844903925112373262](https://juejin.cn/post/6844903925112373262)

[https://docs.spring.io/spring-framework/reference/core/aop/introduction-defn.html](https://docs.spring.io/spring-framework/reference/core/aop/introduction-defn.html)





AOP 是一种编程范式，旨在通过分离横切关注点来提高模块化。它通过**在现有代码中添加额外行为而不修改代码本身来实现这一点**。

相反，我们可以分别声明新代码和新行为。



下面先来了解一下几个相关概念

+ JoinPoint 连接点 / 加入点：这是一个抽象且宽泛的概念，它表示能够加入切面的位置，例如方法执行时，异常时等。
+ PointCut 切点：进一步缩小连接点的范围（或切面的范围），例如我们指定在某些类的某个方法上切入。<font style="color:rgb(37, 41, 51);">切点是为了缩小切面所通知的连接点的范围，即切面在何处执行。我们通常使用明确的类和方法名称，或者利用正则表达式定义所匹配的类和方法名称来指定切点。</font>
+ <font style="color:rgb(37, 41, 51);">Advice 通知：表示我们的切面可以在一个函数的哪里执行，例如函数执行开始，结束，返回，异常时。</font>
    - 前置通知(Before)：在目标方法被调用之前调用通知功能
    - <font style="color:rgb(37, 41, 51);">后置通知(After)：在目标方法完成之后调用通知，此时不关心方法的输出结果是什么</font>
    - <font style="color:rgb(37, 41, 51);">返回通知(After-returning)：在目标方法成功执行之后调用通知</font>
    - <font style="color:rgb(37, 41, 51);">异常通知(After-throwing)：在目标方法抛出异常后调用通知</font>
    - <font style="color:rgb(37, 41, 51);">环绕通知(Around)：通知包裹了被通知的方法，在被通知的方法调用之前和调用之后执行自定义的行为</font>
+ <font style="color:rgb(37, 41, 51);">Aspect 切面：</font>**<font style="color:rgb(37, 41, 51);">切面是通知和切点的结合</font>**<font style="color:rgb(37, 41, 51);">。通知和切点共同定义了切面的全部内容：它是什么，在何时和何处完成其功能。在切面里面可以定义：在切点x处通知y做了什么。</font>
+ <font style="color:rgb(37, 41, 51);">Inctroduction 引入：引入允许我们在不修改现有类的基础上，向现有类添加新方法或属性。</font>
+ <font style="color:rgb(37, 41, 51);">Weaving 织入：织入是把切面应用到目标对象并</font>**<font style="color:rgb(37, 41, 51);">创建新的代理对象</font>**<font style="color:rgb(37, 41, 51);">的过程。</font>
    - 编译期：切面在目标类编译时被织入。这种方式需要特殊的编译器。AspectJ的织入编译器就是以这种方式织入切面的。
    - <font style="color:rgb(37, 41, 51);">类加载期：切面在目标类加载到JVM时被织入。这种方式需要特殊的类加载器(ClassLoader)，它可以在目标类被引入应用之前增强该目标类的字节码。</font>
    - <font style="color:rgb(37, 41, 51);">运行期：切面在应用运行的某个时刻被织入。一般情况下，在织入切面时，AOP容器会为目标对象动态地创建一个代理对象。Spring AOP就是以这种方式织入切面的。</font>



下面画一幅简单的示意图，来帮助理解上述过程

![](https://cdn.nlark.com/yuque/0/2025/png/50582501/1742814957858-59ec2968-f8cd-446f-ba9f-0278fd29e5df.png)

---

# AOP 改造

> 要求二：这次演出是一次公益演出，在演出开始之前，我们要进行一场公益慈善演出。我们要求，你不能侵入已有的演出系统。并且在演出开始前后，需要进行公益活动的准备。

OK，现在着手开始我们的改造。

我们的目的是，在 perform() 前后进行一些操作。根据上述示意图，我们需要在 perform() 进行连接。

那么，首先我们先定义一个切面

```java
@Aspect
public class ShowAspect {
    // 在里面定义 切点 和 切面操作
}
```

接着，我们的切面只需要对实现 IPerformance 的实现类进行切入，例如，我们把相关的实现类都放在同一个 package 里面，那么我们可以这样定义切点

```java
@Aspect
public class ShowAspect {
    // 在里面定义 切点 和 切面操作
    @Pointcut("execution(* com.bobby.aspect.shows.*.perform(..))")
    public void perform() {}
}
```

接下来，我们要将通知与切点结合。这里我们加了前置/后置和返回通知

```java
@Aspect
public class ShowAspect {
    // 在里面定义 切点 和 切面操作
    @Pointcut("execution(* com.bobby.aspect.shows.IPerformance.perform(..))")
    public void perform() {}

    @Before("perform()")
    public void performBefore(){
        System.out.println("Before perform");
    }

    @After("perform()")
    public void performAfter(){
        System.out.println("After perform");
    }

    @AfterReturning("perform()")
    public void performAfterReturning(){
        System.out.println("After perform returning");
    }
}
```

接下来，我们将我们定义的切面类注册为 bean 和 所用到的 IPerformance 注册为 Bean

```java
@Component
public class AspectConfig {
    @Bean
    public ShowAspect showAspect(){
        return new ShowAspect();
    }
}

```

OK,我们进行测试一下。

注意到，我们的切点是接口 IPerformance 的 perform方法。因此实现了该接口的方法都会被 Weaving 生成一个代理类。

```java
@SpringBootTest
public class AspectUnitTest {

    @Resource
    JayChouPerformance jayChouPerformance;
    @Resource
    SleepNoMoreDrama sleepNoMoreDrama;

    @Test
    public void performShows() {
        ShowManager showManager = new ShowManager();
        showManager.setWhoPerformance(jayChouPerformance);
        showManager.perform();
    }
}

// 话事人: 布置舞台...
// Before perform
// JayChou: oh, 能不能给我一首歌的时间...
// After perform returning
// After perform
// 话事人: 收拾舞台...
```

OK，这样子我们就实现了，不侵入原有代码，

> 要求三：Boss 觉得你的能力非常突出，现在把发布会组织，会议组织...活动都交给你来做。同样，在不侵入原有代码下，你该怎么做呢？

OK，那现在 Boss 又要求你去组织一下活动，在活动前后要做一些相关事宜。我们可以通过定义一个活动切点，然后定义相关切面即可。

---

# 总结

1. 定义切入点。找到需要切入的位置
2. 定义切面：通知与切入点相结合。我们需要什么通知，在这些通知的位置，我们需要做什么
3. 把Aspect类注册为 Bean，把切点相关类注册为 Bean （这样才能动态代理这些类）

AOP 编程的优点

1. 不侵入原有代码
2. 方便进行拓展
3. 使各个部分专注于自己的逻辑。



如有错误，欢迎指正
