# zza-rental

租赁系统后端。`web-admin` 和 `web-app` 是两个独立的 Spring Boot 启动应用，后续分别接入 Spring Cloud；本轮不引入注册中心、网关或跨服务调用，也不改变现有前端接口契约。

## 构建

使用 JDK 17，先确认 `JAVA_HOME` 和 IDE 的 Maven Runner 都指向 JDK 17。项目 Wrapper 固定使用 Maven 3.9.11；首次使用需要下载 Maven，后续使用本地缓存。

```powershell
.\mvnw.cmd -version
.\mvnw.cmd clean install
```

Linux / macOS：

```sh
sh mvnw -version
sh mvnw clean install
```

也可以使用已安装的 Maven 3.9.11 或更高的 Maven 3.x。父 POM 在 `validate` 阶段检查 Maven 和 JDK 版本，并集中管理 Java release、Lombok 及其注解处理器版本；子模块不单独覆盖这些版本。

只构建某个服务及其依赖：

```powershell
.\mvnw.cmd -pl web/web-admin -am clean package
.\mvnw.cmd -pl web/web-app -am clean package
```

`model`、`common` 产出普通依赖 JAR，只有两个启动服务需要 Spring Boot 可执行 JAR。两端不能互相依赖对方的可执行 JAR。

## 运行配置

两端通过各自 `application.yml` 中的环境变量占位符读取部署配置。环境变量清单见 [配置说明](docs/configuration.md)。敏感值没有源码默认值，启动前需在部署环境注入；普通构建和现有单元测试不需要真实基础设施凭据。

```powershell
java -jar web/web-admin/target/web-admin-0.0.1-SNAPSHOT.jar
java -jar web/web-app/target/web-app-0.0.1-SNAPSHOT.jar
```

不要把真实数据库密码、JWT 密钥、对象存储凭据或短信访问密钥提交到仓库。之前已写入 Git 历史的有效凭据需由部署方安排轮换，移出当前源码不等于清除了历史记录。

## 服务边界

- `web-admin`、`web-app` 各自拥有 HTTP ReqVO/RespVO、内部 Command/Query/DTO 和接口装配器。内部模型不放入 `common`，也不成为另一服务的编译依赖。
- Controller 保留原有职责和请求绑定方式；仅在 HTTP 边界进行 VO 与内部模型转换。Service / Mapper 不再依赖 HTTP VO，JSON 属性名、日期格式、分页结构以及 `Result` 不变。
- `common` 保留现有技术能力，集中维护认证、JWT、异常处理等共享实现。它是随各服务打包的代码库，不是一个需要远程调用的微服务。
- 短信、消息客户端及其配置放在所属服务的基础设施包中，通过小型接口封装 SDK，避免直接跨服务复用服务内部实现。

### model 的过渡定位

`model` 暂时仍是两端使用的持久化实体模块，不应作为未来跨服务接口的公共契约。当前绝大多数表实体在两端均被引用，直接删除该模块并复制实体不能解除对同一数据模型的耦合。

正式拆分数据归属时，应把实体、Mapper 和数据库迁移文件一起移入数据所属服务；另一服务通过明确的 API / 消息契约取得所需数据。若需要共享契约包，应只放稳定的跨服务 DTO / 事件，不暴露数据库实体。`SystemUser`、`SystemPost` 当前仅被管理端使用，可在明确拆分范围后优先迁入管理端。
