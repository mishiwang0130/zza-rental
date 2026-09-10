package com.wxy.zzarental.web.admin.controller.assembler;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wxy.zzarental.common.util.VOConverter;
import com.wxy.zzarental.web.admin.service.command.ApartmentSaveCommand;
import com.wxy.zzarental.web.admin.service.command.LoginCommand;
import com.wxy.zzarental.web.admin.service.command.RoomSaveCommand;
import com.wxy.zzarental.web.admin.service.dto.AgreementDTO;
import com.wxy.zzarental.web.admin.service.dto.ApartmentDetailDTO;
import com.wxy.zzarental.web.admin.service.dto.ApartmentItemDTO;
import com.wxy.zzarental.web.admin.service.dto.AppointmentDTO;
import com.wxy.zzarental.web.admin.service.dto.AttrKeyDTO;
import com.wxy.zzarental.web.admin.service.dto.AttrValueDTO;
import com.wxy.zzarental.web.admin.service.dto.CaptchaDTO;
import com.wxy.zzarental.web.admin.service.dto.FeeKeyDTO;
import com.wxy.zzarental.web.admin.service.dto.FeeValueDTO;
import com.wxy.zzarental.web.admin.service.dto.GraphDTO;
import com.wxy.zzarental.web.admin.service.dto.RoomDetailDTO;
import com.wxy.zzarental.web.admin.service.dto.RoomItemDTO;
import com.wxy.zzarental.web.admin.service.dto.SystemPostItemDTO;
import com.wxy.zzarental.web.admin.service.dto.SystemUserInfoDTO;
import com.wxy.zzarental.web.admin.service.dto.SystemUserItemDTO;
import com.wxy.zzarental.web.admin.service.query.AgreementQuery;
import com.wxy.zzarental.web.admin.service.query.ApartmentQuery;
import com.wxy.zzarental.web.admin.service.query.AppointmentQuery;
import com.wxy.zzarental.web.admin.service.query.RoomQuery;
import com.wxy.zzarental.web.admin.service.query.SystemUserQuery;
import com.wxy.zzarental.web.admin.vo.agreement.AgreementPageReqVO;
import com.wxy.zzarental.web.admin.vo.agreement.AgreementRespVO;
import com.wxy.zzarental.web.admin.vo.apartment.ApartmentBasicRespVO;
import com.wxy.zzarental.web.admin.vo.apartment.ApartmentDetailRespVO;
import com.wxy.zzarental.web.admin.vo.apartment.ApartmentItemRespVO;
import com.wxy.zzarental.web.admin.vo.apartment.ApartmentPageReqVO;
import com.wxy.zzarental.web.admin.vo.apartment.ApartmentSaveReqVO;
import com.wxy.zzarental.web.admin.vo.apartment.FacilityRespVO;
import com.wxy.zzarental.web.admin.vo.apartment.LabelRespVO;
import com.wxy.zzarental.web.admin.vo.apartment.LeaseTermRespVO;
import com.wxy.zzarental.web.admin.vo.apartment.PaymentTypeRespVO;
import com.wxy.zzarental.web.admin.vo.appointment.AppointmentPageReqVO;
import com.wxy.zzarental.web.admin.vo.appointment.AppointmentRespVO;
import com.wxy.zzarental.web.admin.vo.attr.AttrKeyRespVO;
import com.wxy.zzarental.web.admin.vo.attr.AttrValueRespVO;
import com.wxy.zzarental.web.admin.vo.fee.FeeKeyRespVO;
import com.wxy.zzarental.web.admin.vo.fee.FeeValueRespVO;
import com.wxy.zzarental.web.admin.vo.graph.GraphReqVO;
import com.wxy.zzarental.web.admin.vo.graph.GraphRespVO;
import com.wxy.zzarental.web.admin.vo.login.CaptchaRespVO;
import com.wxy.zzarental.web.admin.vo.login.LoginReqVO;
import com.wxy.zzarental.web.admin.vo.room.RoomBasicRespVO;
import com.wxy.zzarental.web.admin.vo.room.RoomDetailRespVO;
import com.wxy.zzarental.web.admin.vo.room.RoomItemRespVO;
import com.wxy.zzarental.web.admin.vo.room.RoomPageReqVO;
import com.wxy.zzarental.web.admin.vo.room.RoomSaveReqVO;
import com.wxy.zzarental.web.admin.vo.system.user.SystemPostItemRespVO;
import com.wxy.zzarental.web.admin.vo.system.user.SystemUserInfoRespVO;
import com.wxy.zzarental.web.admin.vo.system.user.SystemUserItemRespVO;
import com.wxy.zzarental.web.admin.vo.system.user.SystemUserPageReqVO;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;

/**
 * HTTP 模型与应用服务模型的边界。嵌套对象在此显式转换，接口字段和分页结构保持不变。
 */
public final class AdminApiAssembler {

    private AdminApiAssembler() {
    }

    public static ApartmentDetailRespVO toResponse(ApartmentDetailDTO source) {
        if (source == null) {
            return null;
        }
        ApartmentDetailRespVO target = new ApartmentDetailRespVO();
        BeanUtils.copyProperties(source, target);
        target.setGraphVoList(toList(source.getGraphVoList(), AdminApiAssembler::toResponse));
        target.setLabelInfoList(VOConverter.toList(source.getLabelInfoList(), LabelRespVO.class));
        target.setFacilityInfoList(VOConverter.toList(source.getFacilityInfoList(), FacilityRespVO.class));
        target.setFeeValueVoList(toList(source.getFeeValueVoList(), AdminApiAssembler::toResponse));
        return target;
    }

    public static ApartmentItemRespVO toResponse(ApartmentItemDTO source) {
        if (source == null) {
            return null;
        }
        ApartmentItemRespVO target = new ApartmentItemRespVO();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static AgreementRespVO toResponse(AgreementDTO source) {
        if (source == null) {
            return null;
        }
        AgreementRespVO target = new AgreementRespVO();
        BeanUtils.copyProperties(source, target);
        target.setApartmentInfo(VOConverter.to(source.getApartmentInfo(), ApartmentBasicRespVO.class));
        target.setRoomInfo(VOConverter.to(source.getRoomInfo(), RoomBasicRespVO.class));
        target.setPaymentType(VOConverter.to(source.getPaymentType(), PaymentTypeRespVO.class));
        target.setLeaseTerm(VOConverter.to(source.getLeaseTerm(), LeaseTermRespVO.class));
        return target;
    }

    public static AppointmentRespVO toResponse(AppointmentDTO source) {
        if (source == null) {
            return null;
        }
        AppointmentRespVO target = new AppointmentRespVO();
        BeanUtils.copyProperties(source, target);
        target.setApartmentInfo(VOConverter.to(source.getApartmentInfo(), ApartmentBasicRespVO.class));
        return target;
    }

    public static AttrKeyRespVO toResponse(AttrKeyDTO source) {
        if (source == null) {
            return null;
        }
        AttrKeyRespVO target = new AttrKeyRespVO();
        BeanUtils.copyProperties(source, target);
        target.setAttrValueList(toList(source.getAttrValueList(), AdminApiAssembler::toResponse));
        return target;
    }

    public static AttrValueRespVO toResponse(AttrValueDTO source) {
        if (source == null) {
            return null;
        }
        AttrValueRespVO target = new AttrValueRespVO();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static FeeKeyRespVO toResponse(FeeKeyDTO source) {
        if (source == null) {
            return null;
        }
        FeeKeyRespVO target = new FeeKeyRespVO();
        BeanUtils.copyProperties(source, target);
        target.setFeeValueList(toList(source.getFeeValueList(), AdminApiAssembler::toResponse));
        return target;
    }

    public static FeeValueRespVO toResponse(FeeValueDTO source) {
        if (source == null) {
            return null;
        }
        FeeValueRespVO target = new FeeValueRespVO();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static GraphRespVO toResponse(GraphDTO source) {
        if (source == null) {
            return null;
        }
        GraphRespVO target = new GraphRespVO();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static RoomDetailRespVO toResponse(RoomDetailDTO source) {
        if (source == null) {
            return null;
        }
        RoomDetailRespVO target = new RoomDetailRespVO();
        BeanUtils.copyProperties(source, target);
        target.setApartmentInfo(VOConverter.to(source.getApartmentInfo(), ApartmentBasicRespVO.class));
        target.setGraphVoList(toList(source.getGraphVoList(), AdminApiAssembler::toResponse));
        target.setAttrValueVoList(toList(source.getAttrValueVoList(), AdminApiAssembler::toResponse));
        target.setFacilityInfoList(VOConverter.toList(source.getFacilityInfoList(), FacilityRespVO.class));
        target.setLabelInfoList(VOConverter.toList(source.getLabelInfoList(), LabelRespVO.class));
        target.setPaymentTypeList(VOConverter.toList(source.getPaymentTypeList(), PaymentTypeRespVO.class));
        target.setLeaseTermList(VOConverter.toList(source.getLeaseTermList(), LeaseTermRespVO.class));
        return target;
    }

    public static RoomItemRespVO toResponse(RoomItemDTO source) {
        if (source == null) {
            return null;
        }
        RoomItemRespVO target = new RoomItemRespVO();
        BeanUtils.copyProperties(source, target);
        target.setApartmentInfo(VOConverter.to(source.getApartmentInfo(), ApartmentBasicRespVO.class));
        return target;
    }

    public static SystemPostItemRespVO toResponse(SystemPostItemDTO source) {
        if (source == null) {
            return null;
        }
        SystemPostItemRespVO target = new SystemPostItemRespVO();
        BeanUtils.copyProperties(source, target);
        target.setSystemUsers(toList(source.getSystemUsers(), AdminApiAssembler::toResponse));
        return target;
    }

    public static SystemUserItemRespVO toResponse(SystemUserItemDTO source) {
        if (source == null) {
            return null;
        }
        SystemUserItemRespVO target = new SystemUserItemRespVO();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static SystemUserInfoRespVO toResponse(SystemUserInfoDTO source) {
        if (source == null) {
            return null;
        }
        SystemUserInfoRespVO target = new SystemUserInfoRespVO();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static CaptchaRespVO toResponse(CaptchaDTO source) {
        return source == null ? null : new CaptchaRespVO(source.getImage(), source.getKey());
    }

    public static ApartmentQuery toQuery(ApartmentPageReqVO source) {
        if (source == null) {
            return null;
        }
        ApartmentQuery target = new ApartmentQuery();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static AgreementQuery toQuery(AgreementPageReqVO source) {
        if (source == null) {
            return null;
        }
        AgreementQuery target = new AgreementQuery();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static AppointmentQuery toQuery(AppointmentPageReqVO source) {
        if (source == null) {
            return null;
        }
        AppointmentQuery target = new AppointmentQuery();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static RoomQuery toQuery(RoomPageReqVO source) {
        if (source == null) {
            return null;
        }
        RoomQuery target = new RoomQuery();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static SystemUserQuery toQuery(SystemUserPageReqVO source) {
        if (source == null) {
            return null;
        }
        SystemUserQuery target = new SystemUserQuery();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static ApartmentSaveCommand toCommand(ApartmentSaveReqVO source) {
        if (source == null) {
            return null;
        }
        ApartmentSaveCommand target = new ApartmentSaveCommand();
        BeanUtils.copyProperties(source, target);
        target.setGraphVoList(toList(source.getGraphVoList(), AdminApiAssembler::toGraph));
        return target;
    }

    public static RoomSaveCommand toCommand(RoomSaveReqVO source) {
        if (source == null) {
            return null;
        }
        RoomSaveCommand target = new RoomSaveCommand();
        BeanUtils.copyProperties(source, target);
        target.setGraphVoList(toList(source.getGraphVoList(), AdminApiAssembler::toGraph));
        return target;
    }

    public static LoginCommand toCommand(LoginReqVO source) {
        if (source == null) {
            return null;
        }
        LoginCommand target = new LoginCommand();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    private static GraphDTO toGraph(GraphReqVO source) {
        if (source == null) {
            return null;
        }
        GraphDTO target = new GraphDTO();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <S, T> List<T> toList(List<S> source, Function<S, T> mapper) {
        return source == null ? null : source.stream().map(mapper).collect(Collectors.toList());
    }

    public static <S, T> IPage<T> toPage(IPage<S> source, Function<S, T> mapper) {
        return source == null ? null : source.convert(mapper);
    }
}
