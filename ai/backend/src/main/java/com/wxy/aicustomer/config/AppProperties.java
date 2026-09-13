package com.wxy.aicustomer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 业务配置入口，全部以 {@code app.} 开头，集中在 application.yml 中维护。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Chat chat = new Chat();
    private final Rag rag = new Rag();
    private final Memory memory = new Memory();
    private final Knowledge knowledge = new Knowledge();
    private final Storage storage = new Storage();
    private final Apartment apartment = new Apartment();
    private final Web web = new Web();

    @Getter
    @Setter
    public static class Chat {

        /** 系统提示词所在位置（classpath 资源） */
        private String systemPromptLocation = "classpath:prompts/system-prompt.st";

        /** 保留的历史消息条数，控制多轮上下文长度 */
        private int memoryWindowSize = 20;

        /** 单条用户消息最大长度 */
        private int maxMessageLength = 2000;
    }

    @Getter
    @Setter
    public static class Rag {

        /** 是否启用知识库检索，关闭后退化为纯模型回答 */
        private boolean enabled = true;

        /** 召回条数 */
        private int topK = 5;

        /** 相似度阈值，0~1，越低召回越宽 */
        private double similarityThreshold = 0.5;

        /** 检索异常时是否直接失败；false 表示降级为纯模型回答 */
        private boolean failFast = false;

        /** 拼接进提示词的参考资料最大字符数 */
        private int maxContextChars = 4000;

        /** 城市标签在向量库 metadata 中的字段名 */
        private String cityMetadataKey = "city";

        /** 平台级通用文档的城市标签值，检索选中城市时一并召回 */
        private String commonCity = "通用";

        /** 按城市过滤后没有命中时，是否自动回退为不带过滤的检索 */
        private boolean fallbackToUnfilteredWhenEmpty = true;
    }

    @Getter
    @Setter
    public static class Memory {

        /** redis：走 Redis（默认）；memory：进程内，便于无中间件本地调试 */
        private String type = "redis";

        /** 会话消息过期时间 */
        private Duration ttl = Duration.ofDays(7);
    }

    @Getter
    @Setter
    public static class Knowledge {

        /** 单个上传文件大小上限（MB） */
        private int maxFileSizeMb = 20;

        /** 切片大小（token 近似值） */
        private int chunkSize = 800;

        private int minChunkSizeChars = 350;

        private int minChunkLengthToEmbed = 5;

        private int maxNumChunks = 10000;

        /** 是否保留切片之间的分隔符 */
        private boolean keepSeparator = true;
    }

    @Getter
    @Setter
    public static class Storage {

        /** local：本地磁盘（默认）；minio：对象存储 */
        private String type = "local";

        /** 本地存储根目录 */
        private String localPath = "./data/knowledge";

        private final Minio minio = new Minio();

        @Getter
        @Setter
        public static class Minio {

            /** 对象 key 前缀；连接信息与 bucket 复用 common 的 minio.* 配置 */
            private String prefix = "documents/";
        }
    }

    @Getter
    @Setter
    public static class Apartment {

        /** 是否允许调用公寓系统内部接口；公寓侧接口未就绪时可关闭 */
        private boolean enabled = true;

        /** 公寓系统地址 */
        private String baseUrl = "http://localhost:8080";

        /** 服务间固定 Token，公寓系统据此校验调用方身份 */
        private String serviceToken = "change-me-service-token";

        /** 内部接口统一前缀，只允许调用该前缀下的白名单接口 */
        private String internalPathPrefix = "/internal/ai";

        private Duration connectTimeout = Duration.ofSeconds(2);

        private Duration readTimeout = Duration.ofSeconds(5);
    }

    @Getter
    @Setter
    public static class Web {

        /** 允许跨域的前端地址，访客端本地开发默认 Vite 端口 */
        private List<String> allowedOrigins = new ArrayList<>(List.of("http://localhost:5173"));
    }
}
