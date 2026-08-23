package com.wxy.zzarental.web.admin.custom.config;

import com.wxy.zzarental.web.admin.custom.converter.StringToBaseEnumConverter;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Resource
    private StringToBaseEnumConverter stringToBaseEnumConverter;

    @Override
    public void addFormatters(FormatterRegistry registry) {

        registry.addConverterFactory(this.stringToBaseEnumConverter);
    }
}
