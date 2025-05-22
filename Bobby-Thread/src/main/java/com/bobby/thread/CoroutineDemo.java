package com.bobby.thread;

public class CoroutineDemo {
    public static void main(String[] args) {
        Runtime.Version version = Runtime.version();
        System.out.println(version);
//        Thread.startVirtualThread(() -> {
//            System.out.println("Hello from virtual thread!");
//        });
//        Thread.startVirtualThread(() -> {
//            System.out.println("2222222222");
//        });
    }
}
