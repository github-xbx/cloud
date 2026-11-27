package com.ai.langgraph.service.langgraph.deploy;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 工作流状态对象：所有节点共享，并会被 checkpointer 持久化以支持断点续跑。
 *
 * 使用 Jackson 序列化落库，同时实现 Serializable 以满足 LangGraph4j
 * 图内 checkpointer（MemorySaver）的 Java 原生序列化克隆需求。
 */
@TableName("deploy_state")
public class DeployState implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO) // 对应 SQLite 的 AUTOINCREMENT
    private Long id;

    // ===== 输入（不可变，任务启动时确定）=====
    private String taskId;           // 任务唯一 ID
    private String projectPath;      // 本地项目路径，或 git 仓库地址
    private String sourceType;       // local / git
    private String projectType;      // java / ts / vue / python
    private String deployMode;       // docker / direct（直传产物）
    private String version;          // 本次部署版本号

    // ===== 中间状态（可变，随流程推进）=====
    private String status;           // PENDING / FETCHING / BUILDING / UPLOADING / DEPLOYING / VERIFYING / SUCCESS / FAILED / ROLLED_BACK
    private String stage;            // 当前所处节点名（用于断点续跑定位）
    private String artifactPath;     // 构建产物本地路径
    private String remoteArtifactPath; // 服务器上产物路径
    private String backupVersion;    // 回滚用的旧版本
    private String lastError;        // 最近一次错误信息
    private String buildLog;         // 构建日志（供 LLM 分析）
    private String extra ; // 扩展字段



    public DeployState() {
        this.status = "PENDING";
    }

    // ===== 便捷方法 =====
    public DeployState withId(String id) {
        this.taskId = id;
        return this;
    }

    public DeployState withProject(String path, String sourceType, String projectType, String deployMode, String version) {
        this.projectPath = path;
        this.sourceType = sourceType;
        this.projectType = projectType;
        this.deployMode = deployMode;
        this.version = version;
        return this;
    }

    public void fail(String stage, String error) {
        this.status = "FAILED";
        this.stage = stage;
        this.lastError = error;
    }

    public void advance(String status, String stage) {
        this.status = status;
        this.stage = stage;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public String getDeployMode() {
        return deployMode;
    }

    public void setDeployMode(String deployMode) {
        this.deployMode = deployMode;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getArtifactPath() {
        return artifactPath;
    }

    public void setArtifactPath(String artifactPath) {
        this.artifactPath = artifactPath;
    }

    public String getRemoteArtifactPath() {
        return remoteArtifactPath;
    }

    public void setRemoteArtifactPath(String remoteArtifactPath) {
        this.remoteArtifactPath = remoteArtifactPath;
    }

    public String getBackupVersion() {
        return backupVersion;
    }

    public void setBackupVersion(String backupVersion) {
        this.backupVersion = backupVersion;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public String getBuildLog() {
        return buildLog;
    }

    public void setBuildLog(String buildLog) {
        this.buildLog = buildLog;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getExtra() {
        return extra;
    }

    @Override
    public String toString() {
        return "DeployState{taskId='" + taskId + "', type='" + projectType + "', mode='" + deployMode
                + "', status='" + status + "', stage='" + stage + "'}";
    }
}
