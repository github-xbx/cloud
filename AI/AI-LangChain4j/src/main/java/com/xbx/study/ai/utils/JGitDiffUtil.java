package com.xbx.study.ai.utils;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

public class JGitDiffUtil {

    public static String getStagedDiff(File repoDir) throws Exception {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        builder.setGitDir(new File(repoDir, ".git"))
                .readEnvironment()
                .findGitDir();

        try (Repository repository = builder.build();
             Git git = new Git(repository)) {

            // 1. 获取 HEAD 的 TreeIterator（作为旧版本）
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            oldTreeIter.reset(
                    repository.newObjectReader(),
                    repository.resolve("HEAD^{tree}")
            );

            // 2. 空的新 TreeIterator 代表索引（暂存区）
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();

            // 3. 执行 diff：旧 = HEAD，新 = 暂存区（索引）
            List<DiffEntry> entries = git.diff()
                    .setOldTree(oldTreeIter)
                    .setNewTree(newTreeIter)
                    .setCached(true) // 关键：等价于 git diff --cached
                    .call();

            // 4. 用 DiffFormatter 格式化为统一 diff 文本
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (DiffFormatter formatter = new DiffFormatter(out)) {
                formatter.setRepository(repository);
                for (DiffEntry entry : entries) {
                    formatter.format(entry);
                }
            }

            return out.toString("UTF-8");
        }
    }
}