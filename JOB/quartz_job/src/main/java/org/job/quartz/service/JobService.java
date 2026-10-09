package org.job.quartz.service;


import com.xbx.database.base.BaseServiceImpl;
import jakarta.annotation.PostConstruct;
import org.job.quartz.mapper.JobMapper;
import org.job.quartz.po.ProjectJob;
import org.job.quartz.utils.ScheduleUtils;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService extends BaseServiceImpl<JobMapper, ProjectJob> {


    // quartz scheduler
    private final Scheduler scheduler;


    @Autowired
    public JobService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }


    /**
     * 项目启动时，初始化定时器 主要是防止手动修改数据库导致未同步到定时任务处理（注：不能手动修改数据库ID和任务组名，否则会导致脏数据）
     */
    @PostConstruct
    public void init() throws Exception {

        List<ProjectJob> list = this.list();

        list.forEach(job -> {
            try {
                ScheduleUtils.createScheduleJob(scheduler,job);
            } catch (SchedulerException e) {
                throw new RuntimeException(e);
            }
        });


    }






















}
