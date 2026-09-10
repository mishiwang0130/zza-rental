package com.wxy.zzarental.web.app.service.dto;

import com.wxy.zzarental.model.entity.ApartmentInfo;
import com.wxy.zzarental.model.entity.LabelInfo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** APP 服务内部模型，与 HTTP VO 的序列化契约独立。 */
@Data
public class RoomItemDTO {
    private Long id;
    private String roomNumber;
    private BigDecimal rent;
    private List<GraphDTO> graphVoList;
    private List<LabelInfo> labelInfoList;
    private ApartmentInfo apartmentInfo;
}
