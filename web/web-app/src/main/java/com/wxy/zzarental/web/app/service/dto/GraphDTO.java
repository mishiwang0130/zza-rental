package com.wxy.zzarental.web.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** APP 服务内部模型，与 HTTP VO 的序列化契约独立。 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GraphDTO implements Serializable {
    private String name;
    private String url;
}
