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
- `application-local.yml`：本地环境配置，当前连的是虚拟机上的真实中间件
  （`192.168.205.128` 上的 Redis / Qdrant / MinIO）和阿里云百炼的 qwen 模型，
  Key 从环境变量 `DASHSCOPE_API_KEY` 读取。
- `src/test/resources/application-test.yml`：测试专用配置，把外部依赖全换成内存/Mock，
  所以 `mvn test` 不需要任何中间件和网络。
- 新增环境：复制 `application-local.yml` 改成 `application-dev.yml` / `application-prod.yml`，
  启动时用 `--spring.profiles.active=dev` 指定即可，`application.yml` 通常不用动。
- 个人不想提交的覆盖值可放 `application-local.local.yml`（被仓库 `.gitignore` 的
  `application-*.local.yml` 规则忽略），不建议直接改要提交的环境文件。

### 启动（profile=local）

```powershell
# 在仓库根目录执行
.\mvnw.cmd -pl ai/backend -am spring-boot:run
```

启动前先确认模型 Key 已注入（PowerShell 示例，IDE 里跑就在 Run Configuration 的环境变量里加）：

```powershell
$env:DASHSCOPE_API_KEY = '<你的百炼 Key>'
```

- 接口文档（Knife4j）：<http://localhost:8083/doc.html>，左侧按「访客聊天 / 知识库管理」分组
- 健康检查：<http://localhost:8083/actuator/health>
- 想切回"零中间件"的纯本地调试（Mock 模型 + 进程内存储 + 关闭检索），
  按 `application-local.yml` 顶部注释改 4 个开关即可

### 测试（profile=test）

```powershell
.\mvnw.cmd -pl ai/backend -am test
```

### 关键配置

| 配置 | 说明 |
| --- | --- |
| `app.chat.provider` | `openai`（OpenAI 兼容协议，local 用）/ `mock`（本地假模型） |
| `app.memory.type` | `redis`（多实例共享，local 用）/ `memory`（进程内） |
| `app.knowledge.repository` | `redis`（local 用）/ `memory` |
| `app.storage.type` | `minio`（local 用，连接参数复用 `minio.*`）/ `local`（本机磁盘） |
| `app.rag.*` | `enabled`、`top-k`、`similarity-threshold`、`fail-fast`、`city-metadata-key`、`common-city`、`fallback-to-unfiltered-when-empty` |
| `app.apartment.*` | 公寓系统地址、Service Token、超时；`enabled=false` 时 Tool 返回友好提示 |
| `spring.ai.openai.*` | 对话与 Embedding 的 base-url / api-key / model，可分别指向不同厂商 |

上表中随环境变化的值（provider、memory.type、rag.enabled、地址与 Key 等）都只在
`application-{profile}.yml` 里出现。

### 接口

| 接口 | 方法 | 说明 |
| --- | --- | --- |
| `/api/chat/stream` | POST SSE | 事件顺序 `meta → delta* → sources → done`，异常给 `error`；可选 `city` 按城市过滤知识库 |
| `/api/chat` | POST | 非流式问答，便于 Swagger 调试；可选 `city` 同上 |
| `/api/chat/conversations/{id}/messages` | GET | 读取会话 Memory 中的历史消息 |
| `/api/chat/conversations/{id}` | DELETE | 清空会话（前端“新对话”） |
| `/api/knowledge/documents` | POST/GET | 上传文档（自动解析、切片、向量化）/ 文档列表；可选 `city` 城市标签，缺省为“通用” |
| `/api/knowledge/documents/{id}/rebuild` | POST | 重新解析切片并覆盖向量 |
| `/api/knowledge/documents/{id}` | DELETE | 删除文档及其全部向量 |
| `/api/knowledge/search` | POST | 语义检索调试，返回 TopK 切片；可选 `city`，用于对比过滤前后差异 |

知识库支持按城市标签（metadata 字段 `city`，取值如 `武汉` / `广州` / `深圳` / `通用`）过滤：请求带 `city`
时召回“选中城市 + 通用”，不带则完全不过滤。样例文档见 `docs/` 下的城市规则文档集，配套验证步骤见
[RAG 城市标签检索验证清单](../docs/RAG城市标签检索验证清单.md)。

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
