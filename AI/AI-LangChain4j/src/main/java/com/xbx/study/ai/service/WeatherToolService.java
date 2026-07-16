package com.xbx.study.ai.service;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Service;

@Service
public class WeatherToolService {


    @Tool("指定城市的最近的天气情况")
    public String handle(@P("城市") String city, @P("最近天数") int dayNum){
        //查询天气逻辑

        return city + " beijin;"+"最近"+dayNum+"天， 晴。";
    }



}
