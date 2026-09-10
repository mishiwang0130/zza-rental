package com.wxy.zzarental.web.app.vo.agreement;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.wxy.zzarental.model.enums.LeaseSourceType;
import com.wxy.zzarental.model.enums.LeaseStatus;
import com.wxy.zzarental.web.app.vo.graph.GraphRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Date;
import java.math.BigDecimal;

@Data
@Schema(description = "租约详细信息")
public class AgreementDetailRespVO {

    private Long id;

    private String phone;
    private String name;
    private String identificationNumber;
    private Long apartmentId;
    private Long roomId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date leaseStartDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date leaseEndDate;
    private Long leaseTermId;
    private BigDecimal rent;
    private BigDecimal deposit;
    private Long paymentTypeId;
    private LeaseStatus status;
    private LeaseSourceType sourceType;
    private String additionalInfo;

    @Schema(description = "公寓名称")
    private String apartmentName;

    @Schema(description = "公寓图片列表")
    private List<GraphRespVO> apartmentGraphVoList;

    @Schema(description = "房间号")
    private String roomNumber;

    @Schema(description = "房间图片列表")
    private List<GraphRespVO> roomGraphVoList;

    @Schema(description = "支付方式")
    private String paymentTypeName;

    @Schema(description = "租期月数")
    private Integer leaseTermMonthCount;

    @Schema(description = "租期单位")
    private String leaseTermUnit;

}
