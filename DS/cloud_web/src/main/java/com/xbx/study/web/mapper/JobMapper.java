package com.xbx.study.web.mapper;

import com.xbx.study.web.po.ProjectJob;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface JobMapper {


    int insertJob(ProjectJob projectJob);

    List<ProjectJob> selectAllJob();

    ProjectJob selectJobById(Integer id);


}
