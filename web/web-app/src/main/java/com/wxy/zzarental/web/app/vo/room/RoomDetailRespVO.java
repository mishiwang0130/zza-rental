package com.wxy.zzarental.web.app.vo.room;

import com.wxy.zzarental.web.app.vo.apartment.ApartmentItemRespVO;
import com.wxy.zzarental.web.app.vo.attr.AttrValueRespVO;
import com.wxy.zzarental.web.app.vo.common.FacilityRespVO;
import com.wxy.zzarental.web.app.vo.common.LabelRespVO;
import com.wxy.zzarental.web.app.vo.fee.FeeValueRespVO;
import com.wxy.zzarental.web.app.vo.graph.GraphRespVO;
import com.wxy.zzarental.web.app.vo.leaseterm.LeaseTermRespVO;
import com.wxy.zzarental.web.app.vo.payment.PaymentTypeRespVO;
import com.wxy.zzarental.model.enums.ReleaseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.math.BigDecimal;

@Data
@Schema(description = "APP房间详情")
public class RoomDetailRespVO {

    private Long id;

    private String roomNumber;
    private BigDecimal rent;
    private Long apartmentId;
    private ReleaseStatus isRelease;

    @Schema(description = "所属公寓信息")
    private ApartmentItemRespVO apartmentItemVo;

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

    @Schema(description = "杂费列表")
    private List<FeeValueRespVO> feeValueVoList;

    @Schema(description = "租期列表")
    private List<LeaseTermRespVO> leaseTermList;

}
