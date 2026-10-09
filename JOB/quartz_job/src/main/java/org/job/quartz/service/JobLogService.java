package org.job.quartz.service;

import com.xbx.database.base.BaseServiceImpl;
import org.job.quartz.mapper.JobLogMapper;
import org.job.quartz.po.ProjectJobLog;
import org.springframework.stereotype.Service;

@Service
public class JobLogService extends BaseServiceImpl<JobLogMapper, ProjectJobLog> {
}
