package com.xbx.study.ai.mapper;

import com.xbx.study.ai.entity.po.AiMessagePo;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AiMessageMapper {

    /**
     * 新增
     * @param message
     */
    @Insert(value = "INSERT INTO ai_message(message_id,thread_id,run_id,role,content,create_at) VALUES (#{message.messageId}, #{message.threadId}, #{message.runId}, #{message.role}, #{message.content}, #{message.createAt})")
    void insert(@Param("message") AiMessagePo message);

    /**
     * 批量新增
     * @param list
     */
    @Insert({
            "<script>",
            "INSERT INTO ai_message(message_id,thread_id,run_id,role,content,create_at) VALUES ",
            "<foreach collection='list' item='message' separator=','>",
            "(#{message.messageId}, #{message.threadId}, #{message.runId}, #{message.role}, #{message.content}, #{message.createAt}) ",
            "</foreach>",
            "</script>"
    })
    void batchInsert(@Param("list") List<AiMessagePo> list);

    /**
     * 根据 threadId 查询历史消息
     */
    @Select("SELECT * FROM ai_message where thread_id = #{threadId} ORDER BY create_at ")
    List<AiMessagePo> selectByThreadId(@Param("threadId") String threadId);

    @Delete(
            {
                    "<script>",
                    "DELETE FROM ai_message WHERE id IN ",
                    "<foreach collection='list' item='item' separator=',' open='(' close=')' >",
                    "#{item}",
                    "</foreach>",
                    "</script>"
            }
    )
    int deleteByIds(@Param("list") List<Long> ids);

}
