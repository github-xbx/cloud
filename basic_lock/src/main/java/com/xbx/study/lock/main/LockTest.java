package com.xbx.study.lock.main;

import com.xbx.study.lock.basic.JavaLock;

public class LockTest {


    private final JavaLock lock = new JavaLock();



    public void use(){
        lock.lock();
        try {
            System.out.println(Thread.currentThread().getName()+ "=> 123");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            lock.unlock();
        }
    }


    public static void main(String[] args) {


        LockTest test = new LockTest();
        for (int i = 0; i < 5; i++) {
            new Thread(test::use).start();
        }
    }
}
