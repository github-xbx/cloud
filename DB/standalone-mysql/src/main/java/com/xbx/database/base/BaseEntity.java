package com.xbx.database.base;

import com.baomidou.mybatisplus.annotation.Version;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用实体基类，内置乐观锁版本号字段。
 *
 * <p>主键不在此定义，由子类用 {@code @TableId} 自行指定策略。
 */
public abstract class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 乐观锁版本号，插入时默认 0，每次更新自动 +1。 */
    @Version
    private Integer version = 0;

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
