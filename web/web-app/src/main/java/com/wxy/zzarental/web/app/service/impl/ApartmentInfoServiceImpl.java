package com.wxy.zzarental.web.app.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wxy.zzarental.model.entity.ApartmentInfo;
import com.wxy.zzarental.model.entity.ApartmentLabel;
import com.wxy.zzarental.model.entity.GraphInfo;
import com.wxy.zzarental.model.entity.LabelInfo;
import com.wxy.zzarental.web.app.mapper.*;
import com.wxy.zzarental.web.app.service.ApartmentInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxy.zzarental.web.app.vo.apartment.ApartmentItemVo;
import com.wxy.zzarental.web.app.vo.graph.GraphVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author liubo
 * @description 针对表【apartment_info(公寓信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
public class ApartmentInfoServiceImpl extends ServiceImpl<ApartmentInfoMapper, ApartmentInfo>
        implements ApartmentInfoService {

    @Resource
    private ApartmentLabelMapper apartmentLabelMapper;
    @Resource
    private GraphInfoMapper graphInfoMapper;
    @Resource
    private LabelInfoMapper labelInfoMapper;
    @Resource
    private RoomInfoMapper roomInfoMapper;

    @Override
    public ApartmentItemVo getInfoById(Long apartmentId) {
        ApartmentInfo apartmentInfo = getById(apartmentId);
        if (apartmentInfo == null) {
            return null;
        }
        ApartmentItemVo apartmentItemVo = new ApartmentItemVo();
        BeanUtil.copyProperties(apartmentInfo, apartmentItemVo);
        //公寓的labelInfoList，graphVoList，minRent
        LambdaQueryWrapper<GraphInfo> graphVoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        graphVoLambdaQueryWrapper.eq(GraphInfo::getItemType, 1);
        graphVoLambdaQueryWrapper.eq(GraphInfo::getItemId, apartmentInfo.getId());
        List<GraphInfo> graphInfos = graphInfoMapper.selectList(graphVoLambdaQueryWrapper);
        List<GraphVo> graphVos = graphInfos.stream().map(
                graphInfo -> {
                    GraphVo graphVo = new GraphVo();
                    graphVo.setName(graphInfo.getName());
                    graphVo.setUrl(graphInfo.getUrl());
                    return graphVo;
                }).toList();
        apartmentItemVo.setGraphVoList(graphVos);

        //公寓标签
        LambdaQueryWrapper<ApartmentLabel> apartmentLabelLambdaQueryWrapper = new LambdaQueryWrapper<>();
        apartmentLabelLambdaQueryWrapper.eq(ApartmentLabel::getApartmentId, apartmentInfo.getId());
        List<ApartmentLabel> apartmentLabels = apartmentLabelMapper.selectList(apartmentLabelLambdaQueryWrapper);
        Set<Long> labelIds = apartmentLabels.stream().map(ApartmentLabel::getLabelId).collect(Collectors.toSet());
        List<LabelInfo> labelInfos = labelInfoMapper.selectBatchIds(labelIds);
        apartmentItemVo.setLabelInfoList(labelInfos);

        //最低租金
        BigDecimal minRent = roomInfoMapper.selectMinRent(apartmentInfo.getId());
        apartmentItemVo.setMinRent(minRent);
        return apartmentItemVo;
    }
}




