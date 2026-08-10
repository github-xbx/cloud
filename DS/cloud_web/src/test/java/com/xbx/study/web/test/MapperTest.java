package com.xbx.study.web.test;

import com.xbx.study.web.mapper.JobMapper;
import com.xbx.study.web.po.ProjectJob;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
public class MapperTest {

    @Autowired
    JobMapper jobMapper;

    @Test
    public void test(){

        ProjectJob projectJob = new ProjectJob();
        projectJob.setJobName("hello word");
        projectJob.setJobGroup("test");
        projectJob.setInvokeTarget("com.xbx.study.web.task.HelloTask.hello()");
        projectJob.setCron("0/5 * * * * ?");
        projectJob.setPolicy(1);
        projectJob.setConcurrent(1);
        projectJob.setStatus(0);
        projectJob.setCreateBy("test-user");
        projectJob.setCreateTime(new Date());

        jobMapper.insertJob(projectJob);

    }


}
