package com.xbx.study.web.po;


import java.util.Date;

public class ProjectJob {

    private Long jobId;

    private String jobName;

    private String jobGroup;

    private String invokeTarget;

    private String cron;

    private Integer policy;

    /** 是否并发执行 */
    private Integer concurrent;

    private String remark;

    private Integer status;

    private String createBy;

    private Date createTime;

    private String updateBy;

    private Date updateTime;



}
