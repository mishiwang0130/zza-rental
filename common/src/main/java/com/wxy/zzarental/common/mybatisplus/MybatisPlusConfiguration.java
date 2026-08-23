package com.wxy.zzarental.common.mybatisplus;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.wxy.zzarental.web.*.mapper")
public class MybatisPlusConfiguration {

}