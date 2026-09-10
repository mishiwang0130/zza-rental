package com.wxy.zzarental.web.app.vo.room;


import com.wxy.zzarental.web.app.vo.apartment.ApartmentBasicRespVO;
import com.wxy.zzarental.web.app.vo.common.LabelRespVO;
import com.wxy.zzarental.web.app.vo.graph.GraphRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "APP房间列表实体")
@Data
public class RoomItemRespVO {

    @Schema(description = "房间id")
    private Long id;

    @Schema(description = "房间号")
    private String roomNumber;

    @Schema(description = "租金（元/月）")
    private BigDecimal rent;

    @Schema(description = "房间图片列表")
    private List<GraphRespVO> graphVoList;

    @Schema(description = "房间标签列表")
    private List<LabelRespVO> labelInfoList;

    @Schema(description = "房间所属公寓信息")
    private ApartmentBasicRespVO apartmentInfo;

}
