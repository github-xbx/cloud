package com.xbx.study.lock.main;

import com.xbx.study.lock.basic.JavaLock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockTest001 {


    Lock lock0 = new ReentrantLock();
    Lock lock1 = new ReentrantLock();
    Lock javaLock = new JavaLock();


    public static void main(String[] args) {

        LockTest001 lockTest001 = new LockTest001();
        for (int i = 0; i < 100; i++) {
            int finalI = i;
            new Thread(() -> {
                lockTest001.test("用户=>"+ finalI);
                lockTest001.test1("test1 用户=>"+ finalI);

                lockTest001.test0("test0用户=>"+ finalI);

            }).start();

        }


    }


    public void test(String name){

        try {
            if (lock0.tryLock(1, TimeUnit.SECONDS)){
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                    System.out.println("==test=== 抢到锁并执行 = " + name);
                }finally {
                    lock0.unlock();
                }
            }else {
                System.out.println("--test-- 未抢到锁超时 = " + name);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }



    public void test0(String name){

        lock1.lock();
        try {
            TimeUnit.MILLISECONDS.sleep(500);
            System.out.println("==test0=== 抢到锁并执行 = " + name);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock1.unlock();
        }

    }


    public void test1(String name){
        if (javaLock.tryLock()){
            try {
                TimeUnit.MILLISECONDS.sleep(500);
                System.out.println("==test1=== 抢到锁并执行 = " + name);
                return;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                javaLock.unlock();
            }
        }
        System.out.println("--test1-- 未抢到锁 = " + name);
    }


}
