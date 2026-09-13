# 公寓智能客服（ai）

与 `web` 同级、纳入 zza-rental 聚合工程的第二个业务方向：匿名访客 AI 客服。
对应设计文档：[docs/公寓智能客服_系统架构设计_V3.docx](../docs/公寓智能客服_系统架构设计_V3.docx)。

```
ai/
├─ backend/     Spring Boot + Spring AI 服务（Maven 模块，与 web 同一个 reactor）
├─ frontend/    Vue 3 + Vite + TypeScript 访客聊天页
└─ docker-compose.yml   Qdrant / Redis / MinIO 本地中间件
```

## 与公寓系统的关系

- 复用 `common` 模块：`Result`、`ResultCodeEnum`、`ZZAException`、`GlobalExceptionHandler`、
  `RedisUtil`、MinIO 客户端配置。同一个 reactor 内直接引用，改 common 不需要单独 `install`。
- 业务数据不落 AI 服务：房源、租约、账单等仍由公寓系统维护，AI 服务只通过 HTTP 调用
  `/internal/ai/**` 白名单接口（见 `client/ApartmentClient`）。
- `backend` 的 parent 是 `spring-boot-starter-parent 3.5.16` 而不是 zza-rental 根 pom：
  聚合与继承是两件事，`web-admin` / `web-app` 停留在 Spring Boot 3.0.5，而 Spring AI 1.1.x
  需要 3.5.x 基线。两者只通过 HTTP 契约通信，没有运行期依赖。

## 后端

### 配置分层

- `application.yml`：与环境无关的公共配置——应用名、业务参数（切片大小、记忆窗口、召回条数等）、
  Swagger 路径、actuator 暴露项、日志基础级别。**不放**端口、地址、账号、Key 这类随环境变化的配置。
- `application-{profile}.yml`：某个环境独有的配置。当前只有 `application-local.yml`，
  端口、Redis/Qdrant/MinIO 地址、模型 base-url 与 Key、公寓系统地址与 Service Token、
  存储类型、日志级别，以及"用 Mock 还是真实模型"都在这里。
- 新增环境：复制 `application-local.yml` 改成 `application-test.yml` / `application-prod.yml`，
  启动时用 `--spring.profiles.active=test` 指定即可，`application.yml` 通常不用动。
- 个人不想提交的覆盖值可放 `application-local.local.yml`（被仓库 `.gitignore` 的
  `application-*.local.yml` 规则忽略），不建议直接改要提交的环境文件。

### 本地启动（profile=local，开箱可跑）

`application-local.yml` 默认是"零中间件"组合：进程内会话与文档记录、关闭 RAG 检索、
用 `MockChatModel` 顶替真实模型，用于验证前端交互与 SSE 链路。

```powershell
# 在仓库根目录执行
.\mvnw.cmd -pl ai/backend -am spring-boot:run
```

接口文档：<http://localhost:8083/swagger-ui.html>，健康检查：<http://localhost:8083/actuator/health>

### 连真实中间件与模型

```powershell
docker compose -f ai/docker-compose.yml up -d
$env:AI_API_KEY = '<你的模型 Key>'
$env:AI_EMBEDDING_API_KEY = '<你的 Embedding Key>'
.\mvnw.cmd -pl ai/backend -am spring-boot:run
```

然后把 `application-local.yml` 中标了 `[真实依赖]` 的项改掉：`spring.ai.model.chat` 与
`app.chat.provider` 改回 `openai`、`app.memory.type` 与 `app.knowledge.repository` 改回 `redis`、
`app.rag.enabled` 与 `spring.ai.vectorstore.qdrant.initialize-schema` 改为 `true`、
`app.apartment.enabled` 视公寓系统接口是否就绪而定。

### 关键配置

| 配置 | 说明 |
| --- | --- |
| `app.chat.provider` | `openai`（OpenAI 兼容协议）/ `mock`（本地假模型，local 默认） |
| `app.memory.type` | `redis`（多实例共享）/ `memory`（进程内，local 默认） |
| `app.knowledge.repository` | `redis` / `memory`（local 默认） |
| `app.storage.type` | `local`（默认）/ `minio`，切换 MinIO 时连接参数复用 `minio.*` |
| `app.rag.*` | `enabled`、`top-k`、`similarity-threshold`、`fail-fast` |
| `app.apartment.*` | 公寓系统地址、Service Token、超时；`enabled=false` 时 Tool 返回友好提示 |
| `spring.ai.openai.*` | 对话与 Embedding 的 base-url / api-key / model，可分别指向不同厂商 |

上表中随环境变化的值（provider、memory.type、rag.enabled、地址与 Key 等）都只在
`application-{profile}.yml` 里出现。

### 接口

| 接口 | 方法 | 说明 |
| --- | --- | --- |
| `/api/chat/stream` | POST SSE | 事件顺序 `meta → delta* → sources → done`，异常给 `error` |
| `/api/chat` | POST | 非流式问答，便于 Swagger 调试 |
| `/api/chat/conversations/{id}/messages` | GET | 读取会话 Memory 中的历史消息 |
| `/api/chat/conversations/{id}` | DELETE | 清空会话（前端“新对话”） |
| `/api/knowledge/documents` | POST/GET | 上传文档（自动解析、切片、向量化）/ 文档列表 |
| `/api/knowledge/documents/{id}/rebuild` | POST | 重新解析切片并覆盖向量 |
| `/api/knowledge/documents/{id}` | DELETE | 删除文档及其全部向量 |
| `/api/knowledge/search` | POST | 语义检索调试，返回 TopK 切片 |

响应体统一复用 common 的 `Result`：HTTP 200 + `code`（200 成功，202 参数错误，203 服务异常…），
前端按 `code` 判断业务结果。

### 包结构（对应架构文档 §9）

```
com.wxy.aicustomer
├─ chat        ChatController / ChatService / dto
├─ knowledge   KnowledgeController / KnowledgeService / entity / repository
├─ rag         RagService / ChunkService / parser
├─ tool        RoomTools / ViewingTools
├─ client      ApartmentClient（/internal/ai/** 白名单）
├─ memory      RedisChatMemoryRepository / MessageCodec
├─ config      AppProperties / ChatClient / Memory / 通用模块复用
└─ common      常量
```

## 前端

```powershell
cd ai/frontend
npm install
npm run dev      # http://localhost:5173，/api 代理到后端 8083
npm run build    # 类型检查 + 产物到 dist/
```

页面能力：匿名 `visitorId` / `conversationId`（localStorage）、消息列表、SSE 流式渲染、
新对话、参考资料展示。`src/types/chat.ts` 与后端 `ChatStreamPayload` 字段一一对应。

## 后续开发点

1. 公寓系统实现 `/internal/ai/rooms/search`、`/internal/ai/rooms/{id}`、`/internal/ai/viewings`，
   并把 Service Token 校验加到 `web-app`；之后把 `app.apartment.enabled` 打开。
2. 新增 Tool：实现带 `@Tool` 的方法并声明为 Bean，`AiChatClientConfiguration` 会自动注册。
3. 新增文档类型：实现 `DocumentParser` 并在 `DocumentParserFactory` 中生效（当前支持 md/txt/pdf/docx 等）。
4. Tool 入参校验、幂等、审计事件按需扩展；写操作参考 `ViewingTools` 的 requestId 方案。
