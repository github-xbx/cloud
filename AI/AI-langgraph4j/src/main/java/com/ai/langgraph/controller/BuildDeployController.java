package com.ai.langgraph.controller;

import com.ai.langgraph.service.langgraph.deploy.DeployState;
import com.ai.langgraph.service.langgraph.deploy.DeployWorkflow;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用法:
 *   run    --task <id> --path <路径/git> --source local|git --type java|ts|vue|python --mode docker|direct --version <v>
 *   resume --task <id>
 *   status --task <id>
 *   mermaid
 * 环境变量:
 *   DEPLOY_SERVER_HOST / PORT / USER / PASSWORD / KEY / REMOTE_DIR
 *   DEPLOY_DB_TYPE / URL / USER / PASSWORD
 *   LLM_BASE_URL / API_KEY / MODEL
 *
 *
 *
 */
@RestController
@RequestMapping("deploy")
public class BuildDeployController {


    private final DeployWorkflow deployWorkflow;


    public BuildDeployController(DeployWorkflow deployWorkflow) {
        this.deployWorkflow = deployWorkflow;
    }



    @GetMapping("run")
    public String run(
            @RequestParam("taskId")String taskId,
            @RequestParam("path")String path,
            @RequestParam("source")String source,
            @RequestParam("type")String type,
            @RequestParam("mode")String mode,
            @RequestParam("version")String version
    ){

        DeployState result = deployWorkflow.run(taskId, path, source, type, mode, version);
        printResult(result);

        return "success";
    }


    @GetMapping("resume")
    public String resume(@RequestParam("taskId") String taskId){
        DeployState resume = deployWorkflow.resume(taskId);
        printResult(resume);
        return "success";
    }

    @GetMapping("status")
    public String status(@RequestParam("taskId") String taskId){
        DeployState s = deployWorkflow.getState(taskId);
        System.out.println("任务 " + taskId + " 状态: " + s.getStatus()
                + " | 阶段: " + s.getStage()
                + " | 类型: " + s.getProjectType()
                + " | 版本: " + s.getVersion());
        if (s.getLastError() != null) {
            System.out.println("最近错误: " + s.getLastError());
        }
        return "success";
    }

    @GetMapping("mermaid")
    public String mermaid(){
        System.out.println(deployWorkflow.toMermaid());
        return deployWorkflow.toMermaid();
    }


    private static void printResult(DeployState s) {
        System.out.println("\n========== 部署结果 ==========");
        System.out.println("任务 ID   : " + s.getTaskId());
        System.out.println("最终状态  : " + s.getStatus());
        System.out.println("项目类型  : " + s.getProjectType());
        System.out.println("部署方式  : " + s.getDeployMode());
        System.out.println("版本      : " + s.getVersion());
        if (s.getLastError() != null) {
            System.out.println("错误信息  : " + s.getLastError());
        }
        Object suggestion = s.getExtra().get("llmSuggestion");
        if (suggestion != null) {
            System.out.println("LLM 建议  : " + suggestion);
        }
        System.out.println("===============================");
    }

}
