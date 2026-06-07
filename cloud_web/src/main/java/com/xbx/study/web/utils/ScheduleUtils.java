package com.xbx.study.web.utils;

import com.xbx.study.web.config.quartz.QuartzDisallowConcurrentExecution;
import com.xbx.study.web.config.quartz.QuartzJobExecution;
import com.xbx.study.web.po.ProjectJob;
import com.xbx.study.web.utils.spring.ContextHolder;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;

import java.text.ParseException;
import java.util.Date;

public class ScheduleUtils {


    private static final String[] JOB_WHITELIST_STR = {"com.xbx.study.web.task"};

    //定时任务违规的字符
    public static final String[] JOB_ERROR_STR = { "java.net.URL", "javax.naming.InitialContext", "org.yaml.snakeyaml",
            "org.springframework", "org.apache", };

    /**
     * 得到quartz任务类
     *
     * @param sysJob 执行计划
     * @return 具体执行任务类
     */
    private static Class<? extends Job> getQuartzJobClass(ProjectJob sysJob) {
        boolean isConcurrent = Integer.valueOf("0").equals(sysJob.getConcurrent());
        return isConcurrent ? QuartzJobExecution.class : QuartzDisallowConcurrentExecution.class;
    }

    /**
     * 构建任务触发对象
     */
    public static TriggerKey getTriggerKey(Long jobId, String jobGroup) {
        return TriggerKey.triggerKey("task" + jobId, jobGroup);
    }

    /**
     * 构建任务键对象
     */
    public static JobKey getJobKey(Long jobId, String jobGroup) {
        return JobKey.jobKey("task" + jobId, jobGroup);
    }





    /**
     * 创建定时任务
     */
    public static void createScheduleJob(Scheduler scheduler, ProjectJob job) throws SchedulerException {
        Class<? extends Job> jobClass = getQuartzJobClass(job);
        // 构建job信息
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(getJobKey(jobId, jobGroup)).build();

        // 表达式调度构建器
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCron());
        cronScheduleBuilder = handleCronScheduleMisfirePolicy(job, cronScheduleBuilder);

        // 按新的cronExpression表达式构建一个新的trigger
        CronTrigger trigger = TriggerBuilder
                .newTrigger()
                .withIdentity(getTriggerKey(jobId, jobGroup))
                .withSchedule(cronScheduleBuilder).build();

        // 放入参数，运行时的方法可以获取
        jobDetail.getJobDataMap().put("TASK_PROPERTIES", job);

        // 判断是否存在
        if (scheduler.checkExists(getJobKey(jobId, jobGroup))) {
            // 防止创建时存在数据问题 先移除，然后在执行创建操作
            scheduler.deleteJob(getJobKey(jobId, jobGroup));
        }

        // 判断任务是否过期
        if (ObjectUtils.isNotEmpty(getNextExecution(job.getCron()))) {
            // 执行调度任务 注册任务，按 cron 周期执行
            scheduler.scheduleJob(jobDetail, trigger);
        }

        // 暂停任务
        if (job.getStatus().equals(1)) {
            scheduler.pauseJob(getJobKey(jobId, jobGroup));
        }
    }

    /**
     * 设置定时任务策略
     */
    public static CronScheduleBuilder handleCronScheduleMisfirePolicy(ProjectJob job, CronScheduleBuilder cb) {
        switch (job.getPolicy())
        {
            case 0:
                return cb;
            case 1: //立即触发执行
                return cb.withMisfireHandlingInstructionIgnoreMisfires();
            case 2: //触发一次执行
                return cb.withMisfireHandlingInstructionFireAndProceed();
            case 3: //不触发立即执行
                return cb.withMisfireHandlingInstructionDoNothing();
            default:
                throw new RuntimeException("The task misfire policy '" + job.getPolicy()
                        + "' cannot be used in cron schedule tasks");
        }
    }

    /**
     * 检查包名是否为白名单配置
     *
     * @param invokeTarget 目标字符串
     * @return 结果
     */
    public static boolean whiteList(String invokeTarget)
    {
        String packageName = StringUtils.substringBefore(invokeTarget, "(");
        int count = StringUtils.countMatches(packageName, ".");
        if (count > 1)
        {
            return StringUtils.containsAnyIgnoreCase(invokeTarget, JOB_WHITELIST_STR);
        }
        Object obj = ContextHolder.getBean(StringUtils.split(invokeTarget, ".")[0]);
        String beanPackageName = obj.getClass().getPackage().getName();
        return StringUtils.containsAnyIgnoreCase(beanPackageName, JOB_WHITELIST_STR)
                && !StringUtils.containsAnyIgnoreCase(beanPackageName, JOB_ERROR_STR);
    }


    /**
     * 返回下一个执行时间根据给定的Cron表达式
     *
     * @param cronExpression Cron表达式
     * @return Date 下次Cron表达式执行时间
     */
    public static Date getNextExecution(String cronExpression) {
        try {
            CronExpression cron = new CronExpression(cronExpression);
            return cron.getNextValidTimeAfter(new Date(System.currentTimeMillis()));
        }
        catch (ParseException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }


    /**
     * 返回一个字符串值,表示该消息无效Cron表达式给出有效性
     *
     * @param cronExpression Cron表达式
     * @return String 无效时返回表达式错误描述,如果有效返回null
     */
    public static String getInvalidMessage(String cronExpression) {
        try {
            new CronExpression(cronExpression);
            return null;
        }
        catch (ParseException pe) {
            return pe.getMessage();
        }
    }

}
