package ai.agui.common;

import java.time.LocalDateTime;

/**
 * 原始消息数据
 * @param role
 * @param message
 * @param runId
 * @param threadId
 * @param messageId
 */
public record OriginalMessage(String role, String message, String runId, String threadId, String messageId, LocalDateTime time) {
}
