package com.ai.langgraph.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 多语言项目构建器：根据项目类型执行对应的构建命令，产出部署产物。
 *
 * 支持：java / ts / vue / python，均可选择 Docker 打包或直传产物两种模式。
 * Docker 模式下会额外生成 Dockerfile 并构建镜像。
 */
public class ProjectBuilder {

    private static final Logger log = LoggerFactory.getLogger(ProjectBuilder.class);

    /**
     * 执行构建，返回产物路径。
     *
     * @param projectDir 项目根目录
     * @param type       项目类型
     * @param mode       docker / direct
     * @return 产物路径（jar / dist 目录 / 源码目录），Docker 模式返回构建上下文目录
     */
    public BuildResult build(File projectDir, String type, String mode) throws Exception {
        String buildLog;
        String artifact;

        switch (type.toLowerCase()) {
            case "java" -> {
                buildLog = buildJava(projectDir);
                artifact = new File(projectDir, "target").getAbsolutePath();
            }
            case "ts", "typescript" -> {
                buildLog = buildNode(projectDir, "tsc");
                artifact = new File(projectDir, "dist").getAbsolutePath();
            }
            case "vue" -> {
                buildLog = buildNode(projectDir, "vue");
                artifact = new File(projectDir, "dist").getAbsolutePath();
            }
            case "python" -> {
                buildLog = buildPython(projectDir);
                artifact = projectDir.getAbsolutePath();
            }
            default -> throw new IllegalArgumentException("不支持的项目类型: " + type
                    + "（支持 java/ts/vue/python）");
        }

        return new BuildResult(buildLog, artifact);
    }

    private String buildJava(File dir) throws Exception {
        log.info("开始构建 Java 项目: {}", dir);
        CommandRunner.Result r;
        // 优先 Maven，回退 Gradle
        if (new File(dir, "pom.xml").exists()) {
            r = CommandRunner.run(dir, "mvn clean package -DskipTests", 30);
        } else if (new File(dir, "build.gradle").exists() || new File(dir, "build.gradle.kts").exists()) {
            r = CommandRunner.run(dir, "gradle clean build -x test", 30);
        } else {
            throw new IllegalStateException("未找到 pom.xml 或 build.gradle");
        }
        if (!r.ok()) {
            throw new BuildException("Java 构建失败", r.output());
        }
        return r.output();
    }

    private String buildNode(File dir, String flavor) throws Exception {
        log.info("开始构建 {} 项目: {}", flavor, dir);
        // 安装依赖
        CommandRunner.Result install = CommandRunner.run(dir, "npm install", 20);
        if (!install.ok()) {
            throw new BuildException("npm install 失败", install.output());
        }
        String cmd = flavor.equals("vue") ? "npm run build" : "npx tsc";
        CommandRunner.Result build = CommandRunner.run(dir, cmd, 30);
        if (!build.ok()) {
            throw new BuildException(flavor + " 构建失败", build.output());
        }
        return install.output() + "\n" + build.output();
    }

    private String buildPython(File dir) throws Exception {
        log.info("开始处理 Python 项目: {}", dir);
        StringBuilder log = new StringBuilder();
        // 可选：pip 依赖打包。默认直传源码，由部署脚本负责装依赖。
        if (new File(dir, "requirements.txt").exists()) {
            log.append("[检测到 requirements.txt，将在服务器端安装依赖]\n");
        }
        // Python 通常无需"编译"，做语法检查作为构建校验
        CommandRunner.Result check = CommandRunner.run(dir,
                "python -m compileall -q .", 5);
        if (!check.ok()) {
            throw new BuildException("Python 语法检查失败", check.output());
        }
        return log + check.output();
    }

    public record BuildResult(String buildLog, String artifactPath) {}

    public static class BuildException extends RuntimeException {
        public BuildException(String msg, String log) {
            super(msg);
            this.buildLog = log;
        }
        public final String buildLog;
    }
}
