package com.xbx.study.lock.basic;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class JavaLock extends AbstractQueuedSynchronizer implements Lock {


    /**
     * 获取锁
     */
    @Override
    public void lock() {
        //获取锁
        acquire(1);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    /**
     * 立刻获取锁，成功获取锁返回 true，没有回去到锁 返回false
     * @return true 获取锁成功 false 获取锁失败
     */
    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    /**
     * 释放锁
     */
    @Override
    public void unlock() {
        release(1);
    }

    @Override
    public Condition newCondition() {
        return null;
    }


    /**
     * 释放锁 核心逻辑 需要自己实现 AQS没有实现
     * @param arg the release argument. This value is always the one
     *        passed to a release method, or the current state value upon
     *        entry to a condition wait.  The value is otherwise
     *        uninterpreted and can represent anything you like.
     * @return  true
     */
    @Override
    protected boolean tryRelease(int arg) {

        int c = getState() - arg;
        if (getExclusiveOwnerThread() != Thread.currentThread())
            throw new IllegalMonitorStateException();

        //设置所持有者为空
        setExclusiveOwnerThread(null);
        setState(c);
        System.out.println(Thread.currentThread().getName() + ", 释放锁成功");
        return true;
    }

    @Override
    protected boolean tryAcquire(int arg) {

        //CAS 尝试获取锁
        if (compareAndSetState(0,arg)){
            //独占农事下 设置锁的持有者为当前线程，来自AQS
            setExclusiveOwnerThread(Thread.currentThread());
            System.out.println(Thread.currentThread().getName() + ", 获取锁成功");
            return true;
        }
        System.out.println(Thread.currentThread().getName() + ", 获取锁失败");
        return false;
    }
}
