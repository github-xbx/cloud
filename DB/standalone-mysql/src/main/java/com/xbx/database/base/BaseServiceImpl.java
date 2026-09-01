package com.xbx.database.base;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 通用 Service 实现基类。
 *
 * @param <M> Mapper 类型（须继承 {@link BaseMapper}）
 * @param <T> 实体类型
 */
public abstract class BaseServiceImpl<M extends BaseMapper<T>, T>
        extends ServiceImpl<M, T> implements IBaseService<T> {
}
