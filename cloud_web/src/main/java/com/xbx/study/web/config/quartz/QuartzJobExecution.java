package com.xbx.study.web.config.quartz;

import com.xbx.study.web.po.ProjectJob;
import com.xbx.study.web.utils.JobInvokeUtil;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;

/**
 *  定时任务处理（允许并发执行）
 */
@DisallowConcurrentExecution
public class QuartzJobExecution extends AbstractQuartzJob{
    @Override
    protected void doExecute(JobExecutionContext context, ProjectJob sysJob) throws Exception {
        JobInvokeUtil.invokeMethod(sysJob);
    }
}
