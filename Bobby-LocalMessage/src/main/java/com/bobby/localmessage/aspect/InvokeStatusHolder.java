package com.bobby.localmessage.aspect;

public class InvokeStatusHolder {

    private static volatile boolean inInvoke = false;

    public static boolean inInvoke() {
        return inInvoke;
    }
    public static void startInvoke() {
        inInvoke = true;
    }
    public static void endInvoke() {
        inInvoke = false;
    }
}
