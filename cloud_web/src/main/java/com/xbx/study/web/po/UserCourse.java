package com.xbx.study.web.po;


import java.util.Date;

public class UserCourse {

    private long id;

    private String courseId;

    private String userId;

    private Date createTime;

    private String type;

    public long getId() {
        return id;
    }

    public UserCourse setId(long id) {
        this.id = id;
        return this;
    }

    public String getCourseId() {
        return courseId;
    }

    public UserCourse setCourseId(String courseId) {
        this.courseId = courseId;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public UserCourse setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public UserCourse setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public String getType() {
        return type;
    }

    public UserCourse setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public String toString() {
        return "UserCourse{" +
                "id=" + id +
                ", courseId='" + courseId + '\'' +
                ", userId='" + userId + '\'' +
                ", createTime=" + createTime +
                ", type='" + type + '\'' +
                '}';
    }
}
