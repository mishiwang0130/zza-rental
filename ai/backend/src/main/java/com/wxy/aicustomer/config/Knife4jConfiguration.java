package com.wxy.aicustomer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 接口文档配置，写法与 web-admin / web-app 的 Knife4jConfiguration 保持一致。
 *
 * <p>Knife4j 的界面在 /doc.html，它读取的是 springdoc 暴露的 /v3/api-docs；
 * 这里按业务模块分组，doc.html 左侧的分组菜单就来自下面这些 GroupedOpenApi。
 */
@Configuration
public class Knife4jConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("公寓智能客服API")
                .version("1.0")
                .description("匿名访客聊天（SSE）、RAG 知识库管理、Function Tool 调试接口")
                .termsOfService("http://doc.xiaominfo.com")
                .license(new License().name("Apache 2.0").url("http://doc.xiaominfo.com")));
    }

    @Bean
    public GroupedOpenApi chatAPI() {
        return GroupedOpenApi.builder().group("访客聊天")
                .pathsToMatch(
                        "/api/chat/**"
                )
                .build();
    }

    @Bean
    public GroupedOpenApi knowledgeAPI() {
        return GroupedOpenApi.builder().group("知识库管理")
                .pathsToMatch(
                        "/api/knowledge/**"
                )
                .build();
    }
}
