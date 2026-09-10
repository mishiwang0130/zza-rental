# 运行配置

管理端与 APP 端分别使用各自的 `application.yml`。数据库、Redis、MinIO、RocketMQ、应用名称、端口和 JWT 配置均为普通 YAML 配置，不要求额外注入环境变量。两端配置文件独立，可按实际部署环境分别修改。

## 短信访问凭据

只有 APP 短信访问凭据使用环境变量，变量名保持现有约定：

| 必需环境变量 | 对应配置键 |
| --- | --- |
| `APP_SMS_ACCESS_KEY_ID` | `integration.sms.access-key-id` |
| `APP_SMS_ACCESS_KEY_SECRET` | `integration.sms.access-key-secret` |

启动 APP 服务前，通过操作系统、IDE Run Configuration、容器或部署平台向进程注入这两个变量。项目不会自动读取 `.env` 文件。管理端不使用短信客户端，因此不需要这两个变量。构建和纯单元测试无需真实短信访问凭据。

PowerShell 示例仅含占位值，启动前应替换为对应环境的有效凭据：

```powershell
$env:APP_SMS_ACCESS_KEY_ID = '<SMS_ACCESS_KEY_ID>'
$env:APP_SMS_ACCESS_KEY_SECRET = '<SMS_ACCESS_KEY_SECRET>'
```

短信 endpoint、签名和模板标识保留在 APP 的 `integration.sms` 普通 YAML 配置中。可按需要增加 `connect-timeout-ms`、`read-timeout-ms` 两个正整数毫秒配置；省略时使用 SDK 原有超时默认值。短信客户端由 Spring 复用，模板参数仍为 `code` 和 `min`。

## JWT 与其他普通配置

两端 JWT 密钥分别位于各自 YAML 的 `security.jwt.secret`，不再写在 Java 类中，也不依赖环境变量。当前值沿用原签名配置，以保持现有 token 兼容。密钥按原始 UTF-8 文本使用，不做 Base64 解码；HS256 要求至少 32 字节。

请求头仍为 `access-token`，subject 为 `User_Login`，claims 为 `userId`、`userName`，有效期保持 24 小时。两端可以使用相同或不同的普通配置值，本次没有增加 issuer/audience 或跨服务认证协议。轮换 JWT 密钥会使旧密钥签发的 token 失效，应安排相应登录切换。

修改部署地址、账号、短信签名/模板或消息组名时，请核对对应服务配置；消息 topic、消费者组、消息结构与发送方式未在本次改造中调整。共享配置或日志时应隐藏真实账号、密钥和连接信息，本文不提供这些实际值。
