package com.xbx.study.web.service;

import com.xbx.study.common.TestLock;
import com.xbx.study.web.mapper.UserCourseMapper;
import com.xbx.study.web.po.UserCourse;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RedisLock implements TestLock {

    private final static Logger logger = LoggerFactory.getLogger(RedisLock.class);
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserCourseMapper userCourseMapper;

    private Integer count = 100;
    private Integer count1 = 100;

    public void dataInit(String courseId){

        redisTemplate.opsForValue().set("course:storage:" + courseId,100, 60 * 60 * 24,TimeUnit.SECONDS);


    }



    /**
     * redis 分布式锁课程秒杀场景
     * redis 中存储秒杀商品数量  100个
     *
     */
    @Override
    public void CourseSecondKill(String ...courseId) {

        String redisLockKey = CollectionUtils.isEmpty(List.of(courseId)) ? "default": courseId[0];
        String redisStorageKey = CollectionUtils.isEmpty(List.of(courseId)) ? "default": courseId[0];
        RLock rLock = redissonClient.getLock("second_kill:course:"+redisLockKey);
        try {
            //尝试获取锁，参数分别是：获取锁的最大等待时间(期间会重试)，锁自动释放时间，时间单位
            boolean lockIsSuccess = rLock.tryLock(10,10, TimeUnit.SECONDS);//上锁
            if (lockIsSuccess){
                //上锁成功
                //Long decrement = redisTemplate.opsForValue().decrement("course:storage:" + redisStorageKey);
                --count1;
                if (count1 < 0){
                    throw new RuntimeException("课程报名人数已满");
                }
                //秒杀成功 （课程报名成功）报名记录写入数据库
                UserCourse userCourse = new UserCourse()
                        .setCourseId(courseId[0]+"_"+count1.toString())
                        .setUserId("test_lock_" + Thread.currentThread().getName())
                        .setCreateTime(new Date())
                        .setType("1");
                userCourseMapper.addOne(userCourse);
                TimeUnit.MILLISECONDS.sleep(1000L);
                logger.info("报名成功，报名信息： {}, thread=> {}",userCourse.toString(),Thread.currentThread().getName());
            }else {
                //上锁失败
                throw new RuntimeException("上锁失败");
            }

        } catch (Exception e) {

            logger.error("错误信息：{}",e.getMessage());
        } finally {
            if (rLock.isLocked() && rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }

    }

    @Override
    public void CourseSecondKillNotLock(String... courseId) {

        String redisStorageKey = CollectionUtils.isEmpty(List.of(courseId)) ? "default": courseId[0];
        try {
            Long decrement = redisTemplate.opsForValue().decrement("course:storage:" + redisStorageKey);
            --count;
            if (count < 0L ){
                throw new RuntimeException("课程报名人数已满");
            }
            //秒杀成功 （课程报名成功）报名记录写入数据库
            UserCourse userCourse = new UserCourse()
                    .setCourseId(courseId[0]+"_"+count.toString())
                    .setUserId("test_lock_" + Thread.currentThread().getName())
                    .setCreateTime(new Date())
                    .setType("1");
            userCourseMapper.addOne(userCourse);
            TimeUnit.MILLISECONDS.sleep(1000L);
            logger.info("报名成功，报名信息： {}, thread=> {}",userCourse.toString(),Thread.currentThread().getName());


        } catch (Exception e) {

            logger.error("错误信息：{}, unLock",e.getMessage());
        } finally {

        }

    }
}
