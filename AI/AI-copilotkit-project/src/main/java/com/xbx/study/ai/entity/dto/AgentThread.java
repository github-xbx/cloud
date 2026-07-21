package com.xbx.study.ai.entity.dto;

import java.time.LocalDateTime;

public class AgentThread {

    /** id */
    private String id;
    /** 名称 */
    private String name;
    /** 创建时间 */
    private LocalDateTime createAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
    /** 最后运行时间 */
    private LocalDateTime lastRunAt;
    /** 是否已归档 */
    private Boolean archived;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getLastRunAt() {
        return lastRunAt;
    }

    public void setLastRunAt(LocalDateTime lastRunAt) {
        this.lastRunAt = lastRunAt;
    }

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }
}
