package com.xbx.study.lock.main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class abc {


    public static void main(String[] args) {
        List<String> list = new CopyOnWriteArrayList<>();
        //List<String> list = new ArrayList<>();

        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");

        for (String s : list) {
//        for (int i = 0; i < list.size(); i++) {
//
//            String s = list.get(i);
            if (s == "1"){
                list.remove(s);
            }
            System.out.println(s+ " ");
        }

        System.out.println(list);
    }


}
