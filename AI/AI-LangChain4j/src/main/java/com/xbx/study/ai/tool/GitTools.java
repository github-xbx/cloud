package com.xbx.study.ai.tool;

import com.xbx.study.ai.utils.JGitDiffUtil;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GitTools {


    @Tool("获取暂存区（staged）的代码变更内容（git diff）")
    public String getStagedDiff(
            @P(value = "仓库根目录路径；省略则使用当前默认仓库", required = false) String repoPath){

        try {

            if (repoPath == null || repoPath.isBlank()){
                return "没有传入目录";
            }

            File file =  validateRepo(repoPath);
            return JGitDiffUtil.getStagedDiff(file);
        } catch (Exception e) {

            return "error "+ e.getMessage();
        }

    }



    private static final Path ALLOWED_ROOT = Paths.get("/home/user/projects").toAbsolutePath();

    private File validateRepo(String repoPath) {
        Path p = Paths.get(repoPath).toAbsolutePath().normalize();

        // 1. 必须位于允许的根目录下，防止 AI 传入 /etc、~/.ssh 等
        if (!p.startsWith(ALLOWED_ROOT)) {
            throw new SecurityException("仓库路径不在允许范围内: " + p);
        }
        // 2. 必须是存在的目录
        if (!Files.isDirectory(p)) {
            throw new IllegalArgumentException("路径不是目录: " + p);
        }
        // 3. 必须是 Git 仓库（含 .git）
        if (!Files.isDirectory(p.resolve(".git"))) {
            throw new IllegalArgumentException("不是 Git 仓库: " + p);
        }
        return p.toFile();
    }

}
