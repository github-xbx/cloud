# CopilotKit AG-UI 协议接口文档

> 基于 `@copilotkit/vue` v1.63.1 / `@ag-ui/core` v0.0.57  
> 后端需要实现以下 HTTP 接口，并按协议格式返回 SSE (Server-Sent Events)

---

## 目录

1. [HTTP 接口总览](#1-http-接口总览)
2. [GET /info — 运行时信息](#2-get-info--运行时信息)
3. [POST /agent/{agentId}/run — 执行 Agent（核心接口）](#3-post-agentagentidrun--执行-agent核心接口)
4. [GET /threads — 线程列表](#4-get-threads--线程列表)
5. [SSE 事件类型定义](#5-sse-事件类型定义)
   - [5.1 RUN_STARTED — 运行开始](#51-run_started)
   - [5.2 TEXT_MESSAGE_START — 文本消息开始](#52-text_message_start)
   - [5.3 TEXT_MESSAGE_CONTENT — 文本消息内容](#53-text_message_content)
   - [5.4 TEXT_MESSAGE_END — 文本消息结束](#54-text_message_end)
   - [5.5 REASONING_MESSAGE_START — 推理消息开始](#55-reasoning_message_start)
   - [5.6 REASONING_MESSAGE_CONTENT — 推理消息内容](#56-reasoning_message_content)
   - [5.7 REASONING_MESSAGE_END — 推理消息结束](#57-reasoning_message_end)
   - [5.8 TOOL_CALL_START — 工具调用开始](#58-tool_call_start)
   - [5.9 TOOL_CALL_ARGS — 工具调用参数](#59-tool_call_args)
   - [5.10 TOOL_CALL_END — 工具调用结束](#510-tool_call_end)
   - [5.11 TOOL_CALL_RESULT — 工具调用结果](#511-tool_call_result)
   - [5.12 STATE_SNAPSHOT — 状态快照](#512-state_snapshot)
   - [5.13 STATE_DELTA — 状态增量](#513-state_delta)
   - [5.14 RUN_FINISHED — 运行结束](#514-run_finished)
   - [5.15 RUN_ERROR — 运行错误](#515-run_error)
6. [完整 SSE 响应示例](#6-完整-sse-响应示例)
7. [消息类型定义（请求体中的 messages）](#7-消息类型定义)

---

## 1. HTTP 接口总览

| 方法 | 路径 | 说明 | 必实现 |
|------|------|------|--------|
| GET | `/info` | 返回 Agent 列表和能力信息 | ✅ 是 |
| POST | `/agent/{agentId}/run` | 执行 Agent，返回 SSE 流 | ✅ 是 |
| GET | `/threads?agentId={agentId}` | 返回线程列表 | ⚠️ 建议 |

> **SSE 格式要求：**
> - `Content-Type: text/event-stream`
> - 每个事件格式：`data:{JSON}\n\n`（双换行分隔）
> - 示例：`data:{"type":"RUN_STARTED","threadId":"...","runId":"..."}\n\n`

---

## 2. GET /info — 运行时信息

### 响应格式

```json
{
    "a2uiEnabled": false,
    "agents": {
        "default": {
            "name": "默认Agent",
            "description": "通用智能助手"
        }
    }
}
```

### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `a2uiEnabled` | boolean | 否 | 是否启用 A2UI 功能，默认 false |
| `agents` | object(Map) | ✅ | **以 agentId 为 key 的对象**，非数组！ |
| `agents.{id}.name` | string | 否 | Agent 显示名称 |
| `agents.{id}.description` | string | 否 | Agent 描述信息 |

> ⚠️ **关键：`agents` 必须是 JSON 对象（Map），不是数组！**
> 
> ❌ 错误：`"agents": [{ "id": "default", ... }]`（数组）
> ✅ 正确：`"agents": { "default": { ... } }`（对象，key 为 agentId）

---

## 3. POST /agent/{agentId}/run — 执行 Agent（核心接口）

### 请求格式

前端发送 `POST` 请求，`Content-Type: application/json`：

```json
{
    "threadId": "daa656f0-1739-4511-b6b0-5bca2b695f94",
    "runId": "121dc74d-91ea-48a3-8f37-de44c17f3caa",
    "tools": [],
    "context": [],
    "forwardedProps": {},
    "state": {},
    "messages": [
        {
            "id": "11180e70-d576-4b8a-b399-ead302d6c2aa",
            "role": "user",
            "content": "你好"
        }
    ]
}
```

### 请求体字段

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `threadId` | string | ✅ | 会话线程 ID（UUID） |
| `runId` | string | ✅ | 本次运行的唯一 ID（UUID） |
| `parentRunId` | string | 否 | 父运行 ID，用于嵌套调用 |
| `state` | any | ✅ | 当前会话状态，可以为 `{}` |
| `messages` | Message[] | ✅ | 消息列表，见[消息类型定义](#7-消息类型定义) |
| `tools` | Tool[] | ✅ | 可用工具列表，可以为 `[]` |
| `context` | Context[] | ✅ | 上下文信息，可以为 `[]` |
| `forwardedProps` | any | ✅ | 转发属性，可以为 `{}` |
| `resume` | ResumeEntry[] | 否 | 中断恢复数据 |

### 响应格式

返回 **SSE (Server-Sent Events)** 流，`Content-Type: text/event-stream`。

每个事件一行 JSON，格式：`data:{JSON内容}\n\n`

所有事件类型见[第 5 节](#5-sse-事件类型定义)。

---

## 4. GET /threads — 线程列表

### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `agentId` | string | ✅ | Agent ID |
| `includeArchived` | string | 否 | `"true"` 时包含已归档线程 |
| `limit` | string | 否 | 每页数量 |

### 请求示例

```
GET /api/copilotkit/threads?agentId=default
```

### 响应格式

```json
{
    "threads": [
        {
            "id": "thread_001",
            "name": "可选标题",
            "createdAt": "2026-07-21T10:00:00.000Z",
            "updatedAt": "2026-07-21T10:30:00.000Z",
            "lastRunAt": "2026-07-21T10:30:00.000Z",
            "archived": false
        }
    ],
    "joinCode": null,
    "nextCursor": null
}
```

### 线程对象字段

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | string | ✅ | 线程唯一 ID |
| `name` | string | 否 | 线程显示名称 |
| `createdAt` | string | 否 | 创建时间（ISO 8601），用于排序 |
| `updatedAt` | string | 否 | 更新时间，排序优先级高于 createdAt |
| `lastRunAt` | string | 否 | 最后运行时间，排序优先级最高 |
| `archived` | boolean | 否 | 是否已归档 |

### 最小可用响应（暂不需要线程管理时）

```json
{ "threads": [], "joinCode": null, "nextCursor": null }
```

---

## 5. SSE 事件类型定义

### 5.1 RUN_STARTED

运行开始，必须是 SSE 流的**第一个事件**。

```json
{
    "type": "RUN_STARTED",
    "threadId": "daa656f0-...",
    "runId": "121dc74d-..."
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `type` | string | ✅ | 固定值 `"RUN_STARTED"` |
| `threadId` | string | ✅ | 会话线程 ID |
| `runId` | string | ✅ | 本次运行的唯一 ID |
| `parentRunId` | string | 否 | 父运行 ID |
| `input` | object | 否 | 原始输入数据，一般不需要返回 |

---

### 5.2 TEXT_MESSAGE_START

文本消息开始。

```json
{
    "type": "TEXT_MESSAGE_START",
    "messageId": "msg-001",
    "role": "assistant"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `type` | string | ✅ | 固定值 `"TEXT_MESSAGE_START"` |
| `messageId` | string | ✅ | 消息唯一 ID（UUID） |
| `role` | string | ✅ | 消息角色，通常为 `"assistant"` |
| `name` | string | 否 | 消息发送者名称 |

> `role` 可选值：`"developer"` / `"system"` / `"assistant"` / `"user"`  
> 未填时默认为 `"assistant"`

---

### 5.3 TEXT_MESSAGE_CONTENT

文本消息内容（流式增量）。

```json
{
    "type": "TEXT_MESSAGE_CONTENT",
    "messageId": "msg-001",
    "delta": "你好！"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `type` | string | ✅ | 固定值 `"TEXT_MESSAGE_CONTENT"` |
| `messageId` | string | ✅ | 对应 TEXT_MESSAGE_START 的 messageId |
| `delta` | string | ✅ | 增量文本内容 |

> ⚠️ 注意：字段名是 `delta`，不是 `content`！

---

### 5.4 TEXT_MESSAGE_END

文本消息结束。

```json
{
    "type": "TEXT_MESSAGE_END",
    "messageId": "msg-001"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `type` | string | ✅ | 固定值 `"TEXT_MESSAGE_END"` |
| `messageId` | string | ✅ | 对应 TEXT_MESSAGE_START 的 messageId |

---

### 5.5 REASONING_MESSAGE_START

推理/思考消息开始。

```json
{
    "type": "REASONING_MESSAGE_START",
    "messageId": "reasoning-001",
    "role": "reasoning"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `type` | string | ✅ | 固定值 `"REASONING_MESSAGE_START"` |
| `messageId` | string | ✅ | 消息唯一 ID |
| `role` | string | ✅ | **必须为固定值 `"reasoning"`** |

> ⚠️ `role` 不能省略，必须写 `"role": "reasoning"`

---

### 5.6 REASONING_MESSAGE_CONTENT

推理消息内容（流式增量）。

```json
{
    "type": "REASONING_MESSAGE_CONTENT",
    "messageId": "reasoning-001",
    "delta": "正在思考..."
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `type` | string | ✅ | 固定值 `"REASONING_MESSAGE_CONTENT"` |
| `messageId` | string | ✅ | 对应 REASONING_MESSAGE_START 的 messageId |
| `delta` | string | ✅ | 增量推理内容 |

---

### 5.7 REASONING_MESSAGE_END

推理消息结束。

```json
{
    "type": "REASONING_MESSAGE_END",
    "messageId": "reasoning-001"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `type` | string | ✅ | 固定值 `"REASONING_MESSAGE_END"` |
| `messageId` | string | ✅ | 对应 REASONING_MESSAGE_START 的 messageId |

---

### 5.8 TOOL_CALL_START

工具调用开始。

```json
{
    "type": "TOOL_CALL_START",
    "toolCallId": "tool-001",
    "toolCallName": "search_knowledge"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `type` | string | ✅ | 固定值 `"TOOL_CALL_START"` |
| `toolCallId` | string | ✅ | 工具调用唯一 ID |
| `toolCallName` | string | ✅ | 工具名称 |
| `parentMessageId` | string | 否 | 触发此工具调用的父消息 ID |

> ⚠️ 字段名是 `toolCallName`，不是 `toolName`！

---

### 5.9 TOOL_CALL_ARGS

工具调用参数（流式增量）。

```json
{
    "type": "TOOL_CALL_ARGS",
    "toolCallId": "tool-001",
    "delta": "{\"query\": \"你好\"}"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `type` | string | ✅ | 固定值 `"TOOL_CALL_ARGS"` |
| `toolCallId` | string | ✅ | 对应 TOOL_CALL_START 的 toolCallId |
| `delta` | string | ✅ | 增量参数 JSON 字符串 |

> ⚠️ 字段名是 `delta`，不是 `args`！

---

### 5.10 TOOL_CALL_END

工具调用结束（参数传输完成，等待结果）。

```json
{
    "type": "TOOL_CALL_END",
    "toolCallId": "tool-001"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `type` | string | ✅ | 固定值 `"TOOL_CALL_END"` |
| `toolCallId` | string | ✅ | 对应 TOOL_CALL_START 的 toolCallId |

---

### 5.11 TOOL_CALL_RESULT

工具调用结果返回。

```json
{
    "type": "TOOL_CALL_RESULT",
    "messageId": "tool-result-001",
    "toolCallId": "tool-001",
    "content": "找到了相关信息..."
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `type` | string | ✅ | 固定值 `"TOOL_CALL_RESULT"` |
| `messageId` | string | ✅ | **必填！** 工具结果消息的唯一 ID（UUID） |
| `toolCallId` | string | ✅ | 对应 TOOL_CALL_START 的 toolCallId |
| `content` | string | ✅ | 工具执行结果内容 |
| `role` | string | 否 | 固定值 `"tool"`，可不填 |

> ⚠️ `messageId` 必填不能省略；内容字段是 `content`，不是 `result`！

---

### 5.12 STATE_SNAPSHOT

状态快照（覆盖整个 state）。

```json
{
    "type": "STATE_SNAPSHOT",
    "snapshot": {
        "status": "processing",
        "threadId": "xxx"
    }
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `type` | string | ✅ | 固定值 `"STATE_SNAPSHOT"` |
| `snapshot` | any | ✅ | 完整的状态对象 |

---

### 5.13 STATE_DELTA

状态增量更新（JSON Patch 格式）。

```json
{
    "type": "STATE_DELTA",
    "delta": [
        { "op": "replace", "path": "/status", "value": "completed" }
    ]
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `type` | string | ✅ | 固定值 `"STATE_DELTA"` |
| `delta` | array | ✅ | JSON Patch 操作数组 |

---

### 5.14 RUN_FINISHED

运行结束，必须是 SSE 流的**最后一个事件**。

```json
{
    "type": "RUN_FINISHED",
    "threadId": "xxx",
    "runId": "xxx"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `type` | string | ✅ | 固定值 `"RUN_FINISHED"` |
| `threadId` | string | ✅ | 会话线程 ID |
| `runId` | string | ✅ | 本次运行的唯一 ID |
| `result` | any | 否 | 运行结果数据 |
| `outcome` | string | 否 | 运行结果类型：`"success"` 或 `"interrupt"` |

---

### 5.15 RUN_ERROR

运行出错。

```json
{
    "type": "RUN_ERROR",
    "message": "发生错误：请求超时",
    "code": "TIMEOUT"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `type` | string | ✅ | 固定值 `"RUN_ERROR"` |
| `message` | string | ✅ | 错误描述信息 |
| `code` | string | 否 | 错误代码 |

---

## 6. 完整 SSE 响应示例

### 6.1 纯文本回复（最简）

```
data:{"type":"RUN_STARTED","runId":"r1","threadId":"t1"}

data:{"type":"TEXT_MESSAGE_START","messageId":"msg1","role":"assistant"}

data:{"type":"TEXT_MESSAGE_CONTENT","messageId":"msg1","delta":"你好！有什么可以帮助你的？"}

data:{"type":"TEXT_MESSAGE_END","messageId":"msg1"}

data:{"type":"RUN_FINISHED","runId":"r1","threadId":"t1"}
```

### 6.2 带推理过程 + 文本回复

```
data:{"type":"RUN_STARTED","runId":"r1","threadId":"t1"}

data:{"type":"REASONING_MESSAGE_START","messageId":"reason1","role":"reasoning"}

data:{"type":"REASONING_MESSAGE_CONTENT","messageId":"reason1","delta":"正在分析用户问题..."}

data:{"type":"REASONING_MESSAGE_CONTENT","messageId":"reason1","delta":"确定需要给出友好问候"}

data:{"type":"REASONING_MESSAGE_END","messageId":"reason1"}

data:{"type":"TEXT_MESSAGE_START","messageId":"msg1","role":"assistant"}

data:{"type":"TEXT_MESSAGE_CONTENT","messageId":"msg1","delta":"你好！有什么可以帮助你的？"}

data:{"type":"TEXT_MESSAGE_END","messageId":"msg1"}

data:{"type":"RUN_FINISHED","runId":"r1","threadId":"t1"}
```

### 6.3 带推理 + 工具调用 + 文本回复

```
data:{"type":"RUN_STARTED","runId":"r1","threadId":"t1"}

data:{"type":"REASONING_MESSAGE_START","messageId":"reason1","role":"reasoning"}

data:{"type":"REASONING_MESSAGE_CONTENT","messageId":"reason1","delta":"用户问了一个需要查资料的问题..."}

data:{"type":"REASONING_MESSAGE_CONTENT","messageId":"reason1","delta":"我需要调用搜索工具"}

data:{"type":"REASONING_MESSAGE_END","messageId":"reason1"}

data:{"type":"TOOL_CALL_START","toolCallId":"tool1","toolCallName":"search_knowledge"}

data:{"type":"TOOL_CALL_ARGS","toolCallId":"tool1","delta":"{\"query\": \"你好\"}"}

data:{"type":"TOOL_CALL_END","toolCallId":"tool1"}

data:{"type":"TOOL_CALL_RESULT","messageId":"tool-result-1","toolCallId":"tool1","content":"找到相关信息：问候语相关资料..."}

data:{"type":"TEXT_MESSAGE_START","messageId":"msg1","role":"assistant"}

data:{"type":"TEXT_MESSAGE_CONTENT","messageId":"msg1","delta":"根据查询结果，你好是常见的问候语..."}

data:{"type":"TEXT_MESSAGE_END","messageId":"msg1"}

data:{"type":"STATE_DELTA","delta":[{"op":"replace","path":"/status","value":"completed"}]}

data:{"type":"RUN_FINISHED","runId":"r1","threadId":"t1"}
```

---

## 7. 消息类型定义

前端发送的 `messages` 数组中，每条消息的角色类型：

### 7.1 user（用户消息）

```json
{
    "id": "msg-uuid",
    "role": "user",
    "content": "用户输入的文本内容"
}
```

### 7.2 assistant（AI 回复）

```json
{
    "id": "msg-uuid",
    "role": "assistant",
    "content": "AI 的回复文本"
}
```

### 7.3 system（系统消息）

```json
{
    "id": "msg-uuid",
    "role": "system",
    "content": "系统提示词内容"
}
```

### 7.4 tool（工具结果）

```json
{
    "id": "msg-uuid",
    "role": "tool",
    "toolCallId": "tool-001",
    "content": "工具返回的结果",
    "error": "错误信息（可选）"
}
```

### 7.5 reasoning（推理消息）

```json
{
    "id": "msg-uuid",
    "role": "reasoning",
    "content": "推理过程的文本"
}
```

---

## 附录：常见错误对照

| 错误现象 | 可能原因 | 解决方案 |
|---------|---------|---------|
| `useAgent: Agent 'default' not found` | `/info` 返回的 `agents` 是数组 | 改为对象 `{"default": {...}}` |
| 纯文本能显示，加推理就不行 | `REASONING_MESSAGE_START` 缺少 `role` | 加上 `"role": "reasoning"` |
| 工具调用后文本不显示 | `TOOL_CALL_START` 用了 `toolName` | 改为 `toolCallName` |
| | `TOOL_CALL_ARGS` 用了 `args` | 改为 `delta` |
| | `TOOL_CALL_RESULT` 用了 `result` | 改为 `content` |
| | `TOOL_CALL_RESULT` 缺少 `messageId` | 添加 UUID |
| SSE 数据不解析 | `Content-Type` 不对 | 设置为 `text/event-stream` |
| | 事件之间缺少空行 | 确保每个 `data:` 之间用 `\n\n` 分隔 |
| 页面无响应 | 缺少 `RUN_FINISHED` | 必须发送 `RUN_FINISHED` 结束流 |
