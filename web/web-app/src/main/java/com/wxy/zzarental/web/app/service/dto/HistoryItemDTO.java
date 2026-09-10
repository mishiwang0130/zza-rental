package com.wxy.zzarental.web.app.service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Date;

/** APP 服务内部模型，与 HTTP VO 的序列化契约独立。 */
@Data
public class HistoryItemDTO {

    private Long id;

    private Long userId;
    private Long roomId;
    private Date browseTime;
    private String roomNumber;
    private BigDecimal rent;
    private List<GraphDTO> roomGraphVoList;
    private String apartmentName;
    private String provinceName;
    private String cityName;
    private String districtName;
}
