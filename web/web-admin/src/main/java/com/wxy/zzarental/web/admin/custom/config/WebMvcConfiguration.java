package com.wxy.zzarental.web.admin.custom.config;

import com.wxy.zzarental.web.admin.custom.converter.StringToBaseEnumConverter;
import com.wxy.zzarental.common.login.AuthenticationInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Resource
    private StringToBaseEnumConverter stringToBaseEnumConverter;
    @Resource
    private AuthenticationInterceptor authenticationInterceptor;

    @Override
    public void addFormatters(FormatterRegistry registry) {

        registry.addConverterFactory(this.stringToBaseEnumConverter);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.authenticationInterceptor).addPathPatterns("/admin/**").excludePathPatterns("/admin/login/**");
    }
}
