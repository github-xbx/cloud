package com.xbx.study.web.service;

import com.xbx.study.web.mapper.JobMapper;
import com.xbx.study.web.po.ProjectJob;
import com.xbx.study.web.utils.ScheduleUtils;
import jakarta.annotation.PostConstruct;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService  {


    // quartz scheduler
    private final Scheduler scheduler;

    private final JobMapper jobMapper;

    @Autowired
    public JobService(Scheduler scheduler, JobMapper jobMapper) {
        this.scheduler = scheduler;
        this.jobMapper = jobMapper;
    }


    //@PostConstruct
    public void init() throws Exception {

        List<ProjectJob> list = jobMapper.selectAllJob();

        list.forEach(job -> {
            try {
                ScheduleUtils.createScheduleJob(scheduler,job);
            } catch (SchedulerException e) {
                throw new RuntimeException(e);
            }
        });


    }






















}
