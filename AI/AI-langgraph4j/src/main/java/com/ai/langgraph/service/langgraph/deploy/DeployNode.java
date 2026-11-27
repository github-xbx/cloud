package com.ai.langgraph.service.langgraph.deploy;

import com.ai.langgraph.config.DeployConfig;
import com.ai.langgraph.service.langchain.DeployLLMService;
import com.ai.langgraph.utils.CommandRunner;
import com.ai.langgraph.utils.CommandRunner.Result;
import com.ai.langgraph.utils.FileUtil;
import com.ai.langgraph.utils.ProjectBuilder;
import com.ai.langgraph.utils.SshChannel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Map;

/**
 * 工作流节点集合：每个节点是一个纯函数，输入/输出 DeployState。
 * 由 LangGraph4j 的 StateGraph 编排。
 *
 * 节点清单：
 *  - fetch   拉取代码（本地 or git clone）
 *  - build   本地构建打包
 *  - upload  上传产物到服务器
 *  - deploy  远程部署（备份旧版 + 启动）
 *  - verify  健康检查验证
 *  - rollback 失败回滚
 *  - llmAnalyze LLM 分析失败原因（条件触发）
 *  - notify  通知 + 记录结果
 */
@Service
public class DeployNode {

    private final static Logger logger = LoggerFactory.getLogger(DeployNode.class);
    private final ProjectBuilder builder = new ProjectBuilder();
    private final DeployConfig config = DeployConfig.fromEnv();

    private final DeployLLMService deployLLMService;

    public DeployNode(DeployLLMService deployLLMService){
        this.deployLLMService = deployLLMService;
    }

    // ===== 工具方法 =====



    private static final ObjectMapper JSON = new ObjectMapper();

    private static String toJson(DeployState s) {
        try {
            Map<String, Object> m = Map.of(
                    "taskId", s.getTaskId() == null ? "" : s.getTaskId(),
                    "status", s.getStatus() == null ? "" : s.getStatus(),
                    "projectType", s.getProjectType() == null ? "" : s.getProjectType(),
                    "deployMode", s.getDeployMode() == null ? "" : s.getDeployMode(),
                    "version", s.getVersion() == null ? "" : s.getVersion(),
                    "suggestion", s.getExtra().get("llmSuggestion") == null ? "" : s.getExtra().get("llmSuggestion")
            );
            return JSON.writeValueAsString(m);
        } catch (Exception e) {
            logger.warn("序列化结果 JSON 失败: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * 节点1 拉取代码
     *
     */
    public DeployState node1fetch(DeployState state){
        logger.info("[{}] 开始拉取代码", state.getTaskId());
        state.advance("FETCHING", "fetch");
        try {
            if ("git".equalsIgnoreCase(state.getSourceType())) {
                // git 拉取：克隆到临时工作目录
                File workDir = new File("workspace/" + state.getTaskId());
                if (workDir.exists()) {
                    FileUtil.deleteRecursively(workDir);
                }
                workDir.mkdirs();
                Result r = CommandRunner.run(null,
                        "git clone " + state.getProjectPath() + " " + workDir.getAbsolutePath(), 10);
                if (!r.ok()) {
                    throw new RuntimeException("git clone 失败: " + r.output());
                }
                // 更新 projectPath 指向克隆后的本地目录
                state.setProjectPath(workDir.getAbsolutePath());
            }
            // local 类型：直接使用本地路径，仅做存在性校验
            File local = new File(state.getProjectPath());
            if (!local.exists()) {
                throw new RuntimeException("本地项目路径不存在: " + state.getProjectPath());
            }
            logger.info("[{}] 代码就绪: {}", state.getTaskId(), state.getProjectPath());
            state.advance("BUILDING", "build");
            return state;
        } catch (Exception e) {
            state.fail("fetch", e.getMessage());
            state.getExtra().put("llmHint", "[拉取失败，无需 LLM 分析]");
            return state;
        }

    }

    /**
     * 节点2 构建代码
     */
    public DeployState node2build(DeployState state){
        logger.info("[{}] 开始构建", state.getTaskId());
        state.advance("BUILDING", "build");
        try {
            File dir = new File(state.getProjectPath());
            ProjectBuilder.BuildResult result = builder.build(dir, state.getProjectType(), state.getDeployMode());
            state.setBuildLog(result.buildLog());
            state.setArtifactPath(result.artifactPath());
            logger.info("[{}] 构建完成，产物: {}", state.getTaskId(), state.getArtifactPath());
            state.advance("UPLOADING", "upload");
            return state;
        } catch (ProjectBuilder.BuildException e) {
            state.setBuildLog(e.buildLog);
            state.fail("build", e.getMessage());
            // 构建失败 → 标记需要 LLM 分析
            state.getExtra().put("needLlm", true);
            return state;
        } catch (Exception e) {
            state.fail("build", e.getMessage());
            state.getExtra().put("needLlm", true);
            return state;
        }
    }

    /**
     * 节点3 上传构建产物到服务器
     */
    public DeployState node3upload(DeployState state){

        logger.info("[{}] 开始上传产物", state.getTaskId());
        state.advance("UPLOADING", "upload");
        try (SshChannel ssh = new SshChannel(config)) {
            String remoteDir = config.remoteBaseDir() + "/" + state.getTaskId();
            // 直接上传产物目录；若为目录则打包成 tar 再传
            File artifact = new File(state.getArtifactPath());
            if (artifact.isDirectory()) {
                File tar = FileUtil.tarDirectory(artifact);
                ssh.upload(tar.getAbsolutePath(), remoteDir);
                state.setRemoteArtifactPath(remoteDir + "/" + tar.getName());
                // 远程解压
                ssh.exec("cd " + remoteDir + " && tar xzf " + tar.getName() + " && rm " + tar.getName());
            } else {
                ssh.upload(state.getArtifactPath(), remoteDir);
                state.setRemoteArtifactPath(remoteDir + "/" + artifact.getName());
            }
            logger.info("[{}] 上传完成: {}", state.getTaskId(), state.getRemoteArtifactPath());
            state.advance("DEPLOYING", "deploy");
            return state;
        } catch (Exception e) {
            state.fail("upload", e.getMessage());
            return state;
        }

    }

    /**
     * 节点4 远程部署
     *
     */
    public DeployState node4deploy(DeployState state){
        logger.info("[{}] 开始远程部署", state.getTaskId());
        state.advance("DEPLOYING", "deploy");
        try (SshChannel ssh = new SshChannel(config)) {
            String deployDir = config.remoteBaseDir() + "/" + state.getTaskId() + "/app";
            // 1. 备份旧版本
            String backup = "";
            String check = ssh.exec("ls " + deployDir + " 2>/dev/null && echo EXISTS || echo EMPTY");
            if (check.contains("EXISTS")) {
                backup = config.remoteBaseDir() + "/" + state.getTaskId() + "/backup_" + System.currentTimeMillis();
                ssh.exec("cp -r " + deployDir + " " + backup);
                state.setBackupVersion(backup);
                logger.info("[{}] 已备份旧版本到 {}", state.getTaskId(), backup);
            }
            // 2. 放置新产物
            ssh.exec("mkdir -p " + deployDir);
            ssh.exec("rm -rf " + deployDir + "/*");
            String src = state.getRemoteArtifactPath();
            ssh.exec("cp -r " + src + "/* " + deployDir + "/ 2>/dev/null || cp -r " + src + " " + deployDir + "/");

            // 3. 启动服务（根据部署方式）
            if ("docker".equalsIgnoreCase(state.getDeployMode())) {
                ssh.exec("cd " + deployDir + " && (docker compose up -d --build || docker build -t app_" + state.getTaskId() + " . && docker run -d --name app_" + state.getTaskId() + " app_" + state.getTaskId() + ")");
            } else {
                // 直传模式：根据项目类型启动
                String startCmd = switch (state.getProjectType().toLowerCase()) {
                    case "java" -> "nohup java -jar $(ls " + deployDir + "/*.jar | head -1) > " + deployDir + "/app.log 2>&1 &";
                    case "ts", "vue" -> "cd " + deployDir + " && nohup npx serve -s . -l 8080 > app.log 2>&1 &";
                    case "python" -> "cd " + deployDir + " && (pip install -r requirements.txt 2>/dev/null; nohup python app.py > app.log 2>&1 &)";
                    default -> throw new RuntimeException("未知启动方式");
                };
                ssh.exec(startCmd);
            }
            logger.info("[{}] 部署完成", state.getTaskId());
            state.advance("VERIFYING", "verify");
            return state;
        } catch (Exception e) {
            state.fail("deploy", e.getMessage());
            state.getExtra().put("needRollback", true);
            return state;
        }
    }

    /**
     * 节点5 健康检查验证
     */
    public DeployState node5verify(DeployState state){
        logger.info("[{}] 开始部署验证", state.getTaskId());
        state.advance("VERIFYING", "verify");
        try (SshChannel ssh = new SshChannel(config)) {
            String checkCmd;
            if ("docker".equalsIgnoreCase(state.getDeployMode())) {
                checkCmd = "docker ps | grep app_" + state.getTaskId() + " && echo HEALTHY || echo UNHEALTHY";
            } else {
                checkCmd = "curl -sf http://127.0.0.1:8080/ >/dev/null && echo HEALTHY || echo UNHEALTHY";
            }
            // 轮询探活（最多 60 秒）
            String result = "UNHEALTHY";
            for (int i = 0; i < 12; i++) {
                result = ssh.exec(checkCmd);
                if (result.contains("HEALTHY")) break;
                Thread.sleep(5000);
            }
            if (result.contains("HEALTHY")) {
                logger.info("[{}] 验证通过", state.getTaskId());
                state.advance("SUCCESS", "verify");
                return state;
            } else {
                state.fail("verify", "健康检查未通过");
                state.getExtra().put("needRollback", true);
                state.getExtra().put("needLlm", true);
                return state;
            }
        } catch (Exception e) {
            state.fail("verify", e.getMessage());
            state.getExtra().put("needRollback", true);
            state.getExtra().put("needLlm", true);
            return state;
        }
    }

    /**
     * 节点6 失败回滚
     */
    public DeployState node6rollback(DeployState state){
        logger.info("[{}] 开始回滚", state.getTaskId());
        try (SshChannel ssh = new SshChannel(config)) {
            if (state.getBackupVersion() != null && !state.getBackupVersion().isBlank()) {
                String deployDir = config.remoteBaseDir() + "/" + state.getTaskId() + "/app";
                ssh.exec("rm -rf " + deployDir + " && cp -r " + state.getBackupVersion() + " " + deployDir);
                // 重启旧版本
                if ("docker".equalsIgnoreCase(state.getDeployMode())) {
                    ssh.exec("cd " + deployDir + " && docker compose up -d");
                }
                state.advance("ROLLED_BACK", "rollback");
                logger.info("[{}] 已回滚到旧版本: {}", state.getTaskId(), state.getBackupVersion());
            } else {
                state.advance("ROLLED_BACK", "rollback");
                logger.warn("[{}] 无备份版本，仅清理", state.getTaskId());
            }
            return state;
        } catch (Exception e) {
            // 回滚也失败：保留失败状态与错误信息，而不是用 ROLLED_BACK 覆盖掉
            state.fail("rollback", "回滚失败: " + e.getMessage());
            return state;
        }
    }

    /**
     * 节点7 大模型解析失败原因
     */
    public DeployState node7llm(DeployState state){
        logger.info("[{}] LLM 分析失败原因", state.getTaskId());
        String suggestion;
        // 用 stage 判断失败发生在哪一步：build 失败走构建分析，否则走部署分析。
        // 注意：build 节点失败时 fail("build", ...) 会把 stage 置为 "build"；
        //      部署/验证失败时 stage 为 "deploy"/"verify"，且 buildLog 为空。
        boolean isBuildFailure = "build".equals(state.getStage())
                || (state.getBuildLog() != null && !state.getBuildLog().isBlank());
        if (isBuildFailure) {
            suggestion = deployLLMService.analyzeBuildFailure(state.getProjectType(), state.getBuildLog());
        } else {
            suggestion = deployLLMService.analyzeDeployFailure(state.getProjectType(), state.getLastError());
        }
        state.getExtra().put("llmSuggestion", suggestion);
        return state;
    }

    /**
     * 节点8 通知+记录结果
     */
    public DeployState node8notify(DeployState state){
        logger.info("[{}] 流程结束，最终状态: {}", state.getTaskId(), state.getStatus());
        // 可接入钉钉/企微/webhook，这里打印并写入结果文件
        try {
            File resultDir = new File("results");
            resultDir.mkdirs();
            java.nio.file.Files.writeString(
                    new File(resultDir, state.getTaskId() + ".json").toPath(),
                    toJson(state));
        } catch (Exception e) {
            logger.warn("写结果文件失败: {}", e.getMessage());
        }
        return state;
    }
}
