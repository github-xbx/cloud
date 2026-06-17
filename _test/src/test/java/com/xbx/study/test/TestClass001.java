package com.xbx.study.test;


import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class TestClass001 {

    ThreadPoolExecutor pool = new ThreadPoolExecutor(
            4,
            8,
            60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(10),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setName("test-pool-thread");
                    return thread;
                }
            },
            new ThreadPoolExecutor.AbortPolicy()
    );

    @Test
    public void test1(){

        Map<String, String> map = new HashMap<>(1);

        map.put("key","value");
        map.put("key2","value2");
        map.put("key3","value3");
        map.put("key4","value4");
        map.put("key5","value5");
        map.put("key6","value6");

        Map<String, String> map2 = new ConcurrentHashMap<>();


    }


    @Test
    public void test2(){




    }





}
