package com.xbx.study.jdbc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xbx.study.jdbc.po.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

}
