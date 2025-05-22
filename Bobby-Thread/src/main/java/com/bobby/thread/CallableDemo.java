package com.bobby.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class CallableDemo implements Callable<String> {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        FutureTask<String> futureTask = new FutureTask<String>(new CallableDemo());
        new Thread(futureTask).start();

        // 阻塞获取
        String s = futureTask.get();
        System.out.println(s);

    }

    @Override
    public String call() throws Exception {
        return "hello";
    }
}
