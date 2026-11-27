package com.ai.langgraph.utils;

import com.ai.langgraph.config.DeployConfig;
import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/**
 * SSH 通道封装：负责上传文件（SCP）与远程执行命令。
 * 一次任务复用一个连接，避免频繁握手。
 */
public class SshChannel implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(SshChannel.class);

    private final DeployConfig config;
    private Session session;

    public SshChannel(DeployConfig config) {
        this.config = config;
    }

    /** 建立连接（懒加载）。 */
    private Session session() throws Exception {
        if (session == null || !session.isConnected()) {
            JSch jsch = new JSch();
            // 优先用私钥，其次密码
            if (config.serverKeyPath() != null && !config.serverKeyPath().isBlank()) {
                jsch.addIdentity(config.serverKeyPath());
            }
            session = jsch.getSession(config.serverUser(), config.serverHost(), config.serverPort());
            if (config.serverPassword() != null && !config.serverPassword().isBlank()) {
                session.setPassword(config.serverPassword());
            }
            Properties props = new Properties();
            props.put("StrictHostKeyChecking", "no");
            session.setConfig(props);
            session.connect(10_000);
            log.info("SSH 连接成功: {}@{}:{}", config.serverUser(), config.serverHost(), config.serverPort());
        }
        return session;
    }

    /** 上传本地文件到远程目录（SCP）。 */
    public void upload(String localPath, String remoteDir) throws Exception {
        Session s = session();
        boolean ptimestamp = true;

        // 确保远程目录存在
        exec("mkdir -p " + remoteDir);

        File file = new File(localPath);
        if (!file.exists()) {
            throw new FileNotFoundException("产物不存在: " + localPath);
        }
        String remotePath = remoteDir + "/" + file.getName();

        ChannelSftp sftp = (ChannelSftp) s.openChannel("sftp");
        sftp.connect(10_000);
        try {
            sftp.put(localPath, remotePath);
            log.info("上传完成: {} -> {}", localPath, remotePath);
        } finally {
            sftp.disconnect();
        }
    }

    /** 远程执行命令，返回 stdout+stderr。 */
    public String exec(String command) throws Exception {
        Session s = session();
        ChannelExec channel = (ChannelExec) s.openChannel("exec");
        channel.setCommand(command);
        channel.setInputStream(null);
        channel.setErrStream(System.err);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        channel.setOutputStream(out);
        channel.connect(10_000);

        // 等待命令结束（最长 10 分钟，适配打包/部署类长命令）
        long deadline = System.currentTimeMillis() + 600_000;
        while (!channel.isClosed() && System.currentTimeMillis() < deadline) {
            Thread.sleep(100);
        }
        int exit = channel.getExitStatus();
        channel.disconnect();

        String result = out.toString("UTF-8");
        if (exit != 0) {
            throw new RuntimeException("远程命令失败(exit=" + exit + "): " + command + "\n" + result);
        }
        return result;
    }

    @Override
    public void close() {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }
}
