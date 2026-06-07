package com.xbx.study.web.config.quartz;

import com.xbx.study.web.po.ProjectJob;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

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

//        final SysJobLog sysJobLog = new SysJobLog();
//        sysJobLog.setJobName(sysJob.getJobName());
//        sysJobLog.setJobGroup(sysJob.getJobGroup());
//        sysJobLog.setInvokeTarget(sysJob.getInvokeTarget());
//        sysJobLog.setStartTime(startTime);
//        sysJobLog.setStopTime(new Date());
//        long runMs = sysJobLog.getStopTime().getTime() - sysJobLog.getStartTime().getTime();
//        sysJobLog.setJobMessage(sysJobLog.getJobName() + " 总共耗时：" + runMs + "毫秒");
//        if (e != null)
//        {
//            sysJobLog.setStatus(Constants.FAIL);
//            String errorMsg = StringUtils.substring(ExceptionUtil.getExceptionMessage(e), 0, 2000);
//            sysJobLog.setExceptionInfo(errorMsg);
//        }
//        else
//        {
//            sysJobLog.setStatus(Constants.SUCCESS);
//        }

        // 写入数据库当中
        // ContextHolder.getBean(ISysJobLogService.class).addJobLog(sysJobLog);
    }

    /**
     * 执行方法，由子类重载
     *
     * @param context 工作执行上下文对象
     * @param sysJob 系统计划任务
     * @throws Exception 执行过程中的异常
     */
    protected abstract void doExecute(JobExecutionContext context, ProjectJob sysJob) throws Exception;

}
