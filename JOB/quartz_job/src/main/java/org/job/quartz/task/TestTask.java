package org.job.quartz.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component("testTask")
public class TestTask {

    private static final Logger logger = LoggerFactory.getLogger(TestTask.class);

    public void timedCrawling(String sourceId){
        logger.info("农价通定时任务-爬取数据 =====> 开始执行。");
        LocalDateTime startTime = LocalDateTime.now();



        final int maxRetries = 3; //最大重试次数
        final long retryIntervalMs = 3000;  //重试间隔 3000ms = 3s
        StringBuilder remarkStrBuf = new StringBuilder();
        Exception lastException = null;
        Map<String, Object> map = null;

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                //数据爬取 返回爬取成功 并落库的条数
                map = setMap();

                //爬取成功直接 break;
                break;

            } catch (Exception e) {
                //记录错误信息 和重试次数，
                remarkStrBuf.append("爬取次数：第").append(attempt+1).append("次")
                        .append("错误信息：").append(e.getMessage()).append("\t\t\t");
                lastException = e;

                //打印所有的错误信息
                logger.error(remarkStrBuf.toString());

                // 时间间隔 延时 指定时间 间隔
                try {
                    TimeUnit.MILLISECONDS.sleep(retryIntervalMs);
                }catch (InterruptedException ie) {
                    remarkStrBuf.append("爬取次数：第").append(attempt+1).append("次")
                            .append("错误信息：").append(ie.getMessage()).append("\t\t\t");
                    lastException = ie;
                    break;
                }

            }
        }

        // 判断是否爬取成功
        if (map != null){
            // ---------- 最终成功 ----------

            //保存采集日志 （成功信息）
            logger.info("农价通定时任务-爬取数据 =====> 执行完成。");

        }else {
            // ---------- 最终失败 ----------
            //保存采集日志 （错误信息）

            logger.info(remarkStrBuf.toString());
            throw new RuntimeException(lastException);
        }








    }




    private Map<String, Object> setMap(){

        Map<String, Object> map = new HashMap<>();
        map.put("size",10);
        System.out.println("hahahahahahahahahahhahahahahahahahah");
        //int i = 1/0;

        return map;
    }
}
