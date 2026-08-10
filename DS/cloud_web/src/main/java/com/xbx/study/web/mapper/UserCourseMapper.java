package com.xbx.study.web.mapper;

import com.xbx.study.web.po.UserCourse;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserCourseMapper {


    void addOne(UserCourse userCourse);

}
