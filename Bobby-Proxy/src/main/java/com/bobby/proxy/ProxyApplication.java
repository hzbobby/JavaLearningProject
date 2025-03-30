package com.bobby.proxy;

import com.bobby.proxy.factory.ProxyFactory;
import com.bobby.proxy.service.IBlogService;
import com.bobby.proxy.service.IUserService;
import com.bobby.proxy.service.impl.BlogServiceImpl;
import com.bobby.proxy.service.impl.UserServiceImpl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author: Bobby
 * @email: vividbobby@163.com
 * @date: 2025/3/30
 */
public class ProxyApplication {
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        IUserService userService = new UserServiceImpl();
        IUserService proxy = ProxyFactory.createProxy(IUserService.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                System.out.println("代理类方法调用开始");
                Object invoke = method.invoke(userService, args);
                System.out.println("代理类方法调用结束");
                return invoke;
            }
        });

        // 通过反射获取某个类的字段，然后把代理类注入给它
        System.out.println("代理类的使用");
        proxy.doSomething();

        IBlogService blogService = new BlogServiceImpl();
        blogService.setUserService(userService);
        System.out.println("代理类注入之前的调用");
        blogService.doSomething();

        Field field = blogService.getClass().getDeclaredField("userService");
        // 将代理类注入
        field.setAccessible(true);
        field.set(blogService, proxy);

        System.out.println("代理类注入之后的调用");
        blogService.doSomething();

    }
}
