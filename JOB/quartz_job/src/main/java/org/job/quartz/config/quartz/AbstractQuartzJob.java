package org.job.quartz.config.quartz;


import org.apache.commons.lang3.StringUtils;
import org.job.quartz.po.ProjectJob;
import org.job.quartz.po.ProjectJobLog;
import org.job.quartz.service.JobLogService;
import org.job.quartz.utils.spring.ContextHolder;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

public abstract class AbstractQuartzJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(AbstractQuartzJob.class);

    /**
     * 线程本地变量
     */
    private static final ThreadLocal<Date> threadLocal = new ThreadLocal<>();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        ProjectJob sysJob = new ProjectJob();

        BeanUtils.copyProperties(context.getMergedJobDataMap().get("TASK_PROPERTIES"), sysJob);
        try {
            before(context, sysJob);
            doExecute(context, sysJob);
            after(context, sysJob, null);
        }
        catch (Exception e) {
            log.error("任务执行异常  - ：", e);
            after(context, sysJob, e);
        }
    }

    /**
     * 执行前
     *
     * @param context 工作执行上下文对象
     * @param sysJob 系统计划任务
     */
    protected void before(JobExecutionContext context, ProjectJob sysJob) {
        threadLocal.set(new Date());
    }

    /**
     * 执行后
     *
     * @param context 工作执行上下文对象
     * @param sysJob 系统计划任务
     */
    protected void after(JobExecutionContext context, ProjectJob sysJob, Exception e) {
        Date startTime = threadLocal.get();
        threadLocal.remove();

        final ProjectJobLog projectJobLog = new ProjectJobLog();
        projectJobLog.setJobName(sysJob.getJobName());
        projectJobLog.setJobGroup(sysJob.getJobGroup());
        projectJobLog.setInvokeTarget(sysJob.getInvokeTarget());
        projectJobLog.setStartTime(startTime);
        projectJobLog.setStopTime(new Date());
        projectJobLog.setCreateTime(startTime);
        long runMs = projectJobLog.getStopTime().getTime() - projectJobLog.getStartTime().getTime();
        projectJobLog.setJobMessage(projectJobLog.getJobName() + " 总共耗时：" + runMs + "毫秒");
        if (e != null)
        {
            projectJobLog.setStatus("1");
            String errorMsg = StringUtils.substring(getExceptionMessage(e), 0, 2000);
            projectJobLog.setExceptionInfo(errorMsg);
        }
        else
        {
            projectJobLog.setStatus("0");
        }

         //写入数据库当中
         ContextHolder.getBean(JobLogService.class).save(projectJobLog);
    }

    /**
     * 执行方法，由子类重载
     *
     * @param context 工作执行上下文对象
     * @param sysJob 系统计划任务
     * @throws Exception 执行过程中的异常
     */
    protected abstract void doExecute(JobExecutionContext context, ProjectJob sysJob) throws Exception;


    /**
     * 获取exception的详细错误信息。
     */
    private String getExceptionMessage(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw, true));
        return sw.toString();
    }

}
