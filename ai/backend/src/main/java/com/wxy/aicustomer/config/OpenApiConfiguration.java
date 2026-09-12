package com.wxy.aicustomer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger 配置。知识库当前只提供后端接口，上传与重建都通过这里调试。
 */
@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI aiCustomerOpenApi() {
        return new OpenAPI().info(new Info()
                .title("公寓智能客服 API")
                .description("匿名访客聊天（SSE）、RAG 知识库管理、Function Tool 调试接口")
                .version("v1"));
    }
}
