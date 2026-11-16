package com.xbx.study.test;


import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
    public void test2() throws IOException {


        byte[] bytes = Files.readAllBytes(Paths.get("D:/logo.png"));
        String base64 = Base64.getEncoder().encodeToString(bytes);
        System.out.println(base64);


    }


    /**
     * lur 最近 最久未使用
     */
    @Test
    public void lur(){

        //三个字含义 分别 容量、荷载因子、是否顺序访问
        Map<String, String> map = new LinkedHashMap<>(2,0.75F, true);

        map.put("a","a");
        map.put("b","b");
        map.put("c","c");

        System.out.println(map);

        map.get("c");
        System.out.println(map);
        map.get("a");
        System.out.println(map);
        map.get("a");
        System.out.println(map);
        map.get("b");
        System.out.println(map);


    }





}
