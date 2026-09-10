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

`common` 产出普通依赖 JAR，只有两个启动服务需要 Spring Boot 可执行 JAR。两端不能互相依赖对方的可执行 JAR，不再依赖公共 `model` 模块。

## 运行配置

两端的常规配置仍在各自的 `application.yml` 中维护。本轮仅将 APP 短信的 `accessKeyId`、`accessKeySecret` 改为环境变量，启动 APP 前需注入这两个值；其他配置不要求新增环境变量。具体说明见 [配置说明](docs/configuration.md)。普通构建和现有单元测试不需要真实基础设施凭据。

```powershell
java -jar web/web-admin/target/web-admin-0.0.1-SNAPSHOT.jar
java -jar web/web-app/target/web-app-0.0.1-SNAPSHOT.jar
```

短信访问密钥不提供源码默认值。之前已写入 Git 历史的短信凭据仍需由部署方安排轮换，移出当前源码不等于清除了历史记录。

## 服务边界

- `web-admin`、`web-app` 各自拥有 HTTP ReqVO/RespVO、内部 Command/Query/DTO 和接口装配器。内部模型不放入 `common`，也不成为另一服务的编译依赖。
- 持久化实体和业务枚举分别位于 `com.wxy.zzarental.web.admin.entity` / `enums` 与 `com.wxy.zzarental.web.app.entity` / `enums`，由各服务独立维护；管理端专用的 `SystemUser`、`SystemPost`、`SystemUserType` 只保留在管理端。
- Controller 保留原有职责和请求绑定方式；仅在 HTTP 边界进行 VO 与内部模型转换。Service / Mapper 不再依赖 HTTP VO，JSON 属性名、日期格式、分页结构以及 `Result` 不变。
- `common` 保留现有技术能力，集中维护认证、JWT、异常处理等共享实现。它是随各服务打包的代码库，不是一个需要远程调用的微服务。
- 短信、消息客户端及其配置放在所属服务的基础设施包中，通过小型接口封装 SDK，避免直接跨服务复用服务内部实现。

### 数据与跨服务契约

公共 `model` 模块已移除，实体及枚举不会成为其他微服务的公共依赖。当前迁移保留原有表映射和数据库访问行为，没有拆库，也没有改变两端对现有数据表的使用；解除源码依赖不等于完成数据归属拆分。

接入 Spring Cloud 时，版本、注册中心和配置中心由上层工程统一选择。后续明确数据归属后，再调整跨服务的数据访问；需要共享契约包时，只放稳定的跨服务 DTO / 事件，不暴露数据库实体。本次不提前创建空的契约模块，不引入远程调用或改变业务流程。
