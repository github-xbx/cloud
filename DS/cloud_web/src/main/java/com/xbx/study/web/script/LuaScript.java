package com.xbx.study.web.script;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

/**
 * Lua 脚本定义实现类
 */
@Component
public class LuaScript {



    public RedisScript<Long> limitScript(){
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(limitScriptText());
        script.setResultType(Long.class);
        return script;
    }


    private String limitScriptText(){


        /**
         *
         * 在 Lua 脚本中 使用 redis.call() 和 redis.pcall() 执行命令
         * redis.call() 执行命令出错时脚本会停止并返回错误
         * redis.pcall() 执行命令 出错时会捕获错误并返回一个包含错误信息的Lua表，脚本会继续执行
         * ===========================================
         * KEYS 与 ARGV 全局数组：用于接收脚本的外部参数
         * KEYS：用于传递 Redis 的键名。为确保在集群环境下的正确性，脚本中所有要操作的键都必须通过此数组传递
         * ARGV：用于传递除键名外的其他参数（如值、偏移量等）
         * ===========================================
         * tonumber() 将输入的参数（通常是字符串）尝试转换为数字（Number）类型。如果传入的参数本身就是数字则直接返回
         * 因为 redis.call() 返回的所有值默认都是字符串（String）类型，如果对数字做处理 都要先私用tonumber()转化一下
         *
         */

        /**
         * 这段脚本的作用
         * 参数： key（一个redis的key） argv1 个数(数字字符串都可) argv2 时间(数字字符串都可)单位s
         * 作用：
         * 当前key在argv2时间段内写入的数据（每次执行脚本+1）并返回当前写入的数据个数
         *
         *  local key = KEYS[1]                                 //获取参数key
         *  local count = tonumber(ARGV[1])                     //获取参数1
         *  local time = tonumber(ARGV[2])                      //获取参数2
         *  local current = redis.call('get',key);              //根据key 获取key中存储的值
         *  if current and tonumber(current) > count then       //如果key中的值>参数1
         *      return tonumber(current);                       //直接返回key中存储的值
         *  end                                                 //end 表示if结束
         *  current = redis.call('incr', key)                   //key 中的值自加一
         *  if tonumber(current) == 1 then                      //如果key的值 == 1 （表示 key 第一次保存）
         *      redis.call('expire', key, time)                 //设置key的过期实际按 参数2
         *  end
         *  return tonumber(current);                           //返回 key 的值
         */
        return """
                local key = KEYS[1]
                local count = tonumber(ARGV[1])
                local time = tonumber(ARGV[2])
                local current = redis.call('get',key);
                if current and tonumber(current) > count then
                    return tonumber(current);
                end
                current = redis.call('incr', key)
                if tonumber(current) == 1 then
                    redis.call('expire', key, time)
                end
                return tonumber(current);
                """;
    }


    public String redisLockText(){

        return """
                local key = KEYS[1]
                local value = ARGV[1]
                local time = tonumber(ARGV[2])
                local lock = redis.call('set',key,value,'nx','px',time)
                if lock == 'OK' then
                    return 1
                end
                return 0
                """;
    }



}
