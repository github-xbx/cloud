package com.ai.langgraph.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 本地命令执行工具：用于 git、maven、npm、python 等本地构建命令。
 * 捕获 stdout/stderr 并返回，供日志记录与 LLM 分析。
 */
public final class CommandRunner {

    public record Result(int exitCode, String output) {
        public boolean ok() { return exitCode == 0; }
    }

    private CommandRunner() {}

    /** 在指定工作目录执行命令。 */
    public static Result run(File workDir, List<String> command, long timeoutMinutes) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        if (workDir != null && workDir.exists()) {
            pb.directory(workDir);
        }
        pb.redirectErrorStream(true); // 合并 stderr 到 stdout

        Process proc = pb.start();
        StringBuilder out = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line).append("\n");
            }
        }

        boolean finished = proc.waitFor(timeoutMinutes, TimeUnit.MINUTES);
        if (!finished) {
            proc.destroyForcibly();
            return new Result(-1, out + "\n[命令超时，已强制终止]");
        }
        return new Result(proc.exitValue(), out.toString());
    }

    /** 便捷方法：字符串命令。 */
    public static Result run(File workDir, String command, long timeoutMinutes) throws Exception {
        List<String> parts = new ArrayList<>();
        if (isWindows()) {
            parts.add("cmd");
            parts.add("/c");
        } else {
            parts.add("/bin/sh");
            parts.add("-c");
        }
        parts.add(command);
        return run(workDir, parts, timeoutMinutes);
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}
