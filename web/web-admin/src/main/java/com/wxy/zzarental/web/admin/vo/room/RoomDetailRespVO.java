package com.wxy.zzarental.web.admin.vo.room;

import com.wxy.zzarental.web.admin.enums.ReleaseStatus;
import com.wxy.zzarental.web.admin.vo.apartment.ApartmentBasicRespVO;
import com.wxy.zzarental.web.admin.vo.apartment.FacilityRespVO;
import com.wxy.zzarental.web.admin.vo.apartment.LabelRespVO;
import com.wxy.zzarental.web.admin.vo.apartment.LeaseTermRespVO;
import com.wxy.zzarental.web.admin.vo.apartment.PaymentTypeRespVO;
import com.wxy.zzarental.web.admin.vo.attr.AttrValueRespVO;
import com.wxy.zzarental.web.admin.vo.graph.GraphRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.math.BigDecimal;


@Schema(description = "房间信息")
@Data
public class RoomDetailRespVO {

    private Long id;

    private String roomNumber;
    private BigDecimal rent;
    private Long apartmentId;
    private ReleaseStatus isRelease;

    @Schema(description = "所属公寓信息")
    private ApartmentBasicRespVO apartmentInfo;

    @Schema(description = "图片列表")
    private List<GraphRespVO> graphVoList;

    @Schema(description = "属性信息列表")
    private List<AttrValueRespVO> attrValueVoList;

    @Schema(description = "配套信息列表")
    private List<FacilityRespVO> facilityInfoList;

    @Schema(description = "标签信息列表")
    private List<LabelRespVO> labelInfoList;

    @Schema(description = "支付方式列表")
    private List<PaymentTypeRespVO> paymentTypeList;

    @Schema(description = "可选租期列表")
    private List<LeaseTermRespVO> leaseTermList;
}
