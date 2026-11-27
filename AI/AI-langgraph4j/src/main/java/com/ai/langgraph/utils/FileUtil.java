package com.ai.langgraph.utils;

import java.io.File;

public class FileUtil {

    public static File tarDirectory(File dir) throws Exception {
        File tar = new File(dir.getParentFile(), dir.getName() + ".tar.gz");
        CommandRunner.Result r = CommandRunner.run(dir.getParentFile(),
                "tar czf " + tar.getName() + " " + dir.getName(), 10);
        if (!r.ok()) {
            throw new RuntimeException("打包目录失败: " + r.output());
        }
        return tar;
    }

    public static void deleteRecursively(File f) {
        if (f.isDirectory()) {
            File[] children = f.listFiles();
            if (children != null) for (File c : children) deleteRecursively(c);
        }
        f.delete();
    }
}
