package com.wxy.zzarental.web.admin.vo.room;

import com.wxy.zzarental.web.admin.enums.ReleaseStatus;
import com.wxy.zzarental.web.admin.vo.graph.GraphReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;


@Data
@Schema(description = "房间信息")
public class RoomSaveReqVO {

    @Schema(description = "房间ID，更新时传入")
    private Long id;

    @Schema(description = "房间号")
    private String roomNumber;

    @Schema(description = "租金")
    private java.math.BigDecimal rent;

    @Schema(description = "所属公寓ID")
    private Long apartmentId;

    @Schema(description = "发布状态")
    private ReleaseStatus isRelease;

    @Schema(description = "图片列表")
    private List<GraphReqVO> graphVoList;

    @Schema(description = "属性信息列表")
    private List<Long> attrValueIds;

    @Schema(description = "配套信息列表")
    private List<Long> facilityInfoIds;

    @Schema(description = "标签信息列表")
    private List<Long> labelInfoIds;

    @Schema(description = "支付方式列表")
    private List<Long> paymentTypeIds;

    @Schema(description = "可选租期列表")
    private List<Long> leaseTermIds;

}
