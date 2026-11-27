package com.ai.langgraph.service.langgraph.deploy;

import com.ai.langgraph.service.AgentStateService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.bsc.langgraph4j.*;
import org.bsc.langgraph4j.action.AsyncEdgeAction;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class DeployWorkflow {


    private static final Logger logger = LoggerFactory.getLogger(DeployWorkflow.class);

    private final DeployNode deployNode;
    private final CompiledGraph<AgentState> graph;
    private final AgentStateService agentStateService;

    public DeployWorkflow(DeployNode deployNode, AgentStateService agentStateService) {
        this.deployNode = deployNode;
        this.agentStateService = agentStateService;
        try {
            this.graph = buildGraph();
        } catch (Exception e) {
            throw new RuntimeException("构建工作流图失败", e);
        }
    }

    private CompiledGraph<AgentState> buildGraph() throws Exception {

        // 状态 schema：一个名为 "state" 的 channel，承载 DeployState 对象
        Map<String, Channel<?>> schema = Map.of("state", Channels.base(DeployState::new));

        StateGraph<AgentState> g = new StateGraph<>(schema,AgentState::new)
                .addNode("fetch", AsyncNodeAction.node_async(state -> runNode(state, deployNode::node1fetch)))
                .addNode("build", AsyncNodeAction.node_async(state -> runNode(state, deployNode::node2build)))
                .addNode("upload", AsyncNodeAction.node_async(state -> runNode(state, deployNode::node3upload)))
                .addNode("deploy", AsyncNodeAction.node_async(state -> runNode(state, deployNode::node4deploy)))
                .addNode("verify", AsyncNodeAction.node_async(state -> runNode(state, deployNode::node5verify)))
                .addNode("rollback", AsyncNodeAction.node_async(state -> runNode(state, deployNode::node6rollback)))
                .addNode("llmAnalyze", AsyncNodeAction.node_async(state -> runNode(state, deployNode::node7llm)))
                .addNode("notify", AsyncNodeAction.node_async(state -> runNode(state, deployNode::node8notify)))
                // 入口：START → fetch
                .addEdge(StateGraph.START, "fetch")
                .addEdge("fetch", "build")
                // build 之后：成功→upload，失败→llmAnalyze
                .addConditionalEdges("build",
                        AsyncEdgeAction.edge_async(this::routeAfterBuild),
                        Map.of("upload", "upload", "llmAnalyze", "llmAnalyze"))
                .addEdge("upload", "deploy")
                // deploy 之后：成功→verify，失败→rollback
                .addConditionalEdges("deploy",
                        AsyncEdgeAction.edge_async(this::routeAfterDeploy),
                        Map.of("verify", "verify", "rollback", "rollback"))
                // verify 之后：健康→notify，不健康→rollback
                .addConditionalEdges("verify",
                        AsyncEdgeAction.edge_async(this::routeAfterVerify),
                        Map.of("notify", "notify", "rollback", "rollback"))
                .addEdge("rollback", "notify")
                .addEdge("llmAnalyze", "notify")
                // 终点：notify → END
                .addEdge("notify", StateGraph.END);

        return g.compile(CompileConfig.builder()
                .checkpointSaver(new MemorySaver())
                .build());

    }


    // ===== 节点包装：把 DeployState 装进/取出 AgentState =====

    private Map<String, Object> runNode(AgentState agentState,
                                        java.util.function.Function<DeployState, DeployState> fn) {
        DeployState s = agentState.value("state")
                .map(v -> (DeployState) v)
                .orElse(new DeployState());
        DeployState result = fn.apply(s);
        // 每步落库（业务状态跨进程持久化）
        agentStateService.save(result);
        //checkpointer.save(result);
        Map<String, Object> update = new HashMap<>();
        update.put("state", result);
        return update;
    }

    // ===== 条件路由（同步 EdgeAction）=====

    private String routeAfterBuild(AgentState state) {
        DeployState s = read(state);
        return "FAILED".equals(s.getStatus()) ? "llmAnalyze" : "upload";
    }

    private String routeAfterDeploy(AgentState state) {
        DeployState s = read(state);
        return "FAILED".equals(s.getStatus()) ? "rollback" : "verify";
    }

    private String routeAfterVerify(AgentState state) {
        DeployState s = read(state);
        return "SUCCESS".equals(s.getStatus()) ? "notify" : "rollback";
    }

    private DeployState read(AgentState state) {
        return state.value("state").map(v -> (DeployState) v).orElse(new DeployState());
    }


    // ===== 对外执行入口 =====

    /**
     * 启动一次部署任务。
     *
     * @param taskId      任务 ID（同时作为 threadId，是断点续跑的钥匙）
     * @param projectPath 本地项目路径或 git 地址
     * @param sourceType  local / git
     * @param projectType java / ts / vue / python
     * @param deployMode  docker / direct
     * @param version     版本号
     */
    public DeployState run(String taskId, String projectPath, String sourceType,
                           String projectType, String deployMode, String version) {
        validateInputs(taskId, projectPath, sourceType, projectType, deployMode);
        DeployState init = new DeployState()
                .withId(taskId)
                .withProject(projectPath, sourceType, projectType, deployMode, version);
        return execute(taskId, init);
    }

    /**
     * 对外部输入做白名单校验，防止 shell 命令注入。
     * taskId 会拼进远程命令（如容器名 app_<taskId>），projectPath 会拼进 git clone。
     */
    private void validateInputs(String taskId, String projectPath, String sourceType,
                                String projectType, String deployMode) {
        // taskId 只允许字母/数字/下划线/连字符，杜绝 shell 元字符注入
        if (taskId == null || !taskId.matches("[A-Za-z0-9_-]{1,64}")) {
            throw new IllegalArgumentException("非法的 taskId：" + taskId);
        }
        // sourceType / projectType / deployMode 走白名单
        if (sourceType == null || !Set.of("local", "git").contains(sourceType.toLowerCase())) {
            throw new IllegalArgumentException("非法的 sourceType：" + sourceType);
        }
        if (projectType == null || !Set.of("java", "ts", "typescript", "vue", "python").contains(projectType.toLowerCase())) {
            throw new IllegalArgumentException("非法的 projectType：" + projectType);
        }
        if (deployMode == null || !Set.of("docker", "direct").contains(deployMode.toLowerCase())) {
            throw new IllegalArgumentException("非法的 deployMode：" + deployMode);
        }
        // git 地址：只允许 http(s)/ssh 协议 URL，禁止 shell 元字符（空格、分号、|、&、$、` 等）
        if ("git".equalsIgnoreCase(sourceType)) {
            if (projectPath == null
                    || !projectPath.matches("^(https?|git|ssh)://[A-Za-z0-9._@:/~-]+$")
                            && !projectPath.matches("^[A-Za-z0-9._-]+@[A-Za-z0-9._-]+:[A-Za-z0-9._/ -]+$")) {
                throw new IllegalArgumentException("非法的 git 仓库地址：" + projectPath);
            }
        }
    }

    /**
     * 断点续跑入口：从数据库恢复指定任务的状态，从上次失败节点继续。
     */
    public DeployState resume(String taskId) {
        DeployState saved = agentStateService.getOne(Wrappers.<DeployState>lambdaQuery().eq(DeployState::getTaskId, taskId));
        //DeployState saved = checkpointer.load(taskId);
        if (saved == null) {
            throw new IllegalArgumentException("未找到任务 " + taskId + " 的检查点，无法续跑");
        }
        logger.info("[{}] 从检查点续跑，上次停留节点: {}", taskId, saved.getStage());
        // 清空失败标记，让流程从断点处继续
        if ("FAILED".equals(saved.getStatus())) {
            saved.setStatus("PENDING");
        }
        return execute(taskId, saved);
    }

    private DeployState execute(String threadId, DeployState initial) {
        // 关键：threadId = taskId，保证同一任务多次调用共享 checkpoint
        RunnableConfig cfg = RunnableConfig.builder().threadId(threadId).build();

        Map<String, Object> input = new HashMap<>();
        input.put("state", initial);

        Optional<AgentState> result = graph.invoke(input, cfg);
        return result.map(as -> as.<DeployState>value("state").orElse(initial))
                .orElse(initial);
    }

    /** 查询任务当前状态（用于流程可视化）。 */
    public DeployState getState(String taskId) {
        DeployState s = agentStateService.getOne(Wrappers.<DeployState>lambdaQuery().eq(DeployState::getTaskId, taskId));
        //DeployState s = checkpointer.load(taskId);
        return s != null ? s : new DeployState();
    }

    /** 获取图的 Mermaid 表示，用于流程可视化。 */
    public String toMermaid() {
        try {
            GraphRepresentation rep = graph.getGraph(GraphRepresentation.Type.MERMAID);
            return rep.getContent();
        } catch (Exception e) {
            logger.warn("生成 Mermaid 失败: {}", e.getMessage());
            return "";
        }
    }


}
