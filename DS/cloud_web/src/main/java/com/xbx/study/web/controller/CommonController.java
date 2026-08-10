package com.xbx.study.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("common")
public class CommonController {

    private static final String COURSE_ID = "c_100001";
    private static final String UN_LOCK_COURSE_ID = "c_un_lock_100001";



    @GetMapping("test")
    public String test(){
        return "success";
    }


}
