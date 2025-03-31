# 了解 RPC

先来了解一下 RPC 是什么

参考：

[https://javaguide.cn/distributed-system/rpc/rpc-intro.html](https://javaguide.cn/distributed-system/rpc/rpc-intro.html#rpc-%E7%9A%84%E5%8E%9F%E7%90%86%E6%98%AF%E4%BB%80%E4%B9%88)

---

Remote Process Call 远程过程调用。在分布式服务下，不同服务部署在不同机器上，由于服务不在同一块内存上，服务A如何去调用服务B呢？

通过 网络 来调用

RPC 的目的是，**使调用远程服务就像调用本地服务一样方便。**

一个 RPC 可以由以下几部分组成

+ **客户端（服务消费端）**：调用远程方法的一端。
+ **客户端 Stub（桩）**：这其实就是一代理类。代理类主要做的事情很简单，就是把你调用方法、类、方法参数等信息传递到服务端。
+ **网络传输**：网络传输就是你要把你调用的方法的信息比如说参数啊这些东西传输到服务端，然后服务端执行完之后再把返回结果通过网络传输给你传输回来。网络传输的实现方式有很多种比如最基本的 Socket 或者性能以及封装更加优秀的 Netty（推荐）。
+ **服务端 Stub（桩）**：这个桩就不是代理类了。我觉得理解为桩实际不太好，大家注意一下就好。这里的服务端 Stub 实际指的就是接收到客户端执行方法的请求后，去执行对应的方法然后返回结果给客户端的类。
+ **服务端（服务提供端）**：提供远程方法的一端。

原理如下：

![](https://cdn.nlark.com/yuque/0/2025/jpeg/50582501/1743137593320-7ab1d08e-4062-461e-be27-e0c63aac1fab.jpeg)

![](https://camo.githubusercontent.com/4e8904371fbde4068fe676939bd62a9c0db6423e5cecd13d326ceeedfdb772a6/687474703a2f2f67616e676875616e2e6f73732d636e2d7368656e7a68656e2e616c6979756e63732e636f6d2f696d672f696d6167652d32303230303830353132343735393230362e706e67)

+ 服务消费端（client）以本地调用的方式调用远程服务；
+ 客户端 Stub（client stub） 接收到调用后负责将方法、参数等组装成能够进行网络传输的消息体（序列化）：`RpcRequest`；
+ 客户端 Stub（client stub） 找到远程服务的地址，并将消息发送到服务提供端；
+ 服务端 Stub（桩）收到消息将消息反序列化为 Java 对象: `RpcRequest`；
+ 服务端 Stub（桩）根据`RpcRequest`中的类、方法、方法参数等信息调用本地的方法；
+ 服务端 Stub（桩）得到方法执行结果并将组装成能够进行网络传输的消息体：`RpcResponse`（序列化）发送至消费方；
+ 客户端 Stub（client stub）接收到消息并将消息反序列化为 Java 对象:`RpcResponse` ，这样也就得到了最终结果。over!



本质上，就多了一层：**代理**

+ 客户端代理类，负责把包装请求。
+ 服务端代理类，负责解析请求然后调用，返回结果
+ 他们之间靠网络来进行通信。



# 手撕 RPC

参考：

[https://github.com/he2121/MyRPCFromZero?tab=readme-ov-file](https://github.com/he2121/MyRPCFromZero?tab=readme-ov-file)

下面一步步从最简陋的 RPC 封装成可用的 RPC

故事背景：

本地客户端想要调用服务端的获取用户的服务

我们在服务端封装了，该接口及实现类

```java
@Data
@Builder
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String username;
    private Integer age;
}

```

```java
@Slf4j
@Service
public class UserServiceImpl implements IUserService {
    @Override
    public User getUserById(Long id) {
        log.info("getUserById, id: {}", id);
        // 模拟数据库，返回一个用户
        // 模拟从数据库中取用户的行为
        Random random = new Random();
        return User.builder().username(UUID.randomUUID().toString())
                .id(id)
                .age(random.nextInt()).build();
    }
}
```

---

## 原始 Socket 通信

最简陋的版本，相当于没有 ClientStub, ServerStub，且对服务调用没做任何封装。

我只知道我需要调用服务的功能，那么，我直接写个 Socket 通信，请求服务端返回给我信息。

服务端也需要写一个对应的服务，来提供这个特定的功能。



**服务端：**

以 BIO 的方式阻塞等待客户端的请求 `serverSocket.accept()`

```java
public class RPCServer {
    public static void main(String[] args) {
        UserServiceImpl userService = new UserServiceImpl();
        try {
            ServerSocket serverSocket = new ServerSocket(8899);
            System.out.println("服务端启动了");
            // BIO的方式监听Socket
            while (true) {
                Socket socket = serverSocket.accept();
                // 开启一个线程去处理
                new Thread(() -> {
                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                        // 读取客户端传过来的id
                        Long id = ois.readLong();
                        User user = userService.getUserById(id);
                        // 写入User对象给客户端
                        oos.writeObject(user);
                        oos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("从IO中读取数据错误");
                    }
                }).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("服务器启动失败");
        }
    }
}
```

**客户端**

```java
public class RPCClient {
    public static void main(String[] args) {
        try {
            // 建立Socket连接
            Socket socket = new Socket("127.0.0.1", 8899);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            // 传给服务器id
            objectOutputStream.writeLong(new Random().nextLong());
            objectOutputStream.flush();
            // 服务器查询数据，返回对应的对象
            User user = (User) objectInputStream.readObject();
            System.out.println("服务端返回的User:" + user);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("客户端启动失败");
        }
    }
}
```



### 改进点

1. <font style="color:rgb(31, 35, 40);">只能调用服务端 Service 唯一确定的方法，如果有两个方法需要调用呢?（Reuest需要抽象）</font>
2. <font style="color:rgb(31, 35, 40);">返回值只支持 User 对象，如果需要传一个字符串或者一个 Dog，String 对象呢（ Response 需要抽象）</font>
3. <font style="color:rgb(31, 35, 40);">客户端不够通用，host，port， 与调用的方法都特定（需要抽象）</font>



## <font style="color:rgb(31, 35, 40);">封装 Request, Response 使之更加通用</font>

上述 RPCServer 和 RPCClient 的请求和响应都很“专用”，意味着对于每个服务都得写一个这样的server & client。

因此，将请求和响应抽象出来。

服务端要做的就是

+ 读取请求
+ 调用对应服务    （通过，反射机制，拿到对应方法）
+ 返回响应

客户端要做的就是

+ 构造请求
+ 接受响应



为了使用反射机制，就要求请求中必须带有（调用方法，参数类型，参数等）

```java
/**
 * 在上个例子中，我们的Request仅仅只发送了一个id参数过去，这显然是不合理的，
 * 因为服务端不会只有一个服务一个方法，因此只传递参数不会知道调用那个方法
 * 因此一个RPC请求中，client发送应该是需要调用的Service接口名，方法名，参数，参数类型
 * 这样服务端就能根据这些信息根据反射调用相应的方法
 * 还是使用java自带的序列化方式
 */
@Data
@Builder
public class RPCRequest implements Serializable {
    // 服务类名，客户端只知道接口名，在服务端中用接口名指向实现类
    private String interfaceName;
    // 方法名
    private String methodName;
    // 参数列表
    private Object[] params;
    // 参数类型
    private Class<?>[] paramsTypes;
}
```

```java
/**
 * 上个例子中response传输的是User对象，显然在一个应用中我们不可能只传输一种类型的数据
 * 由此我们将传输对象抽象成为Object
 * Rpc需要经过网络传输，有可能失败，类似于http，引入状态码和状态信息表示服务调用成功还是失败
 */
@Data
@Builder
public class RPCResponse implements Serializable {
    // 状态信息
    private int code;
    private String message;
    // 具体数据
    private Object data;

    public static RPCResponse success(Object data) {
        return RPCResponse.builder().code(200).data(data).build();
    }

    public static RPCResponse fail() {
        return RPCResponse.builder().code(500).message("服务器发生错误").build();
    }
}
```

```java
@Slf4j
public class RPCServer {

    IUserService userService = new UserServiceImpl();

    public static void main(String[] args) {
        UserServiceImpl userService = new UserServiceImpl();
        try {
            ServerSocket serverSocket = new ServerSocket(8899);
            System.out.println("服务端启动了");
            // BIO的方式监听Socket
            while (true) {
                Socket socket = serverSocket.accept();
                // 开启一个线程去处理
                new Thread(() -> {
                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                        // 这里接受客户端传过来的 通用 请求
                        // 通过反射来解析
                        RPCRequest rpcRequest = (RPCRequest) ois.readObject();
                        Method method = userService.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamsTypes());
                        Object result = method.invoke(userService, rpcRequest.getParams());
                        // 将结果封装到 Response
                        RPCResponse response = RPCResponse.builder().data(result).code(200).message("OK").build();
                        oos.writeObject(response);
                        oos.flush();
                    } catch (IOException e) {
                        log.error("从IO中读取数据错误: {}", e.getMessage());
                    } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                             IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("服务器启动失败");
        }
    }
}
```

```java
public class RPCClient {
    public static void main(String[] args) {
        try {
            // 建立Socket连接
            Socket socket = new Socket("127.0.0.1", 8899);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            // 客户端构造请求
            RPCRequest request = RPCRequest.builder()
                    .interfaceName("com.bobby.rpc.service.IUserService")
                    .methodName("getUserById")
                    .paramsTypes(new Class[]{Long.class})
                    .params(new Object[]{new Random().nextLong()})
                    .build();
            // 发送请求
            objectOutputStream.writeObject(request);
            RPCResponse response = (RPCResponse) objectInputStream.readObject();

            System.out.println("服务端返回的User:" + response.toString());

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("客户端启动失败");
        }
    }
}
```

这里服务端通过反射机制，能获取 UserService 里面各种方法的调用。

客户端虽然把请求和响应抽象出来了，但是 <font style="color:rgb(31, 35, 40);">host，port， 与调用的方法(只能调用 IUservice )都特定（下面继续改进）</font>

<font style="color:rgb(31, 35, 40);">怎么改呢？我们的目的是希望客户端能够与 host, port 甚至 服务类的特定方法 抽离。</font>

<font style="color:rgb(31, 35, 40);">那就意味着，我们调用一个方法，有个东西能帮我们构建出请求，并且在</font>**<font style="color:rgb(31, 35, 40);">每一次调用时都能构建出对应请求</font>**<font style="color:rgb(31, 35, 40);">。例如，我们想要调用 </font>`<font style="color:rgb(31, 35, 40);">getUserById</font>`<font style="color:rgb(31, 35, 40);">，那个东西就能帮助我们构建出对应的请求。</font>

<font style="color:rgb(31, 35, 40);">ok，那个东西就是 动态代理。JDK 动态代理，可以让代理类在调用每一个方法时，都执行 invoke 逻辑。（适合用来构建 request 请求）</font>

<font style="color:rgb(31, 35, 40);">架构进化图如下</font>

![](https://cdn.nlark.com/yuque/0/2025/png/50582501/1743143559660-e8b782f4-009b-48af-84d8-a8f8da2cbdfb.png)

因此，我们可以将 RPCClient 的代码简化成如下：

```java
/**
 * version 1: 抽取通用请求、响应，并利用代理模式抽象出服务类的代理。在调用代理对象的每个方法时，构建对应的请求并建立socket通信
 */
public class RPCClient {
    public static void main(String[] args) {
        // 使用代理类
        ClientProxy clientProxy = new ClientProxy("127.0.0.1", 8899);
        IUserService proxyService = clientProxy.getProxy(IUserService.class);

        User user = proxyService.getUserById(new Random().nextLong());
        System.out.println(user);

        // 调用其他方法
    }
}
```

```java
public class ClientProxy implements InvocationHandler {
    private String host;
    private int port;

    public ClientProxy(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 代理对象执行每个方法时，都将执行这里的逻辑
        // 我们的目的是，利用这个代理类帮助我构建请求。这样能够有效减少重复的代码
        RPCRequest request = RPCRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .paramsTypes(method.getParameterTypes())
                .params(args)
                .build();
        // 然后将这个请求发送到服务端，并获取响应
        RPCResponse response = IOClient.sendRequest(host, port, request);
        return response.getData();
    }

    // 获取代理对象
    public <T> T getProxy(Class<T> clazz) {
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        return (T) o;
    }
}

```

```java
/**
 * 我们将通信这层逻辑抽离出来
 */
@Slf4j
public class IOClient {
    public static RPCResponse sendRequest(String host, int port, RPCRequest request) {
        try {
            Socket socket = new Socket(host, port);

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            // 发送请求
            objectOutputStream.writeObject(request);
            // 获取响应
            RPCResponse response = (RPCResponse) objectInputStream.readObject();
            return response;
        } catch (IOException | ClassNotFoundException e) {
            log.error("IOClient, sendRequest Exception: ", e);
            return null;
        }
    }
}
```

<font style="color:rgb(31, 35, 40);">因此，我们可以在客户端调用多个服务类不同的方法，而且不用再针对不同方法构造特定请求，建立socket 等</font>

### <font style="color:rgb(31, 35, 40);">总结</font>

1. 通用的 Requeset 和 Response
2. 利用代理类，通用地进行处理每个服务类方法请求的构建
3. socket通信与构建请求分离，降低耦合度



存在问题：

+ 服务端只针对 UserService 接受请求，如果有别的服务呢？（多服务注册）
+ 服务端 BIO 性能低
+ 服务端耦合度高：监听、执行调用。。。



## 多服务注册 & 松耦合

在这节中，我们将改造服务端以支持

+ 多服务注册。构造一个通用的服务端
+ 服务端松耦合



在开始之前，我们先添加一些其他服务，如 BlogService 来模拟多服务

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Blog implements Serializable {
    private Integer id;
    private Integer useId;
    private String title;
}
```

```java
@Service
public class BlogServiceImpl implements IBlogService {
    @Override
    public Blog getBlogById(Integer id) {
        Blog blog = Blog.builder().id(id).title("我的博客").useId(22).build();
        System.out.println("客户端查询了" + id + "博客");
        return blog;
    }
}
```

<font style="color:rgb(31, 35, 40);">ok，接下来先解决多服务问题</font>

<font style="color:rgb(31, 35, 40);">简单，加一个 Map 不就好了嘛，我们在 Server 处，添加一个 Map 或者抽象出一个 服务提供者。</font>

<font style="color:rgb(31, 35, 40);">服务提供者可以Map实现</font>

```java
/**
 * 之前这里使用Map简单实现的
 * 存放服务接口名与服务端对应的实现类
 * 服务启动时要暴露其相关的实现类0
 * 根据request中的interface调用服务端中相关实现类
 */
public class ServiceProvider {
    /**
     * 一个实现类可能实现多个接口
     */
    private Map<String, Object> interfaceProvider;

    public ServiceProvider(){
        this.interfaceProvider = new HashMap<>();
    }

    public void provideServiceInterface(Object service){
        // 根据多态，这里 service 一般是一个具体实现类
        // 因此 serviceName 是 xxxServiceImpl
        // 我们需要获取其实现的接口，并进行接口与对应实现的注册
        String serviceName = service.getClass().getName();
        Class<?>[] interfaces = service.getClass().getInterfaces();

        for(Class clazz : interfaces){
            interfaceProvider.put(clazz.getName(),service);
        }

    }

    public Object getService(String interfaceName){
        return interfaceProvider.get(interfaceName);
    }
}
```

<font style="color:rgb(31, 35, 40);">ok，接下来解决耦合问题</font>

<font style="color:rgb(31, 35, 40);">在前面中，我们在服务端做的工作有：BIO监听、处理方式（接受请求、反射调用、返回结果）</font>

<font style="color:rgb(31, 35, 40);">现在，我们服务端不止解决一个服务的监听，我们想改造成一个更加通用的服务端。并且，后续改造中，我们可能也不想用 BIO 进行监听，可能也不想只用一个线程来进行反射调用（例如，利用线程池操作）等</font>

<font style="color:rgb(31, 35, 40);">所以，将上述功能抽象出来：</font>

+ 服务端启动/停止
+ 处理方式：简单处理、线程池处理... (这里利用服务端的具体实现来体现)

因此，先抽象出一个服务端接口，接口提供服务端启动和停止的方法

```java
public interface IRPCServer {
    void start(int port);

    void stop();
}
```

接下来，特定的RPCServer我们将进行具体实现

```java

public class SimpleRPCServer implements IRPCServer {
    // 存着服务接口名-> service对象的map
    private ServiceProvider serviceProvider;

    public SimpleRPCServer(ServiceProvider serviceProvide) {
        this.serviceProvider = serviceProvide;
    }

    @Override
    public void start(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("服务端启动了");
            // BIO的方式监听Socket
            while (true) {
                Socket socket = serverSocket.accept();
                // 开启一个新线程去处理
                new Thread(new WorkThread(socket, serviceProvider)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("服务器启动失败");
        }
    }

    @Override
    public void stop() {

    }
}
```

也可以是带有线程池的实现

```java

public class ThreadPoolRPCServer implements IRPCServer {
    private final ThreadPoolExecutor threadPool;
    private ServiceProvider serviceProvide;

    public ThreadPoolRPCServer(ServiceProvider serviceProvide) {
        threadPool = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
                1000, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));
        this.serviceProvide = serviceProvide;
    }

    public ThreadPoolRPCServer(ServiceProvider serviceProvide, int corePoolSize,
                               int maximumPoolSize,
                               long keepAliveTime,
                               TimeUnit unit,
                               BlockingQueue<Runnable> workQueue) {

        threadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        this.serviceProvide = serviceProvide;
    }


    @Override
    public void start(int port) {
        System.out.println("服务端启动了");
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept();
                threadPool.execute(new WorkThread(socket, serviceProvide));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
    }
}
```

注意到，服务端里面涉及对请求的反射调用，我们也将这部分逻辑抽离出来。

```java

/**
 * 这里负责解析得到的request请求，执行服务方法，返回给客户端
 * 1. 从request得到interfaceName 2. 根据interfaceName在serviceProvide Map中获取服务端的实现类
 * 3. 从request中得到方法名，参数， 利用反射执行服务中的方法 4. 得到结果，封装成response，写入socket
 */
@AllArgsConstructor
public class WorkThread implements Runnable {
    private Socket socket;
    private ServiceProvider serviceProvide;

    @Override
    public void run() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            // 读取客户端传过来的request
            RPCRequest request = (RPCRequest) ois.readObject();
            // 反射调用服务方法获得返回值
            RPCResponse response = getResponse(request);
            //写入到客户端
            oos.writeObject(response);
            oos.flush();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("从IO中读取数据错误");
        }
    }

    private RPCResponse getResponse(RPCRequest request) {
        // 得到服务名
        String interfaceName = request.getInterfaceName();
        // 得到服务端相应服务实现类
        Object service = serviceProvide.getService(interfaceName);
        // 反射调用方法
        Method method = null;
        try {
            method = service.getClass().getMethod(request.getMethodName(), request.getParamsTypes());
            Object invoke = method.invoke(service, request.getParams());
            return RPCResponse.success(invoke);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            System.out.println("方法执行错误");
            return RPCResponse.fail();
        }
    }
}
```

至此，我们完成了对服务端松耦合处理。并且利用开放原则（IRPCServer 接口）实现了服务端的可拓展

同时，遵循单一职责原则，把服务端的反射处理抽离出来

下面我们进行测试 (这里把类名称改为 RPCServerMain )

```java
/**
 * version 3: 降低耦合度,引入服务提供者
 */
@Slf4j
public class RPCServerMain {
    public static void main(String[] args) {
        IUserService userService = new UserServiceImpl();
        IBlogService blogService = new BlogServiceImpl();

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.provideServiceInterface(userService);
        serviceProvider.provideServiceInterface(blogService);

        IRPCServer rpcServer = new ThreadPoolRPCServer(serviceProvider);
        rpcServer.start(8899);
    }
}
```

### 总结

本届中，为了支持多服务，我们实现了 ServiceProvider 服务提供者。本质是利用Map将服务接口与具体服务实现类绑定起来。在服务端处理阶段，能过通过接口名称获取到具体服务类。通过反射调用服务类的方法。

总体实现了

+ 支持多服务
+ 服务端松耦合处理

但是服务端中仍然是采用 `serverSocket.accept();`阻塞式响应。必须有客户端连接了才能获得响应。在没有客户端连接的时候，服务端一致处于阻塞状态。



## 引入 Netty 支持 NIO

BIO 阻塞住了服务端，那为什么要引入 Netty 呢？

<font style="color:rgb(64, 64, 64);">Netty 是一个 </font>**<font style="color:rgb(64, 64, 64);">异步事件驱动</font>**<font style="color:rgb(64, 64, 64);"> 的高性能网络框架，基于 </font>**<font style="color:rgb(64, 64, 64);">NIO（Non-blocking I/O）</font>**<font style="color:rgb(64, 64, 64);">，适用于高并发、低延迟的网络通信（如 RPC、WebSocket、HTTP 等）</font>

<font style="color:rgb(64, 64, 64);">相比于 NIO 复杂的API，Netty 使用更为方便</font>



首先，我们先对 Client 进行抽象，Client 的共有方法是 发送请求 sendRequest，因此可以抽象出如下接口

```java
public interface IRPCClient {
    RPCResponse sendRequest(RPCRequest request);
}
```

紧接着，我们将原本的客户端改造一下（实现该接口）

```java

// SimpleRPCClient实现这个接口，不同的网络方式有着不同的实现
@AllArgsConstructor
public class SimpleRPCClient implements IRPCClient {
    private String host;
    private int port;

    // 客户端发起一次请求调用，Socket建立连接，发起请求Request，得到响应Response
    // 这里的request是封装好的，不同的service需要进行不同的封装， 客户端只知道Service接口，需要一层动态代理根据反射封装不同的Service
    public RPCResponse sendRequest(RPCRequest request) {
        try {
            // 发起一次Socket连接请求
            Socket socket = new Socket(host, port);

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            System.out.println(request);
            objectOutputStream.writeObject(request);
            objectOutputStream.flush();

            RPCResponse response = (RPCResponse) objectInputStream.readObject();

            //System.out.println(response.getData());
            return response;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println();
            return null;
        }
    }
}
```

由于 Client 接受了 host 和 port ，我们的代理类也要改变一下

```java

public class RPCClientProxy implements InvocationHandler {
    IRPCClient rpcClient;

    public RPCClientProxy(IRPCClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 代理对象执行每个方法时，都将执行这里的逻辑
        // 我们的目的是，利用这个代理类帮助我构建请求。这样能够有效减少重复的代码
        RPCRequest request = RPCRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .paramsTypes(method.getParameterTypes())
                .params(args)
                .build();
        // 然后将这个请求发送到服务端，并获取响应
        RPCResponse response = rpcClient.sendRequest(request);
        return response.getData();
    }

    // 获取代理对象
    public <T> T getProxy(Class<T> clazz) {
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        return (T) o;
    }
}

```



OK，接下来正式引入 Netty

```xml
<!-- https://mvnrepository.com/artifact/io.netty/netty-all -->
<dependency>
  <groupId>io.netty</groupId>
  <artifactId>netty-all</artifactId>
  <version>4.1.119.Final</version>
</dependency>
```

接下来先简单了解一下 Netty 的工作模式，再对我们的 Server 和 Client 进行拓展



下面通过一张图来简单介绍一下 Netty 的使用

![](https://cdn.nlark.com/yuque/0/2025/png/50582501/1743164383609-be9d3f27-e3b1-4c27-a9f9-ce276a1e7bfe.png)

显而易见，服务器和客户端是通过 channel 进行通信的。

因此双方通信时都会根据 host, port 连接到相同的 channel



可以看到客户端和服务端在 channel 初始化时，都得经过一些 pipelines，这些 pipelines 通常包括指定消息格式，指定序列化方式，指定**处理方式。**这个处理方式就是我们需要重点关注的地方。

可以自定义一个 `MyHandler extends SimpleChannelInboundHandler<RPCResponse>`然后重写里面的 `channelRead0`方法，来实现接收消息的处理逻辑。

我们在服务端的自定义 handler 中，处理 request 请求，并向 channel 发送一个 response

我们在客户端的自定义 handler 中，构建 request 请求，并从 channel 接受 response



OK，接下来开始写服务端和客户端的 Netty 实现。

**服务端：**

```java

/**
 * 实现RPCServer接口，负责监听与发送数据
 */
@AllArgsConstructor
public class NettyRPCServer implements IRPCServer {
    private ServiceProvider serviceProvider;
    @Override
    public void start(int port) {
        // netty 服务线程组boss负责建立连接， work负责具体的请求
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        System.out.printf("Netty服务端启动了...");
        try {
            // 启动netty服务器
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            // 初始化
            serverBootstrap
                    .group(bossGroup,workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new NettyServerInitializer(serviceProvider));
            // 同步阻塞
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            // 死循环监听
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    @Override
    public void stop() {
    }
}
```

```java
/**
 * 初始化，主要负责序列化的编码解码， 需要解决netty的粘包问题
 */
@AllArgsConstructor
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    private ServiceProvider serviceProvider;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // 消息格式 [长度][消息体], 解决粘包问题
        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
        // 计算当前待发送消息的长度，写入到前4个字节中
        pipeline.addLast(new LengthFieldPrepender(4));

        // 这里使用的还是java 序列化方式， netty的自带的解码编码支持传输这种结构
        pipeline.addLast(new ObjectEncoder());
        pipeline.addLast(new ObjectDecoder(new ClassResolver() {
            @Override
            public Class<?> resolve(String className) throws ClassNotFoundException {
                return Class.forName(className);
            }
        }));
        System.out.println("initChannel");
        pipeline.addLast(new NettyRPCServerHandler(serviceProvider));
    }
}
```

```java

/**
 * 因为是服务器端，我们知道接受到请求格式是RPCRequest
 * Object类型也行，强制转型就行
 */
@AllArgsConstructor
public class NettyRPCServerHandler extends SimpleChannelInboundHandler<RPCRequest> {
    private ServiceProvider serviceProvider;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RPCRequest msg) throws Exception {
        RPCResponse response = getResponse(msg);
        ctx.writeAndFlush(response);
        ctx.close();
        System.out.println("channel close");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        System.out.println("channel close");
    }

    RPCResponse getResponse(RPCRequest request) {
        // 得到服务名
        String interfaceName = request.getInterfaceName();
        // 得到服务端相应服务实现类
        Object service = serviceProvider.getService(interfaceName);
        // 反射调用方法
        Method method = null;
        try {
            method = service.getClass().getMethod(request.getMethodName(), request.getParamsTypes());
            Object invoke = method.invoke(service, request.getParams());
            return RPCResponse.success(invoke);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            System.out.println("方法执行错误");
            return RPCResponse.fail();
        }
    }
}
```

**客户端**

```java

/**
 * 实现RPCClient接口
 */
public class NettyRPCClient implements IRPCClient {
    private static final Bootstrap bootstrap;
    private static final EventLoopGroup eventLoopGroup;
    private String host;
    private int port;


    private static final AttributeKey<RPCResponse> RESPONSE_KEY =
            AttributeKey.valueOf("RPCResponse");

    public NettyRPCClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    // netty客户端初始化，重复使用
    static {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .handler(new NettyClientInitializer());
    }

    /**
     * 这里需要操作一下，因为netty的传输都是异步的，你发送request，会立刻返回， 而不是想要的相应的response
     */
    @Override
    public RPCResponse sendRequest(RPCRequest request) {
        try {
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            Channel channel = channelFuture.channel();
            channel.attr(RESPONSE_KEY); // 我们用 future 来接受
            // 发送数据
            channel.writeAndFlush(request);
            // closeFuture: channel关闭的Future
            // sync 表示阻塞等待 它 关闭
            channel.closeFuture().sync();
            // 阻塞的获得结果，通过给channel设计别名，获取特定名字下的channel中的内容（这个在hanlder中设置）
            // AttributeKey是，线程隔离的，不会由线程安全问题。
            // 实际上不应通过阻塞，可通过回调函数
            AttributeKey<RPCResponse> key = AttributeKey.valueOf("RPCResponse");
            RPCResponse rpcResponse = channel.attr(key).get();
            return rpcResponse;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
```

```java

/**
 * 通过 handler 获取客户端的结果
 */
public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // 消息格式 [长度][消息体], 解决粘包问题
        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
        // 计算当前待发送消息的长度，写入到前4个字节中
        pipeline.addLast(new LengthFieldPrepender(4));

        // 这里使用的还是java 序列化方式， netty的自带的解码编码支持传输这种结构
        pipeline.addLast(new ObjectEncoder());
        pipeline.addLast(new ObjectDecoder(new ClassResolver() {
            @Override
            public Class<?> resolve(String className) throws ClassNotFoundException {
                return Class.forName(className);
            }
        }));

        pipeline.addLast(new NettyClientHandler());
    }
}
```

```java
public class NettyClientHandler extends SimpleChannelInboundHandler<RPCResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RPCResponse msg) throws Exception {
        // 接收到response, 给channel设计别名，让sendRequest里读取response
        AttributeKey<RPCResponse> key = AttributeKey.valueOf("RPCResponse");
        ctx.channel().attr(key).set(msg);
        ctx.channel().close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
```



测试

```java
/**
 * version 3: 引入 Netty
 */
public class RPCServerMain {
    public static void main(String[] args) {
        IUserService userService = new UserServiceImpl();
        IBlogService blogService = new BlogServiceImpl();

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.provideServiceInterface(userService);
        serviceProvider.provideServiceInterface(blogService);

        IRPCServer rpcServer = new NettyRPCServer(serviceProvider);
        rpcServer.start(8899);
    }
}
```

```java
public class RPCClientMain {
    public static void main(String[] args) {
        // 构建一个使用java Socket传输的客户端
        IRPCClient rpcClient = new NettyRPCClient("127.0.0.1", 8899);
        // 把这个客户端传入代理客户端
        RPCClientProxy rpcClientProxy = new RPCClientProxy(rpcClient);
        // 代理客户端根据不同的服务，获得一个代理类， 并且这个代理类的方法以或者增强（封装数据，发送请求）
        IUserService userService = rpcClientProxy.getProxy(IUserService.class);
        // 调用方法
        User user = userService.getUserById(10L);
        System.out.println(user);

        IBlogService blogService = rpcClientProxy.getProxy(IBlogService.class);
        Blog blog = blogService.getBlogById(234);
        System.out.println(blog);
    }
}
```

### 总结

本小节中，我们对客户端也做了拓展

并引入 Netty 来解决



存在问题：

这里使用的仍然是 java 自带的序列化机制

+ <font style="color:rgb(64, 64, 64);">序列化后的二进制流体积大，编解码速度慢，CPU 和内存开销高。</font>
+ <font style="color:rgb(64, 64, 64);">类结构变更（如增删字段）会导致反序列化失败，</font>`<font style="color:rgb(64, 64, 64);">serialVersionUID</font>`<font style="color:rgb(64, 64, 64);"> 管理麻烦</font>
+ <font style="color:rgb(64, 64, 64);">。。。</font>

## 自定义序列化机制

注：

<font style="color:#DF2A3F;">fastjson 已经不支持自定义类型的反序列化</font>



上节中，我们在 Netty 使用的是 java 自带的序列化机制，存在体积大，反序列化可能失败的问题。

因此，这节我们将引入更多的序列化机制



为了能够正确的读取出 字节 中的内容，我们定义了如下消息的格式：

| <font style="color:rgb(31, 35, 40);">消息类型（2Byte）</font> | <font style="color:rgb(31, 35, 40);">序列化方式 2Byte</font> | <font style="color:rgb(31, 35, 40);">消息长度 4Byte</font> |
| ------------------------------------------------------- | ------------------------------------------------------- | ------------------------------------------------------ |
| <font style="color:rgb(31, 35, 40);">序列化后的Data….</font> | <font style="color:rgb(31, 35, 40);">序列化后的Data…</font>  | <font style="color:rgb(31, 35, 40);">序列化后的Data…</font> |

+ 消息类型：RPCRequest, RPCResponse
+ 序列化方式：JDK序列化，Json 序列化...
+ 后续数据的字节长度



Netty 是支持我们设定编码和解码方式的

为了支持更多种的编码/解码方式，我们定义了如下接口

```java
public interface ISerializer {
    // 把对象序列化成字节数组
    byte[] serialize(Object obj) throws JsonProcessingException;

    // 从字节数组反序列化成消息, 使用java自带序列化方式不用messageType也能得到相应的对象（序列化字节数组里包含类信息）
    // 其它方式需指定消息格式，再根据message转化成相应的对象
    Object deserialize(byte[] bytes, int messageType);

    // 返回使用的序列器，是哪个
    // 0：java自带序列化方式, 1: json序列化方式
    int getType();

    // 根据序号取出序列化器，暂时有两种实现方式，需要其它方式，实现这个接口即可
    static ISerializer getSerializerByCode(int code){
        switch (code){
            case 0:
                return new ObjectSerializer();
            case 1:
                return new JsonSerializer();
            default:
                return null;
        }
    }
}
```

下面我们支持两种方式：JDK序列化，json序列化

**JDK序列化**

```java
public class ObjectSerializer implements ISerializer{

    // 利用java IO 对象 -> 字节数组
    @Override
    public byte[] serialize(Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
            oos.close();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bytes;
    }

    // 字节数组 -> 对象
    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        Object obj = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream ois = new ObjectInputStream(bis);
            obj = ois.readObject();
            ois.close();
            bis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return obj;
    }

    // 0 代表java原生序列化器
    @Override
    public int getType() {
        return 0;
    }
}
```

**JSON 序列化**

⚠：fastjson 已经不支持自定义类的反序列化



**编码器**

```java
@AllArgsConstructor
public class MyEncode extends MessageToByteEncoder {
    private ISerializer serializer;

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        System.out.println(msg.getClass());
        // 写入消息类型
        if(msg instanceof RPCRequest){
            out.writeShort(MessageType.REQUEST.getCode());
        }
        else if(msg instanceof RPCResponse){
            out.writeShort(MessageType.RESPONSE.getCode());
        }
        // 写入序列化方式
        out.writeShort(serializer.getType());
        // 得到序列化数组
        byte[] serialize = serializer.serialize(msg);
        // 写入长度
        out.writeInt(serialize.length);
        // 写入序列化字节数组
        out.writeBytes(serialize);
    }
}
```

```java
@AllArgsConstructor
public class MyDecode extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 1. 读取消息类型
        short messageType = in.readShort();
        // 现在还只支持request与response请求
        if (messageType != MessageType.REQUEST.getCode() &&
                messageType != MessageType.RESPONSE.getCode()) {
            System.out.println("暂不支持此种数据");
            return;
        }
        // 2. 读取序列化的类型
        short serializerType = in.readShort();
        // 根据类型得到相应的序列化器
        ISerializer serializer = ISerializer.getSerializerByCode(serializerType);
        if (serializer == null) throw new RuntimeException("不存在对应的序列化器");
        // 3. 读取数据序列化后的字节长度
        int length = in.readInt();
        // 4. 读取序列化数组
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        // 用对应的序列化器解码字节数组
        Object deserialize = serializer.deserialize(bytes, messageType);
        out.add(deserialize);
    }
}
```

最后在 ChannelInitializer 使用

```java
@AllArgsConstructor
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    private ServiceProvider serviceProvider;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

//        // 消息格式 [长度][消息体], 解决粘包问题
//        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
//        // 计算当前待发送消息的长度，写入到前4个字节中
//        pipeline.addLast(new LengthFieldPrepender(4));
//
//        // 这里使用的还是java 序列化方式， netty的自带的解码编码支持传输这种结构
//        pipeline.addLast(new ObjectEncoder());
//        pipeline.addLast(new ObjectDecoder(new ClassResolver() {
//            @Override
//            public Class<?> resolve(String className) throws ClassNotFoundException {
//                return Class.forName(className);
//            }
//        }));

        // 使用自定义的编解码器
        pipeline.addLast(new MyDecode());
        // 编码需要传入序列化器，这里是json，还支持ObjectSerializer，也可以自己实现其他的
        pipeline.addLast(new MyEncode(new ObjectSerializer()));

        pipeline.addLast(new NettyRPCServerHandler(serviceProvider));
    }
}
```

### 总结

本节中，使我们的rpc支持多种消息类型。

存在问题：

+ <font style="color:rgb(31, 35, 40);">服务端与客户端通信的host与port预先就必须知道的，每一个客户端都必须知道对应服务的ip与端口号， 并且如果服务挂了或者换地址了，就很麻烦。扩展性也不强</font>

<font style="color:rgb(31, 35, 40);"></font>

## <font style="color:rgb(31, 35, 40);">引入 Zookeeper 注册中心</font>

客户端与服务端通信，每次都要管理之间的 host 和 port。

能不能服务端把提供的服务摆上台面，客户端直接给出需要的服务，服务端直接给你 host 和 port。

OK，这里引入 zookeeper 来实现上述功能。

zookeeper 相当于一个中介，房产中介。这个中介手上有很多不同位置（物理位置不同），大小不同（服务能力不同）的房子。客户过来说，我想在洛杉矶买套100平米的房子。

下面我们把 zookeeper 部署到 docker 上

**docker 部署**

```java
 docker run -d -e TZ="Asia/Shanghai" -p 2181:2181 -v ./data:/data --name jl-zk --restart always zookeeper
```

**引入 pom**

```java
<!-- https://mvnrepository.com/artifact/org.apache.curator/curator-recipes -->
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-recipes</artifactId>
    <version>5.8.0</version>
</dependency>
```



有了这个中介，我们得先让这个中介知道，”我手上有哪些房子“。

因此，我们得先对这些服务向注册中心注册，让它知道这个服务是需要被提供的（向中介报告，让它知道这个房子要出售）

同时，客户端一般会提供服务的名称（可以是接口名称），然后中介需要根据这个名称给出服务。（客户只给出房子的大概描述，然后中介给出一套房子）

ok，上述过程涉及了服务，注册中心，客户之间两方面的功能：

+ 服务注册：服务端向注册中心注册可以被发现的服务
+ 服务发现：客户端根据服务名称可以从注册中心得到一个服务

因此，我们定义如下接口描述上述过程

```java
// 服务注册接口，两大基本功能，注册：保存服务与地址。 查询：根据服务名查找地址
public interface IServiceRegister {
    void register(String serviceName, InetSocketAddress serverAddress);
    InetSocketAddress serviceDiscovery(String serviceName);
}
```

注册中心还得初始化，同时支持上述两个功能

```java

public class ZkServiceRegister implements IServiceRegister {
    // curator 提供的zookeeper客户端
    private CuratorFramework client;
    // zookeeper根路径节点
    private static final String ROOT_PATH = "MyRPC";

    // 这里负责zookeeper客户端的初始化，并与zookeeper服务端建立连接
    public ZkServiceRegister(){
        // 指数时间重试
        RetryPolicy policy = new ExponentialBackoffRetry(1000, 3);
        // zookeeper的地址固定，不管是服务提供者还是，消费者都要与之建立连接
        // sessionTimeoutMs 与 zoo.cfg中的tickTime 有关系，
        // zk还会根据minSessionTimeout与maxSessionTimeout两个参数重新调整最后的超时值。默认分别为tickTime 的2倍和20倍
        // 使用心跳监听状态
        this.client = CuratorFrameworkFactory.builder().connectString("192.168.160.128:2181")
                .sessionTimeoutMs(40000).retryPolicy(policy).namespace(ROOT_PATH).build();
        this.client.start();
        System.out.println("zookeeper 连接成功");
    }

    @Override
    public void register(String serviceName, InetSocketAddress serverAddress){
        try {
            // serviceName创建成永久节点，服务提供者下线时，不删服务名，只删地址
            if(client.checkExists().forPath("/" + serviceName) == null){
               client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/" + serviceName);
            }
            // 路径地址，一个/代表一个节点
            String path = "/" + serviceName +"/"+ getServiceAddress(serverAddress);
            // 临时节点，服务器下线就删除节点
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
        } catch (Exception e) {
            System.out.println("此服务已存在");
        }
    }
    // 根据服务名返回地址
    @Override
    public InetSocketAddress serviceDiscovery(String serviceName) {
        try {
            List<String> strings = client.getChildren().forPath("/" + serviceName);
            // 这里默认用的第一个，后面加负载均衡
            String string = strings.get(0);
            return parseAddress(string);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 地址 -> XXX.XXX.XXX.XXX:port 字符串
    private String getServiceAddress(InetSocketAddress serverAddress) {
        return serverAddress.getHostName() +
                ":" +
                serverAddress.getPort();
    }
    // 字符串解析为地址
    private InetSocketAddress parseAddress(String address) {
        String[] result = address.split(":");
        return new InetSocketAddress(result[0], Integer.parseInt(result[1]));
    }
}
```

OK，有了这个，我们得改造以下之前的服务提供者了。之前是采用 Map 的形式作为提供者，现在用 zookeeper 替代

```java

public class ServiceProvider {
    /**
     * 一个实现类可能实现多个服务接口，
     */
    private Map<String, Object> interfaceProvider;

    private IServiceRegister serviceRegister;
    private String host;
    private int port;

    public ServiceProvider(String host, int port) {
        // 需要传入服务端自身的服务的网络地址
        this.host = host;
        this.port = port;
        this.interfaceProvider = new HashMap<>();
        this.serviceRegister = new ZkServiceRegister();
    }

    public void provideServiceInterface(Object service) {
        Class<?>[] interfaces = service.getClass().getInterfaces();

        for (Class clazz : interfaces) {
            // 本机的映射表
            interfaceProvider.put(clazz.getName(), service);
            // 在注册中心注册服务
            serviceRegister.register(clazz.getName(), new InetSocketAddress(host, port));
        }

    }

    public Object getService(String interfaceName) {
        return interfaceProvider.get(interfaceName);
    }
}
```

OK，后面就是改造服务端和客户端的执行逻辑

每个服务端都先注册一下

```java
public class RPCServerMain {
    public static void main(String[] args) {
        IUserService userService = new UserServiceImpl();
        IBlogService blogService = new BlogServiceImpl();

        ServiceProvider serviceProvider = new ServiceProvider("127.0.0.1", 8899);
        serviceProvider.provideServiceInterface(userService);
        serviceProvider.provideServiceInterface(blogService);

        IRPCServer rpcServer = new NettyRPCServer(serviceProvider);
        rpcServer.start(8899);
    }
}
```

```java
public class RPCClientMain {
    public static void main(String[] args) {
        // 构建一个使用java Socket/ netty/....传输的客户端
        IRPCClient rpcClient = new NettyRPCClient();
        // 把这个客户端传入代理客户端
        RPCClientProxy rpcClientProxy = new RPCClientProxy(rpcClient);
        // 代理客户端根据不同的服务，获得一个代理类， 并且这个代理类的方法以或者增强（封装数据，发送请求）
        IUserService userService = rpcClientProxy.getProxy(IUserService.class);
        User userByUserId = userService.getUserById(10L);
        System.out.println("从服务端得到的user为：" + userByUserId);

        IBlogService blogService = rpcClientProxy.getProxy(IBlogService.class);
        Blog blogById = blogService.getBlogById(10000);
        System.out.println("从服务端得到的blog为：" + blogById);
    }
}
```

### 总结

这里利用注册中心，在客户端与服务端之间建立一个桥梁。

客户端不关心服务端的具体 host:port ，只关注它需要哪些服务。

注册中心就可以根据服务名向客户端提供服务。

这个前提是，服务端需要先向注册中心注册



存在问题：

+ 负载均衡。



## 负载均衡

zookeeper 根据服务名称，会给出一些实例列表，负载均衡机制，在这里处理就好了

```java
public interface ILoadBalance {
    String balance(List<String> addressList);
}
```



### 总结

只实现了简单的轮询、随机



存在问题：

+ <font style="color:rgb(31, 35, 40);">客户端每次发起请求都要先与 zookeeper 进行通信得到地址，效率低下。—— 客户端缓存</font>
+ <font style="color:rgb(31, 35, 40);">必须有专门的服务器存放服务，每台服务器上都要开启 NettyServer</font>





# <font style="color:rgb(31, 35, 40);">注解驱动</font>

项目地址：

在该项目里，虽然实现了一个 RPC 过程。那能把这个直接用到别的项目上吗？是否能够易用，且无侵入的放到别的项目上呢？

一个成熟的 RPC 框架他是怎么使用的呢？

下面我们通过 Dubbo 的使用，来了解我们需要再拓展哪些地方。

## Dubbo 简单使用

参考：

[https://juejin.cn/post/7260697121510277157](https://juejin.cn/post/7260697121510277157)



参考 version 6 在 docker 上配置好 zookeeper。

**pom 引入**

```java
        <!-- https://mvnrepository.com/artifact/org.apache.dubbo/dubbo-spring-boot-starter -->
        <dependency>
            <groupId>org.apache.dubbo</groupId>
            <artifactId>dubbo-spring-boot-starter</artifactId>
            <version>3.3.4</version>
        </dependency>

        <!-- Zookeeper 注册中心支持 -->
        <dependency>
            <groupId>org.apache.dubbo</groupId>
            <artifactId>dubbo-registry-zookeeper</artifactId>
            <version>3.3.4</version>
        </dependency>

        <!-- Curator 5.x + ZooKeeper 3.8.x（必须匹配） -->
        <dependency>
            <groupId>org.apache.curator</groupId>
            <artifactId>curator-framework</artifactId>
            <version>5.8.0</version>
        </dependency>
        <dependency>
            <groupId>org.apache.curator</groupId>
            <artifactId>curator-recipes</artifactId>
            <version>5.8.0</version>
        </dependency>
        <dependency>
            <groupId>org.apache.zookeeper</groupId>
            <artifactId>zookeeper</artifactId>
            <version>3.8.4</version>
        </dependency>
```

p.s. `dubbo-zookeeper-dependencies` 存在版本不匹配问题



**yaml 配置**

提供者和消费者都要配置

```java
dubbo:
  application:
    name: provider-app
  registry:
    address: zookeeper://192.168.160.128:2181
```

**定义通用接口**

一般会将远程调用的接口放在公共模块里

```java
package com.bobby.common.service;

import com.bobby.common.utils.Result;

public interface IDubboDemoService {
    public Result getDemo();
}
```



**远程服务定义实现类**

```java
// 使用这个注解，可以将该接口实现注册到注册中心
@DubboService
@Service
public class DubboDemoServiceImpl implements IDubboDemoService {
    @Override
    public Result getDemo() {
        return Result.ok("Hi, it's dubbo remote service. ");
    }
}

```

`@DubboService`用于注册服务

注意，SpringApplication 也要加上注解

```java
@EnableDubbo
@MapperScan("com.bobby.bizb.mapper")
@SpringBootApplication
public class BizBApplication {
    public static void main(String[] args) {
        SpringApplication.run(BizBApplication.class, args);
    }
}

```

`@EnableDubbo`用于开启 Dubbo

**测试**

```java
@Slf4j
@ActiveProfiles("local")
@SpringBootTest
public class DubboDemoTest {
    // 使用 DubboReference 引入服务
    // 这其实就是一个发现服务的过程
    @DubboReference
    IDubboDemoService demoService;


    @Test
    public void remoteCall() {
        log.info("remoteCall: "+ demoService.getDemo());
    }
}
```

`@DubboReference`用于在消费者端发现服务。

---

通过上述的使用，我们发现，Dubbo 其实做了很好的封装。通过注解来注册和发现服务。

通过 spring 配置确定 zookeeper 的地址

因此，为了使 MyRPC 更加易用，我们确立了如下改进目标

+ 注解支持
+ 配置支持
+ 独立模块

---

## 正式改造

为了使该项目能够以一个模块的形式插入到已有项目中，我们对原本的测试方式进行了改造 —— 引入springboot 服务。

在下面的改造，我们会改动一点目录结构（土拨鼠尖叫）

因此，在这里，我们将 rpc 的核心部分提取出来放在 `Bobby-RPC-Core`模块中。为了模拟实际的业务，我们创建了两个 springboot 应用，分别为 `Bobby-RPC-Provider` 和 `Bobby-RPC-Consumer`。改造完成后，我们将利用这两个模块来测试我们的 RPC 框架。目录结构如下：

![](https://cdn.nlark.com/yuque/0/2025/png/50582501/1743398837022-edf0d9d7-86d7-414c-9d51-dbc988832614.png)

下面，我们将在 `Bobby-RPC-Core`里面完成改造

## Spring 配置

利用 yaml 来配置 zookeeper 的地址，然后我们的 ZkServiceRegister 只需要获取里面特定的属性就好。

这里就是配置Spring属性而已

模仿 Dubbo 可以有如下属性

```java
@Data
@Builder
@ConfigurationProperties(prefix = "brpc.zk")
public class ZkProperties {
    private String address;  // 直接映射 myrpc.zk.address
    private int sessionTimeoutMs;  // 自动绑定 session-timeout-ms
    private String namespace;
    private Retry retry;    // 嵌套对象

    @Data
    @Builder
    public static class Retry {
        private int maxRetries;      // 绑定 max-retries
        private int baseSleepTimeMs; // 绑定 base-sleep-time-ms
    }
}

```

```java
@Data
@ConfigurationProperties(prefix = "brpc")
public class BRpcProperties {
    String applicationName;
}
```

```plain
@Data
@ConfigurationProperties(prefix = "brpc.netty")
public class NettyProperties {
    int port;
}
```



那么一个 yaml 里面的实例，可以是如下这个样子

```java
brpc:
  application-name: 'bobby-app'
  zk:
    address: 192.168.160.128:2181
    session-timeout-ms: 30000  # 必须使用中划线
    retry:
      base-sleep-time-ms: 1000 # 嵌套属性同样规则
      max-retries: 3
  netty:
    port: 8089
```

既然引入了配置，我们的一些相关参数，如服务地址，服务端口，就可以从配置类中获取了。

在此之前，我们之前的版本有哪些地方涉及 port 呢？

+ IRpcServer
+ ServerProvider

ok，因此，在下面的修改中，我们会用从配置类读取的参数来创建服务。



## 改造 ServerProvider

接下来改造 ZkServiceRegister 使它从配置类中读取。

+ 我们将一些配置抽取出来，用 BRpcProperties 替代
+ 将 zk 客户端的初始化抽离出来，采用依赖注入的方式构造 ServiceRegister
+ 我们用 ZkProperties 构造 zk 客户端

```java
// 服务注册接口，两大基本功能，注册：保存服务与地址。 查询：根据服务名查找地址
public interface IServiceRegister {
    void register(String serviceName, InetSocketAddress serverAddress);
    InetSocketAddress serviceDiscovery(String serviceName);
}
```

```java

@Slf4j
//@Component
public class ZkServiceRegister implements IServiceRegister {

    private final BRpcProperties rpcProperties;
    private final ILoadBalance loadBalance;
    private final CuratorFramework client;
    private final Map<String, List<String>> serviceMap = new ConcurrentHashMap<>();
    private CuratorCache curatorCache;

    public ZkServiceRegister(BRpcProperties rpcProperties, ILoadBalance loadBalance, CuratorFramework client) {
        this.rpcProperties = rpcProperties;
        this.loadBalance = loadBalance;
        this.client = client;

        startClient();
    }

    private void startClient() {
        client.start();
        try {
            // 等待连接建立
            client.blockUntilConnected();
            log.info("Zookeeper连接成功，地址: {}", client.getZookeeperClient().getCurrentConnectionString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Zookeeper连接被中断", e);
            throw new RuntimeException("Failed to connect to Zookeeper", e);
        } catch (Exception e) {
            log.error("Zookeeper连接失败", e);
            throw new RuntimeException("Failed to connect to Zookeeper", e);
        }
    }

    private String getServicePath(String serviceName) {
        return String.format("/%s/%s", rpcProperties.getApplicationName(), serviceName);
    }

    private String getInstancePath(String serviceName, String addressName) {
        return String.format("/%s/%s/%s", rpcProperties.getApplicationName(), serviceName, addressName);
    }


    @Override
    public void register(String serviceName, InetSocketAddress serverAddress) {
        if (serviceName == null || serverAddress == null) {
            throw new IllegalArgumentException("Service name and server address cannot be null");
        }
        String servicePath = getServicePath(serviceName);

        try {
            // 1. 创建持久化父节点（幂等操作） -- 一般是服务的分类，例如一个服务名
            if (client.checkExists().forPath(servicePath) == null) {
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(servicePath);
            }

            // 2. 注册临时节点（允许重复创建，实际会覆盖）-- 一般是具体的实例，服务名下，不同的实例
//            String addressPath = servicePath + "/" + getServiceAddress(serverAddress);
            String addressPath = getInstancePath(serviceName, getServiceAddress(serverAddress));
            try {
                client.create()
                        .withMode(CreateMode.EPHEMERAL)
                        .forPath(addressPath);
                log.info("服务实例注册成功: {} -> {}", servicePath, serverAddress);
            } catch (Exception e) {
                // 节点已存在说明该实例正常在线，记录调试日志即可
                log.debug("服务实例已存在（正常心跳）: {}", addressPath);
            }
        } catch (Exception e) {
            log.error("服务注册失败: {}", servicePath, e);
            throw new RuntimeException("Failed to register service: " + servicePath, e);
        }

        if (rpcProperties.getWatch() != null && rpcProperties.getWatch()) {
            log.info("服务开启监控: application: {}, serviceName: {}", rpcProperties.getApplicationName(), servicePath);
            watch(serviceName);
        }
    }

    @Override
    public InetSocketAddress serviceDiscovery(String serviceName) {
        if (serviceName == null) {
            throw new IllegalArgumentException("Service name cannot be null");
        }
        String servicePath = getServicePath(serviceName);
        try {
            // 优先从缓存获取
            List<String> instances = serviceMap.get(servicePath);
            // 没有获取到缓存，则从 zk 中读取
            if (instances == null || instances.isEmpty()) {
                instances = client.getChildren().forPath(servicePath);
                // 缓存 key 是 appName + serviceName
                serviceMap.put(servicePath, instances);
            }

            if (instances.isEmpty()) {
                log.warn("未找到可用服务实例: {}", servicePath);
                return null;
            }

            String selectedInstance = loadBalance.balance(instances);
            return parseAddress(selectedInstance);
        } catch (Exception e) {
            log.error("服务发现失败: {}", servicePath, e);
            throw new RuntimeException("Failed to discover service: " + servicePath, e);
        }
    }

    private String getServiceAddress(InetSocketAddress serverAddress) {
        return serverAddress.getHostName() + ":" + serverAddress.getPort();
    }

    private InetSocketAddress parseAddress(String address) {
        String[] parts = address.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid address format: " + address);
        }
        return new InetSocketAddress(parts[0], Integer.parseInt(parts[1]));
    }

    public void watch(String serviceName) {
        if (serviceName == null) {
            throw new IllegalArgumentException("Service name cannot be null");
        }
        String servicePath = getServicePath(serviceName);
        String watchPath = servicePath;

        // 关闭旧的监听器（如果存在）
        if (this.curatorCache != null) {
            this.curatorCache.close();
        }

        // 创建新的 CuratorCache
        this.curatorCache = CuratorCache.build(client, watchPath);

        // 添加监听器
        // 分别在创建时，改变时，删除时对本地缓存进行改动
        CuratorCacheListener listener = CuratorCacheListener.builder()
                .forCreates(childData -> handleNodeCreated(childData, servicePath))
                .forChanges((oldData, newData) -> handleNodeUpdated(newData, servicePath))
                .forDeletes(childData -> handleNodeDeleted(childData, servicePath))
                .forInitialized(() -> log.info("监听器初始化完成: {}", servicePath))
                .build();

        curatorCache.listenable().addListener(listener);
        curatorCache.start();

        log.info("已创建服务监听: {}", servicePath);
    }

    // 处理节点创建事件
    private void handleNodeCreated(ChildData childData, String servicePath) {
        if (!isDirectChild(childData.getPath(), servicePath)) return;

        updateServiceCache(servicePath);
        log.debug("服务实例上线: {}", childData.getPath());
    }

    // 处理节点更新事件
    private void handleNodeUpdated(ChildData childData, String servicePath) {
        if (!isDirectChild(childData.getPath(), servicePath)) return;

        updateServiceCache(servicePath);
        log.debug("服务实例更新: {}", childData.getPath());
    }

    // 处理节点删除事件
    private void handleNodeDeleted(ChildData childData, String servicePath) {
        if (!isDirectChild(childData.getPath(), servicePath)) return;

        updateServiceCache(servicePath);
        log.debug("服务实例下线: {}", childData.getPath());
    }

    // 更新本地缓存
    private void updateServiceCache(String servicePath) {
        try {
            List<String> instances = client.getChildren().forPath(servicePath);
            serviceMap.put(servicePath, instances);
        } catch (Exception e) {
            log.error("更新服务缓存失败: {}", servicePath, e);
        }
    }

    // 判断是否为直接子节点（避免孙子节点干扰）
    public boolean isDirectChild(String fullPath, String parentPath) {
        log.info("fullPath: {}, parentPath: {}, fullPath.substring(parentPath.length()): {}", fullPath, parentPath, fullPath.substring(parentPath.length()));
        return fullPath.startsWith(parentPath) &&
                fullPath.substring(parentPath.length()).lastIndexOf('/') <= 0;
    }
}
```

```java

@Slf4j
public class ServiceProvider {
    /**
     * 一个实现类可能实现多个服务接口，
     */
    private Map<String, Object> interfaceProvider;

    private final IServiceRegister serviceRegister;
    private String host;
    private int port;

    public ServiceProvider(IServiceRegister serviceRegister) {
        this.serviceRegister = serviceRegister;
    }

    public ServiceProvider(IServiceRegister serviceRegister, String host, int port) {
        log.info("服务提供者启动 {}:{}", host, port);
        this.serviceRegister = serviceRegister;
        // 需要传入服务端自身的服务的网络地址
        this.host = host;
        this.port = port;
        this.interfaceProvider = new HashMap<>();
    }

    public void provideServiceInterface(Object service, Class<?> clazz) {
        Class<?>[] interfaces = service.getClass().getInterfaces();
//        // 一个类可能实现多个服务接口
//        for (Class<?> i : interfaces) {
//            // 本机的映射表
//            interfaceProvider.put(i.getName(), service);
//            // 在注册中心注册服务
//            serviceRegister.register(i.getName(), new InetSocketAddress(host, port));
//        }

        // 这里选择,是否需要使 impl 的所有接口都作为服务

        interfaceProvider.put(clazz.getName(), service);
        // 在注册中心注册服务
        serviceRegister.register(clazz.getName(), new InetSocketAddress(host, port));
    }

    public Object getService(String interfaceName) {
        return interfaceProvider.get(interfaceName);
    }
}
```

我们需要将 ServiceProvider 作为 bean 对象引入，因此我们创建一个配置类来创建 bean 对象

```java
@Slf4j
@Component
@EnableConfigurationProperties({ZkProperties.class, BRpcProperties.class, NettyProperties.class})
@RequiredArgsConstructor
public class ZkServiceConfig {
    private final ServerProperties serverProperties;

    @Bean
    public CuratorFramework curatorFramework(ZkProperties zkProperties) {
        log.info("初始化 ZooKeeper 客户端");
        // 使用配置中的参数
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(
                zkProperties.getRetry().getBaseSleepTimeMs(),
                zkProperties.getRetry().getMaxRetries()
        );

        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(zkProperties.getAddress())   // zk 服务地址 host:port
                .sessionTimeoutMs(zkProperties.getSessionTimeoutMs())
                .retryPolicy(retryPolicy)
                .namespace(zkProperties.getNamespace())
                .build();

        return client;
    }

    @Bean
    public ILoadBalance zkLoadBalance() {
        return new RoundLoadBalance();
    }

    @Bean
    public IServiceRegister serviceRegister(BRpcProperties rpcProperties, ILoadBalance loadBalance, CuratorFramework client) {
        return new ZkServiceRegister(rpcProperties, loadBalance, client);
    }


    @Bean
    public ServiceProvider serviceProvider(IServiceRegister serviceRegister, NettyProperties nettyProperties) {
        // 这里统一注册成 netty 的端口
        // 本机 ip + netty 端口
        return new ServiceProvider(serviceRegister, serverProperties.getAddress().getHostAddress(), nettyProperties.getPort());
    }

}
```

在这个配置类中，我们一步步创建 serviceProvider 所需要的依赖bean 对象。

由于服务端口可能会与tomcat端口冲突，所以我们这里通过配置端口，来自己指定服务类的端口。



## 改造 IRpcServer

服务端启动也是需要指定通信端口的，这里我们是通过之前引入的配置 `brpc.netty.port`，来指定端口

在这里，我们也把服务端作为 bean 对象引入

```java
@Configuration
@EnableConfigurationProperties(NettyProperties.class)
public class ServerConfig {

    @Bean
    public IRpcServer rpcServer(ServiceProvider serviceProvider, NettyProperties nettyProperties) {
        NettyRPCServer nettyRPCServer = new NettyRPCServer(serviceProvider);
//        nettyRPCServer.start(serverProperties.getPort());
        nettyRPCServer.start(nettyProperties.getPort());
        return nettyRPCServer;
    }
}
```

## 改造 IRpcClient

```java
@Configuration
public class ClientConfig {
    @Bean
    public IRpcClient rpcClient(IServiceRegister serviceRegister) {
        NettyRpcClient nettyRpcClient = new NettyRpcClient(serviceRegister);
        return nettyRpcClient;
    }
}

```

这里仍然是利用注入的方式，把之前创建的 serviceRegister 注入到客户端，用于服务发现。



## 注解驱动开发

参考：

[https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/annotation/ClassPathBeanDefinitionScanner.html](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/annotation/ClassPathBeanDefinitionScanner.html)

[https://juejin.cn/post/7173843865311379470](https://juejin.cn/post/7173843865311379470)



考虑到，Dubbo 只要加上注解，就可以实现服务注册和服务发现。

在下面我们将通过定义两个注解，来实现类似 `@DubboService` `@DubboReference`类似的功能。



### RpcService 注解

该注解用于服务注册

我们通常会将该注解用到 ServiceImpl 类上面

改造思路：在 Spring bean 注册的过程提供了 `BeanPostProcessor`这样的接口，我们可以通过该节口，在 bean 创建后做一些处理（这里用于服务注册）。因此，只需要找到具有`@RpcService`的 bean 对象，就可以实现对该服务的注册了。



下面先定义 `@RpcService`

```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RpcService {
    /**
     * 服务接口类
     * @return 接口Class对象
     */
    Class<?> interfaceClass() default void.class;
}
```



下面实现 `BeanPostProcessor` 接口

```java
@RequiredArgsConstructor
@Slf4j
@Component
@DependsOn("serviceProvider")
public class RpcServiceProcessor implements BeanPostProcessor {
    //    private final IServiceRegister serviceRegister;
    private final ServiceProvider serviceProvider;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        // 对所有 bean 试图获取 RpcService 注解
        RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
        if (rpcService != null) {
            register(bean, rpcService);
        }
        return bean;
    }

    private void register(Object bean, RpcService rpcService) {
        log.info("RpcServiceProcessor$register 正在注册服务: {}", bean.getClass().getName());
        Class<?> interfaceClass = rpcService.interfaceClass();
        // 默认使用第一个接口
        if (interfaceClass == void.class) {
            interfaceClass = bean.getClass().getInterfaces()[0];
        }
//        String serviceName = interfaceClass.getName();
        // 获取本应用的 host & port
//        serviceRegister.register(serviceName, new InetSocketAddress(serverProperties.getAddress(), nettyProperties.getPort()));
//        serviceRegister.register(serviceName, new InetSocketAddress(serverProperties.getAddress(), serverProperties.getPort()));
        serviceProvider.provideServiceInterface(bean, interfaceClass);
    }

}
```

```java

    @Bean
    public RpcServiceProcessor rpcServiceProcessor(ServiceProvider serviceProvider) {
        return new RpcServiceProcessor(serviceProvider);
    }
```

我们这里，通过发现 `@RpcService` 注解的bean，然后通过 ServiceProvider 进行注册。



至此，我们完成了服务类Bean注册过程

### RpcReference 注解

与上一节是同样的思路

```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface RpcReference {
    Class<?> interfaceClass() default void.class;
}

```

```java

@RequiredArgsConstructor
@Slf4j
public class RpcReferenceProcessor implements BeanPostProcessor {
    private final InvocationHandler rpcClientInvocationHandler;


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            RpcReference rpcReference = field.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                log.debug("找到一个 RpcReference 的字段 {}", field.getName());
                // 实现类似 DubboReference
                // 接口是公共模块的
                // 接口的实现不在同一台服务器上
                // 我们通过代理类，为接口的每个调用构造请求
                // 通过远程调用来获取结果
                Class<?> rpcReferenceInterface = rpcReference.interfaceClass();
                if (rpcReferenceInterface == void.class) {
                    rpcReferenceInterface = field.getType();
                }
                // 根据接口获取代理类对象
                // 生成代理对象并注入
                log.debug("rpcReferenceInterface: {}", rpcReferenceInterface);

                Object proxy = ProxyFactory.createProxy(rpcReferenceInterface, rpcClientInvocationHandler);
                field.setAccessible(true);
                try {
                    log.debug("代理类注入 bean: {}, declareField: {}, proxy: {}", bean.getClass().getTypeName(), field.getName(), proxy.getClass().getTypeName());
                    field.set(bean, proxy);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("注入RPC服务失败", e);
                }
            }
        }
        return bean;
    }

}
```

```java
@Configuration
public class ClientConfig {
    @Bean
    public IRpcClient rpcClient(IServiceRegister serviceRegister) {
        NettyRpcClient nettyRpcClient = new NettyRpcClient(serviceRegister);
        return nettyRpcClient;
    }

    @Bean
    public InvocationHandler rpcClientInvocationHandler(IRpcClient rpcClient) {
        return new InvokeHandler(rpcClient);
    }

    @Bean
    public RpcReferenceProcessor rpcReferenceProcessor(InvocationHandler rpcClientInvocationHandler) {
        return new RpcReferenceProcessor(rpcClientInvocationHandler);
    }
}
```



p.s: 当初没分成两个项目的时候，同一个服务器作为服务提供者，又作为服务消费者。在服务注册与发现的过程中，由于不能用 `BeanPostProcessor` 区分先后顺序，即可能出现：服务还未注册，但先发现，然后出现错误。事实上，先发现也是可以的，因为发现过程只需要注入一个代理类即可。那为什么报错了呢？是因为在 `field.set(bean, proxy);` 之后，调用了 filed.getName() 导致去调用了反射的逻辑。



由于这样，所以我又写了一个，在所有服务创建好之后，再进行服务注册的事件监听器

```java
@RequiredArgsConstructor
@Slf4j
@Component
public class ServiceScanListener implements ApplicationListener<ContextRefreshedEvent> {
    private final InvocationHandler rpcClientInvocationHandler;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.debug("RpcReference 发现");
        // 容器完全启动后执行注册
        Map<String, Object> serviceBeans = event.getApplicationContext().getBeansWithAnnotation(Service.class);
        for (Map.Entry<String, Object> entry : serviceBeans.entrySet()) {
            String serviceName = entry.getKey();
            Object bean = entry.getValue();
            Field[] declaredFields = bean.getClass().getDeclaredFields();
            for (Field declaredField : declaredFields) {
                RpcReference annotation = declaredField.getAnnotation(RpcReference.class);
                if (annotation != null) {
                    log.debug("找到一个 RpcReference 的字段 {}", declaredField.getName());
                    // 为这个字段注入代理类
                    Class<?> referenceClass = annotation.interfaceClass();
                    if (referenceClass == void.class) {
                        referenceClass = declaredField.getType();
                    }
                    log.debug("referenceClass: {}", referenceClass);
                    Object proxy = ProxyFactory.createProxy(referenceClass, rpcClientInvocationHandler);
                    declaredField.setAccessible(true);
                    try {
                        log.debug("bean: {}, declareField: {}, proxy: {}", bean.getClass().getTypeName(), declaredField.getName(), proxy.getClass().getTypeName());
                        declaredField.set(bean, proxy);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

    }

}
```

p.s 这里可以忽略，仅作为自己的记录



至此，我们的小小拓展已经完成了。并且可以作为一个模块引入到项目中

## 测试

我们在 sample 里面定义了两个公共接口，并分别在 consumer 和 provider 定义和引入

![](https://cdn.nlark.com/yuque/0/2025/png/50582501/1743402235241-b6ff04b9-b2fb-4891-9aa6-6c82df61aac5.png)

![](https://cdn.nlark.com/yuque/0/2025/png/50582501/1743402247223-73b39de9-358e-448c-88ba-afa2cc41a69c.png)![](https://cdn.nlark.com/yuque/0/2025/png/50582501/1743402258881-85e82c0e-2487-4dbc-b5aa-12ddf8d8f2d1.png)

首先我们启动消费者和服务者

**消费者**

把我们的 rpc 模块引入到项目中

```java
    <dependencies>
        <dependency>
            <groupId>com.bobby.rpc.core</groupId>
            <artifactId>Bobby-RPC-Core</artifactId>
            <version>1.0-SNAPSHOT</version>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>

```

日志如下：

![](https://cdn.nlark.com/yuque/0/2025/png/50582501/1743402473359-41a02269-fa8c-4948-927a-a5e899b31eca.png)

在日志中，可以看到，我们的consumer作为服务者也作为消费者



**服务端：**

![](https://cdn.nlark.com/yuque/0/2025/png/50582501/1743402558359-d4febbf8-4437-42e9-8285-a9ed6d21c90f.png)



**测试**

<font style="color:rgb(33, 33, 33);">localhost:8085/rpc/consumer/doBiz</font>

![](https://cdn.nlark.com/yuque/0/2025/png/50582501/1743402599450-a4c16a41-4c59-4bf6-a9b0-8ee474084a78.png)

<font style="color:rgb(33, 33, 33);">localhost:8083/rpc/provider/doBiz</font>

![](https://cdn.nlark.com/yuque/0/2025/png/50582501/1743402740852-fac99ecb-e8f5-480c-9895-37198ed1fe6e.png)



## 存在问题

引入注解使我们的RPC 框架更加易用。

可以有以下改进

+ 重传机制：服务通信失败进行重传
+ 限流机制：当大量的请求打到远程调用时，可能会引发服务失败，远程主机宕机... 因此，我们需要设置一些合理的措施，如限流、服务降级、服务熔断措施
+ ...



p.s. 写好日志很重要！有助于定位问题所在...

# TODO

- [ ] 限流、降级、熔断措施
- [ ] 失败重传
- [ ] ...
