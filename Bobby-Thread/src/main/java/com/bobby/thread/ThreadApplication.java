package com.bobby.thread;

import java.util.ArrayList;
import java.util.List;

public class ThreadApplication {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);
        for (Integer i : list) {
            list.set(list.indexOf(i), i*2);
        }
        System.out.println(list);
    }
}
