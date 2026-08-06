package com.xbx.study.ai.mapper;

import com.xbx.study.ai.entity.po.AiThreadPo;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

@Mapper
public interface AiThreadMapper {


    @Insert("INSERT INTO ai_thread(thread_id,thread_name,create_at,update_at,last_run_at) VALUES (#{thread.threadId}, #{thread.threadName} , #{thread.createAt} , #{thread.updateAt} , #{thread.lastRunAt} )")
    int insert(@Param("thread") AiThreadPo thread);


    @Select("SELECT * FROM ai_thread WHERE thread_id = #{threadId} ")
    AiThreadPo selectByThreadId(@Param("threadId") String threadId);


    @Update("UPDATE ai_thread SET update_at = #{time}, last_run_at = #{time} WHERE thread_id = #{threadId} ")
    int updateTimeByThreadId(@Param("threadId") String threadId, @Param("time") LocalDateTime time);


}
