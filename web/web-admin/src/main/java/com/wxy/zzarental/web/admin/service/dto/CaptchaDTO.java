package com.wxy.zzarental.web.admin.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data

@AllArgsConstructor
public class CaptchaDTO {

    private String image;

    private String key;
}
