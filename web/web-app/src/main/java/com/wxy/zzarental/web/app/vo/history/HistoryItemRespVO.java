package com.wxy.zzarental.web.app.vo.history;

import com.fasterxml.jackson.annotation.JsonFormat;


import com.wxy.zzarental.web.app.vo.graph.GraphRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Date;

@Data
@Schema(description = "浏览历史基本信息")
public class HistoryItemRespVO {

    private Long id;

    private Long userId;
    private Long roomId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date browseTime;

    @Schema(description = "房间号")
    private String roomNumber;

    @Schema(description = "租金")
    private BigDecimal rent;

    @Schema(description = "房间图片列表")
    private List<GraphRespVO> roomGraphVoList;

    @Schema(description = "公寓名称")
    private String apartmentName;

    @Schema(description = "省份名称")
    private String provinceName;

    @Schema(description = "城市名称")
    private String cityName;

    @Schema(description = "区县名称")
    private String districtName;

}
