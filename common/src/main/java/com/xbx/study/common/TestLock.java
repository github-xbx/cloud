package com.xbx.study.common;

public interface TestLock {

    /**
     * 锁测试
     * 秒杀场景 有锁
     */
    void CourseSecondKill(String ...param);

    /**
     * 锁测试
     * 秒杀场景 无锁
     *
     */
    void CourseSecondKillNotLock(String ...param);

}
