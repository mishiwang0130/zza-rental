package com.wxy.zzarental.web.app.controller.assembler;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wxy.zzarental.web.app.entity.*;
import com.wxy.zzarental.web.app.service.command.LoginCommand;
import com.wxy.zzarental.web.app.service.dto.*;
import com.wxy.zzarental.web.app.service.query.RoomQuery;
import com.wxy.zzarental.web.app.vo.agreement.AgreementDetailRespVO;
import com.wxy.zzarental.web.app.vo.agreement.AgreementItemRespVO;
import com.wxy.zzarental.web.app.vo.apartment.ApartmentBasicRespVO;
import com.wxy.zzarental.web.app.vo.apartment.ApartmentDetailRespVO;
import com.wxy.zzarental.web.app.vo.apartment.ApartmentItemRespVO;
import com.wxy.zzarental.web.app.vo.appointment.AppointmentDetailRespVO;
import com.wxy.zzarental.web.app.vo.appointment.AppointmentItemRespVO;
import com.wxy.zzarental.web.app.vo.attr.AttrValueRespVO;
import com.wxy.zzarental.web.app.vo.common.FacilityRespVO;
import com.wxy.zzarental.web.app.vo.common.LabelRespVO;
import com.wxy.zzarental.web.app.vo.fee.FeeValueRespVO;
import com.wxy.zzarental.web.app.vo.graph.GraphRespVO;
import com.wxy.zzarental.web.app.vo.history.HistoryItemRespVO;
import com.wxy.zzarental.web.app.vo.leaseterm.LeaseTermRespVO;
import com.wxy.zzarental.web.app.vo.payment.PaymentTypeRespVO;
import com.wxy.zzarental.web.app.vo.room.RoomDetailRespVO;
import com.wxy.zzarental.web.app.vo.room.RoomItemRespVO;
import com.wxy.zzarental.web.app.vo.room.RoomPageReqVO;
import com.wxy.zzarental.web.app.vo.user.LoginReqVO;
import com.wxy.zzarental.web.app.vo.user.UserInfoRespVO;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** HTTP 契约与 APP 服务内部模型之间的转换边界。保留嵌套字段、null 和分页元数据。 */
public final class AppApiAssembler {

    private AppApiAssembler() {
    }

    public static LoginCommand toCommand(LoginReqVO request) {
        return copy(request, LoginCommand::new);
    }

    public static RoomQuery toQuery(RoomPageReqVO request) {
        return copy(request, RoomQuery::new);
    }

    public static UserInfoRespVO toResponse(UserInfoDTO source) {
        return source == null ? null : new UserInfoRespVO(source.getNickname(), source.getAvatarUrl());
    }

    public static ApartmentItemRespVO toResponse(ApartmentItemDTO source) {
        ApartmentItemRespVO response = copy(source, ApartmentItemRespVO::new, "labelInfoList", "graphVoList");
        if (response != null) {
            response.setLabelInfoList(map(source.getLabelInfoList(), AppApiAssembler::toResponse));
            response.setGraphVoList(map(source.getGraphVoList(), AppApiAssembler::toResponse));
        }
        return response;
    }

    public static ApartmentDetailRespVO toResponse(ApartmentDetailDTO source) {
        ApartmentDetailRespVO response = copy(source, ApartmentDetailRespVO::new,
                "labelInfoList", "graphVoList", "facilityInfoList");
        if (response != null) {
            response.setLabelInfoList(map(source.getLabelInfoList(), AppApiAssembler::toResponse));
            response.setGraphVoList(map(source.getGraphVoList(), AppApiAssembler::toResponse));
            response.setFacilityInfoList(map(source.getFacilityInfoList(), AppApiAssembler::toResponse));
        }
        return response;
    }

    public static RoomItemRespVO toResponse(RoomItemDTO source) {
        RoomItemRespVO response = copy(source, RoomItemRespVO::new, "apartmentInfo", "labelInfoList", "graphVoList");
        if (response != null) {
            response.setApartmentInfo(copy(source.getApartmentInfo(), ApartmentBasicRespVO::new));
            response.setLabelInfoList(map(source.getLabelInfoList(), AppApiAssembler::toResponse));
            response.setGraphVoList(map(source.getGraphVoList(), AppApiAssembler::toResponse));
        }
        return response;
    }

    public static RoomDetailRespVO toResponse(RoomDetailDTO source) {
        RoomDetailRespVO response = copy(source, RoomDetailRespVO::new, "apartmentItemVo", "graphVoList",
                "attrValueVoList", "facilityInfoList", "labelInfoList", "paymentTypeList", "feeValueVoList", "leaseTermList");
        if (response != null) {
            response.setApartmentItemVo(toResponse(source.getApartmentItemVo()));
            response.setGraphVoList(map(source.getGraphVoList(), AppApiAssembler::toResponse));
            response.setAttrValueVoList(map(source.getAttrValueVoList(), AppApiAssembler::toResponse));
            response.setFacilityInfoList(map(source.getFacilityInfoList(), AppApiAssembler::toResponse));
            response.setLabelInfoList(map(source.getLabelInfoList(), AppApiAssembler::toResponse));
            response.setPaymentTypeList(map(source.getPaymentTypeList(), AppApiAssembler::toResponse));
            response.setFeeValueVoList(map(source.getFeeValueVoList(), AppApiAssembler::toResponse));
            response.setLeaseTermList(map(source.getLeaseTermList(), AppApiAssembler::toResponse));
        }
        return response;
    }

    public static AgreementItemRespVO toResponse(AgreementItemDTO source) {
        AgreementItemRespVO response = copy(source, AgreementItemRespVO::new, "roomGraphVoList");
        if (response != null) {
            response.setRoomGraphVoList(map(source.getRoomGraphVoList(), AppApiAssembler::toResponse));
        }
        return response;
    }

    public static AgreementDetailRespVO toResponse(AgreementDetailDTO source) {
        AgreementDetailRespVO response = copy(source, AgreementDetailRespVO::new, "apartmentGraphVoList", "roomGraphVoList");
        if (response != null) {
            response.setApartmentGraphVoList(map(source.getApartmentGraphVoList(), AppApiAssembler::toResponse));
            response.setRoomGraphVoList(map(source.getRoomGraphVoList(), AppApiAssembler::toResponse));
        }
        return response;
    }

    public static AppointmentItemRespVO toResponse(AppointmentItemDTO source) {
        AppointmentItemRespVO response = copy(source, AppointmentItemRespVO::new, "graphVoList");
        if (response != null) {
            response.setGraphVoList(map(source.getGraphVoList(), AppApiAssembler::toResponse));
        }
        return response;
    }

    public static AppointmentDetailRespVO toResponse(AppointmentDetailDTO source) {
        AppointmentDetailRespVO response = copy(source, AppointmentDetailRespVO::new, "apartmentItemVo");
        if (response != null) {
            response.setApartmentItemVo(toResponse(source.getApartmentItemVo()));
        }
        return response;
    }

    public static HistoryItemRespVO toResponse(HistoryItemDTO source) {
        HistoryItemRespVO response = copy(source, HistoryItemRespVO::new, "roomGraphVoList");
        if (response != null) {
            response.setRoomGraphVoList(map(source.getRoomGraphVoList(), AppApiAssembler::toResponse));
        }
        return response;
    }

    public static List<AgreementItemRespVO> toAgreementList(List<AgreementItemDTO> source) {
        return map(source, AppApiAssembler::toResponse);
    }

    public static List<AppointmentItemRespVO> toAppointmentList(List<AppointmentItemDTO> source) {
        return map(source, AppApiAssembler::toResponse);
    }

    public static IPage<RoomItemRespVO> toRoomPage(IPage<RoomItemDTO> source) {
        return source == null ? null : source.convert(AppApiAssembler::toResponse);
    }

    public static IPage<HistoryItemRespVO> toHistoryPage(IPage<HistoryItemDTO> source) {
        return source == null ? null : source.convert(AppApiAssembler::toResponse);
    }

    private static GraphRespVO toResponse(GraphDTO source) {
        return copy(source, GraphRespVO::new);
    }

    private static AttrValueRespVO toResponse(AttrValueDTO source) {
        return copy(source, AttrValueRespVO::new);
    }

    private static FeeValueRespVO toResponse(FeeValueDTO source) {
        return copy(source, FeeValueRespVO::new);
    }

    private static LabelRespVO toResponse(LabelInfo source) {
        return copy(source, LabelRespVO::new);
    }

    private static FacilityRespVO toResponse(FacilityInfo source) {
        return copy(source, FacilityRespVO::new);
    }

    private static PaymentTypeRespVO toResponse(PaymentType source) {
        return copy(source, PaymentTypeRespVO::new);
    }

    private static LeaseTermRespVO toResponse(LeaseTerm source) {
        return copy(source, LeaseTermRespVO::new);
    }

    private static <S, T> T copy(S source, Supplier<T> factory, String... ignoredProperties) {
        if (source == null) {
            return null;
        }
        T target = factory.get();
        BeanUtils.copyProperties(source, target, ignoredProperties);
        return target;
    }

    private static <S, T> List<T> map(List<S> source, Function<S, T> converter) {
        return source == null ? null : source.stream().map(converter).collect(Collectors.toList());
    }
}
