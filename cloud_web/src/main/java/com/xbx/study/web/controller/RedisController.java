package com.xbx.study.web.controller;

import com.xbx.study.common.TestLock;
import com.xbx.study.web.service.RedisLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("redis")
public class RedisController {

    private static final String COURSE_ID = "c_100001";
    private static final String UN_LOCK_COURSE_ID = "c_un_lock_100001";

    @Autowired
    private RedisLock redisLock;


    @GetMapping("lock")
    public String lock(){
        redisLock.CourseSecondKill(COURSE_ID);

        return "success";
    }
    @GetMapping("unlock")
    public String unlock(){
        redisLock.CourseSecondKillNotLock(UN_LOCK_COURSE_ID);

        return "success";
    }

    @GetMapping("init")
    public String init(@RequestParam("courseId") String courseId){
        String  param =  StringUtils.hasText(courseId) ? courseId : COURSE_ID;
        redisLock.dataInit(param);
        return "success";
    }


}
