package com.xbx.study.ai.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.Serializable;
import java.util.Map;

/**
 * 联网搜索
 */
@Component
public class BoChaWebSearchTool {

    @Resource
    private RestClient restClient;

    ObjectMapper objectMapper = new ObjectMapper();

    String url = "https://api.bochaai.com/v1/web-search";


    @Tool(name = "实时搜索")
    public String searchWeb(String query){

        String json;

        try {
             json = objectMapper.writeValueAsString(Map.of("query", query, "freshness", "nolimit", "summary", true));

            RestClient.ResponseSpec response = restClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + System.getenv("java_bocha_apikey"))
                    .body(json)
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve();
            String body = response.body(String.class);
            System.out.println(body);
            return body;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }




    }


}
