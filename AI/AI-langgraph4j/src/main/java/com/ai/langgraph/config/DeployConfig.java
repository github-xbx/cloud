package com.ai.langgraph.config;

/**
 * 部署配置：服务器、数据库、LLM 等连接信息。
 * 通过配置文件 application.json 或环境变量注入。
 */
public record DeployConfig(
        // SSH 服务器配置
        String serverHost,
        int serverPort,
        String serverUser,
        String serverPassword,   // 或私钥路径
        String serverKeyPath,    // 私钥路径（可选，优先于密码）
        String remoteBaseDir,    // 服务器上的部署根目录，如 /opt/apps

        // 数据库（checkpointer 持久化）
        String dbType,           // mysql / postgresql
        String dbUrl,
        String dbUser,
        String dbPassword,

        // LLM（OpenAI 兼容）
        String llmBaseUrl,       // 如 https://api.openai.com/v1
        String llmApiKey,
        String llmModel         // 如 gpt-4o-mini / deepseek-chat
) {
    public static DeployConfig fromEnv() {
        return new DeployConfig(
                env("DEPLOY_SERVER_HOST", "127.0.0.1"),
                Integer.parseInt(env("DEPLOY_SERVER_PORT", "22")),
                env("DEPLOY_SERVER_USER", "root"),
                env("DEPLOY_SERVER_PASSWORD", ""),
                env("DEPLOY_SERVER_KEY", ""),
                env("DEPLOY_REMOTE_DIR", "/opt/apps"),
                env("DEPLOY_DB_TYPE", "mysql"),
                env("DEPLOY_DB_URL", ""),
                env("DEPLOY_DB_USER", ""),
                env("DEPLOY_DB_PASSWORD", ""),
                env("LLM_BASE_URL", ""),
                env("LLM_API_KEY", ""),
                env("LLM_MODEL", "gpt-4o-mini")
        );
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v;
    }
}
