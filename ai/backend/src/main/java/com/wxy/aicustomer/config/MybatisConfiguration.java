package com.wxy.aicustomer.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置，写法参考 common 的 MybatisPlusConfiguration，只是扫描本服务的 Mapper 包。
 *
 * <p>AI 服务不需要分页，所以这里不注册分页插件。
 */
@Configuration
@MapperScan("com.wxy.aicustomer.**.mapper")
public class MybatisConfiguration {
}
