package org.job.quartz.mapper;

import com.xbx.database.base.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.job.quartz.po.ProjectJobLog;

@Mapper
public interface JobLogMapper extends BaseMapper<ProjectJobLog> {
}
