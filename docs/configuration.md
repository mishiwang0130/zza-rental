# 运行环境配置

`web-admin` 和 `web-app` 是两个独立 Spring Boot 进程。管理端读取 `ADMIN_` 前缀变量，APP 端读取 `APP_` 前缀变量；部署时可配置为相同或不同的数据库、Redis、对象存储及 JWT 密钥。当前未引入 Spring Cloud，`common` 只共享代码，不共享运行中的配置或登录状态。

两端 `application.yml` 不再保存环境地址、账号或密钥。启动前必须通过操作系统、IDE Run Configuration、容器或部署平台向对应进程注入下列变量。项目不会自动读取 `.env` 文件。没有提供默认值的配置缺失时，应用无法正常启动；构建和纯单元测试不需要真实服务凭据。

## 两端都需要的变量

下表的 `<PREFIX>` 在管理端替换为 `ADMIN`，在 APP 端替换为 `APP`。

| 环境变量 | 用途 |
| --- | --- |
| `<PREFIX>_DB_URL` | 完整 JDBC URL；保留原环境所需连接参数 |
| `<PREFIX>_DB_USERNAME` | 数据库用户名 |
| `<PREFIX>_DB_PASSWORD` | 数据库密码 |
| `<PREFIX>_REDIS_HOST` | Redis 主机名 |
| `<PREFIX>_REDIS_PASSWORD` | Redis 密码；无认证环境应显式配置空值 |
| `<PREFIX>_MINIO_ENDPOINT` | MinIO 服务地址 |
| `<PREFIX>_MINIO_ACCESS_KEY` | MinIO 访问标识 |
| `<PREFIX>_MINIO_SECRET_KEY` | MinIO 访问密钥 |
| `<PREFIX>_MINIO_BUCKET_NAME` | 已有业务桶名称 |
| `<PREFIX>_JWT_SECRET` | HS256 签名原始密钥文本，UTF-8 编码后至少 32 字节 |

当前保留了 `common` 的 MinIO 配置能力，两端均需配置相应变量。不要为生产密钥设置代码默认值，也不要把真实配置提交到仓库。

JWT 仍使用 `access-token` 请求头、`User_Login` subject、`userId`/`userName` claims 和 24 小时有效期。密钥按原始 UTF-8 文本使用，不做 Base64 解码。需要保留现有 token 有效性时，部署侧应先注入与原环境一致的密钥。两端环境变量相互独立；如当前部署需要相同密钥，可由部署平台给两者设置相同值，本次没有增加 issuer/audience 或跨服务认证协议。

## APP 端额外需要的变量

| 环境变量 | 用途 |
| --- | --- |
| `APP_SMS_ACCESS_KEY_ID` | 短信服务访问标识 |
| `APP_SMS_ACCESS_KEY_SECRET` | 短信服务访问密钥 |
| `APP_SMS_ENDPOINT` | 当前短信 SDK 使用的 endpoint |
| `APP_SMS_SIGN_NAME` | 当前已配置的短信签名 |
| `APP_SMS_TEMPLATE_CODE` | 当前已配置的短信模板标识 |
| `APP_ROCKETMQ_NAME_SERVER` | RocketMQ NameServer 地址配置 |
| `APP_ROCKETMQ_PRODUCER_GROUP` | 当前生产者组名 |

迁移时使用原环境的短信签名、模板、endpoint 和生产者组名，避免配置变化影响现有发送行为。短信模板参数仍为 `code` 和 `min`；短信 SDK 客户端改为复用 Bean。消息 topic、消费者组、消息字段及 `convertAndSend` 调用方式保持原样。

## 可选调优变量

| 环境变量 | 默认值 / 行为 |
| --- | --- |
| `ADMIN_APPLICATION_NAME` / `APP_APPLICATION_NAME` | `zza-rental-admin` / `zza-rental-app` |
| `ADMIN_SERVER_PORT` / `APP_SERVER_PORT` | `8080` / `8081` |
| `<PREFIX>_REDIS_PORT` | `6379` |
| `<PREFIX>_REDIS_DATABASE` | `0` |
| `<PREFIX>_DB_CONNECTION_TIMEOUT_MS` | `60000` |
| `<PREFIX>_DB_MAX_POOL_SIZE` | `12` |
| `<PREFIX>_DB_MIN_IDLE` | `10` |
| `APP_SMS_CONNECT_TIMEOUT_MS` | 未配置时沿用 SDK 默认连接超时；配置时为正整数毫秒 |
| `APP_SMS_READ_TIMEOUT_MS` | 未配置时沿用 SDK 默认读取超时；配置时为正整数毫秒 |

## 注入示例

以下只有占位值，不能直接用于启动；需在受控部署环境中替换，并补齐对应应用的所有必填变量。

```powershell
$env:ADMIN_DB_URL = '<JDBC_URL>'
$env:ADMIN_DB_USERNAME = '<DATABASE_USERNAME>'
$env:ADMIN_DB_PASSWORD = '<DATABASE_PASSWORD>'
$env:ADMIN_JWT_SECRET = '<SIGNING_SECRET_AT_LEAST_32_UTF8_BYTES>'

$env:APP_SMS_ACCESS_KEY_ID = '<SMS_ACCESS_KEY_ID>'
$env:APP_SMS_ACCESS_KEY_SECRET = '<SMS_ACCESS_KEY_SECRET>'
$env:APP_SMS_ENDPOINT = '<SMS_ENDPOINT>'
```

也可使用 Spring Boot 支持的外部配置文件或部署平台 Secret 注入。两端入口均接收命令行参数，可按部署需要传入 `--spring.profiles.active` 或 `--spring.config.additional-location`。真实凭据不应直接出现在命令行参数中，以免进入命令历史或进程列表。

本次只清理当前源文件，已有 Git 历史和旧构建产物不会因此自动去除旧凭据。若旧凭据仍有效，应由部署负责人安排轮换；JWT 轮换会使旧密钥签发的 token 失效，需安排相应登录切换。
