package com.xbx.study.test;


import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TestClass001 {

    @Test
    public void test1(){

        Map<String, String> map = new HashMap<>(1);

        map.put("key","value");
        map.put("key2","value2");
        map.put("key3","value3");
        map.put("key4","value4");
        map.put("key5","value5");
        map.put("key6","value6");
    }

}
