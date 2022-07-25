package com.osiris.jsqlgen.utils;


import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MyThread {
    public static Executor cachedPool = Executors.newCachedThreadPool();
    public static Executor singlePool = Executors.newSingleThreadExecutor();

    public static void runAsync(Runnable runnable){
        cachedPool.execute(runnable);
    }

    public static void runAsyncSingle(Runnable runnable){
        singlePool.execute(runnable);
    }
}
