package com.wxy.zzarental.web.app.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.graph.Graph;
import com.wxy.zzarental.model.entity.*;
import com.wxy.zzarental.model.enums.ReleaseStatus;
import com.wxy.zzarental.web.app.mapper.*;
import com.wxy.zzarental.web.app.service.RoomInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxy.zzarental.web.app.vo.apartment.ApartmentItemVo;
import com.wxy.zzarental.web.app.vo.attr.AttrValueVo;
import com.wxy.zzarental.web.app.vo.fee.FeeValueVo;
import com.wxy.zzarental.web.app.vo.graph.GraphVo;
import com.wxy.zzarental.web.app.vo.room.RoomDetailVo;
import com.wxy.zzarental.web.app.vo.room.RoomItemVo;
import com.wxy.zzarental.web.app.vo.room.RoomQueryVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author liubo
 * @description 针对表【room_info(房间信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
@Slf4j
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo>
        implements RoomInfoService {

    @Resource
    private ApartmentInfoMapper apartmentInfoMapper;
    @Resource
    private RoomInfoMapper roomInfoMapper;
    @Resource
    private RoomPaymentTypeMapper roomPaymentTypeMapper;
    @Resource
    private GraphInfoMapper graphInfoMapper;
    @Resource
    private RoomLabelMapper roomLabelMapper;
    @Resource
    private LabelInfoMapper labelInfoMapper;

    @Override
    public IPage<RoomItemVo> pageItem(Page<RoomItemVo> page, RoomQueryVo queryVo) {
        //条件有：省市区，最大租金，最小租金，支付方式，排序
        // 由于查询条件涉及到公寓表的省市区，房间表的租金，支付方式表的支付方式，所以lambdawrapper无法写分页，必须写sql
        // 分页查询出IPage<RoomItemVo>，但是只查询出RoomItemVo中的房间id、房间号、房间租金

        // 如果分页返回空，则直接返回

        // 分页不为空，给IPage<RoomItemVo>的每个RoomItemVo组装房间图片列表、房间标签列表、房间所属公寓信息

        // 1.先获取IPage<RoomItemVo>中涉及到的所有房间的房间图片列表
        // 1.1 将房间图片列表转成map

        // 2.

        //省市区：roominfo里面有公寓id，可以通过公寓id查到apartmentinfo，公寓表里有省市区的名称和id，我们应该是通过id
        //判断roomqueryvo里面有没有创省市区
        List<Long> apartmentIds = new ArrayList<>();
        if(queryVo.getProvinceId() != null || queryVo.getCityId() != null || queryVo.getDistrictId() != null){
            LambdaQueryWrapper<ApartmentInfo> apartmentInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
            //公寓要查已经发布的
            apartmentInfoLambdaQueryWrapper.eq(ApartmentInfo::getIsRelease, ReleaseStatus.RELEASED);
            //省市区的比较
            apartmentInfoLambdaQueryWrapper.eq(queryVo.getProvinceId() != null,ApartmentInfo::getProvinceId,queryVo.getProvinceId());
            apartmentInfoLambdaQueryWrapper.eq(queryVo.getCityId() != null,ApartmentInfo::getCityId,queryVo.getCityId());
            apartmentInfoLambdaQueryWrapper.eq(queryVo.getDistrictId() != null,ApartmentInfo::getDistrictId,queryVo.getDistrictId());

            List<ApartmentInfo> apartmentInfos = apartmentInfoMapper.selectList(apartmentInfoLambdaQueryWrapper);
            //查到的公寓是空的就放回空list
            if (CollUtil.isEmpty(apartmentInfos)){
                Page<RoomItemVo> pageEmpty = new Page<RoomItemVo>(page.getCurrent(), page.getSize(), 0);
                pageEmpty.setRecords(new ArrayList<>());
                return pageEmpty;
            }
            //满足条件的公寓就是最后要的公寓，取公寓id，用于下面房间
            apartmentIds = apartmentInfos.stream().map(BaseEntity::getId).collect(Collectors.toList());
        }
        //租金范围,比较rent，也有条件isRelease
        LambdaQueryWrapper<RoomInfo> roomInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        roomInfoLambdaQueryWrapper.eq(RoomInfo::getIsRelease,ReleaseStatus.RELEASED);
        //房间表有所属公寓id，所以我们要在上面确定了的公寓里面找房间,，如果有公寓id才需在这里
        if(CollUtil.isNotEmpty(apartmentIds)) {
            roomInfoLambdaQueryWrapper.in(RoomInfo::getApartmentId, apartmentIds);
        }
        //租金上限和下线
        roomInfoLambdaQueryWrapper.le(queryVo.getMaxRent()!=null,RoomInfo::getRent,queryVo.getMaxRent());
        roomInfoLambdaQueryWrapper.ge(queryVo.getMinRent()!=null,RoomInfo::getRent,queryVo.getMinRent());
        //排序
        if ("desc".equals(queryVo.getOrderType())){
            roomInfoLambdaQueryWrapper.orderByDesc(RoomInfo::getRent);
        }else {
            roomInfoLambdaQueryWrapper.orderByAsc(RoomInfo::getRent);
        }
        //支付方式，PaymentTyped的id就是queryvo里面有paymentTypeId,如果要用的就是id，为什么还需要查出来，这个条件是不是拼接再要啊
        if (queryVo.getPaymentTypeId()!= null) {
            LambdaQueryWrapper<RoomPaymentType> paymentTypeLambdaQueryWrapper = new LambdaQueryWrapper<>();
            paymentTypeLambdaQueryWrapper.eq(RoomPaymentType::getPaymentTypeId, queryVo.getPaymentTypeId());
            List<RoomPaymentType> paymentTypes = roomPaymentTypeMapper.selectList(paymentTypeLambdaQueryWrapper);
            if (CollUtil.isEmpty(paymentTypes)) {
                Page<RoomItemVo> pageEmpty = new Page<>(page.getCurrent(), page.getSize(), 0);
                pageEmpty.setRecords(new ArrayList<>());
                return pageEmpty;
            }
            List<Long> payRoomIds = paymentTypes.stream().map(RoomPaymentType::getRoomId).distinct().toList();
            roomInfoLambdaQueryWrapper.in(BaseEntity::getId, payRoomIds);
        }
        //主表分页查询
        Page<RoomInfo> roomPage = new Page<>(page.getCurrent(), page.getSize());
        Page<RoomInfo> roomInfoPage = roomInfoMapper.selectPage(roomPage, roomInfoLambdaQueryWrapper);

        //查关联数据，需要房间id
        List<Long> roomIds = roomInfoPage.getRecords().stream().map(BaseEntity::getId).distinct().collect(Collectors.toList());
        //公寓id
        List<Long> nowApartmentIds = roomInfoPage.getRecords().stream().map(RoomInfo::getApartmentId).distinct().collect(Collectors.toList());
        //图片，标签，公寓
        //图片List<GraphVo> graphVoList
//        List<SystemPost> systemPosts = systemPostMapper.selectBatchIds(postIds);
//        // 将postList转换为map
//        Map<Long,String> postNameMap = systemPosts.stream().collect(Collectors.toMap(SystemPost::getId,SystemPost::getName)) ;

        //得到图片的ids
        LambdaQueryWrapper<GraphInfo> graphVoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        graphVoLambdaQueryWrapper.eq(GraphInfo::getItemType,2);
        graphVoLambdaQueryWrapper.in(GraphInfo::getItemId,roomIds);
        List<GraphInfo> graphInfos = graphInfoMapper.selectList(graphVoLambdaQueryWrapper);
        Map<Long,List<GraphInfo> > graphMap = graphInfos.stream().collect(Collectors.groupingBy(GraphInfo::getItemId));


        //标签
        LambdaQueryWrapper<RoomLabel> roomLabelLambdaQueryWrapper = new LambdaQueryWrapper<>();
        roomLabelLambdaQueryWrapper.in(RoomLabel::getRoomId,roomIds);
        // 不需要显示把删除字段写出来，mybatisplus会自己加
//        roomLabelLambdaQueryWrapper.eq(BaseEntity::getIsDeleted,0);
        List<RoomLabel> roomLabels = roomLabelMapper.selectList(roomLabelLambdaQueryWrapper);
        // 转换成房间id和标签列表的map
        Map<Long, List<RoomLabel>> roomLabelMap = roomLabels.stream().collect(Collectors.groupingBy(RoomLabel::getRoomId));
        // 获取到房间标签关联列表后，由于返回值需要的是标签信息，所以要取出列表中的标签id，去查询标签表
        // 收集标签id集合
        Set<Long> labelIdSet = roomLabels.stream().map(RoomLabel::getLabelId).collect(Collectors.toSet());
        // 根据标签id查询标签列表
        List<LabelInfo> labelInfos = labelInfoMapper.selectBatchIds(labelIdSet);
        // 转换成map
        Map<Long, LabelInfo> labelInfoMap = labelInfos.stream().collect(Collectors.toMap(BaseEntity::getId, Function.identity(), (key1, key2) -> key1));

        // 由于需要的不是Map<房间id, 房间标签关联集合>而是Map<房间id, 标签信息集合>
        // 所以要将Map<房间id, 房间标签关联集合>转换为Map<房间id, 标签信息集合>
        // 最终需要的map
        Map<Long, List<LabelInfo>> labelMap = new HashMap<>();
        // 遍历roomLabelMap，将每一个value转换成需要的标签信息集合
        roomLabelMap.forEach((key, roomLabelList) -> {
            List<LabelInfo> list = roomLabelList.stream()
                    .map(item -> labelInfoMap.get(item.getLabelId())).toList();
            labelMap.put(key, list);
        });

        //公寓详情ApartmentInfo,用查到的公寓id就可以了
        List<ApartmentInfo> apartmentInfos = apartmentInfoMapper.selectBatchIds(nowApartmentIds);
        Map<Long,ApartmentInfo> apartmentMap = apartmentInfos.stream().collect(Collectors.toMap(BaseEntity::getId, Function.identity()));


        //组装，用set，有三个map,roomitemvo是一个集合，我们应该是给其中的每一个组装
        List<RoomItemVo> voList = roomInfoPage.getRecords().stream().map(roomInfo -> {
            RoomItemVo roomItemVo = new RoomItemVo();
            roomItemVo.setId(roomInfo.getId());
            roomItemVo.setRoomNumber(roomInfo.getRoomNumber());
            roomItemVo.setRent(roomInfo.getRent());
            //图片
            List<GraphVo> graphList = graphMap.getOrDefault(roomInfo.getId(), new ArrayList<>()).stream().map(
                    graphInfo -> {
                        GraphVo graphVo = new GraphVo();
                        graphVo.setName(graphInfo.getName());
                        graphVo.setUrl(graphInfo.getUrl());
                        return graphVo;
                    }).toList();

            roomItemVo.setGraphVoList(graphList);

            //标签
            roomItemVo.setLabelInfoList(labelMap.get(roomInfo.getId()));
            //公寓信息
            roomItemVo.setApartmentInfo(apartmentMap.get(roomInfo.getId()));
            return roomItemVo;
        }).collect(Collectors.toList());


        Page<RoomItemVo> result = new Page<>(page.getCurrent(), page.getSize(), roomInfoPage.getTotal());
        result.setRecords(voList);


        return result;
    }

    @Resource
    private RoomAttrValueMapper roomAttrValueMapper;
    @Resource
    private RoomFacilityMapper roomFacilityMapper;
    @Resource
    private FacilityInfoMapper facilityInfoMapper;
    @Resource
    private ApartmentLabelMapper apartmentLabelMapper;
    @Resource
    private AttrValueMapper attrValueMapper;
    @Resource
    private AttrKeyMapper attrKeyMapper;
    @Resource
    private PaymentTypeMapper paymentTypeMapper;
    @Resource
    private ApartmentFeeValueMapper apartmentFeeValueMapper;
    @Resource
    private FeeValueMapper feeValueMapper;
    @Resource
    private FeeKeyMapper feeKeyMapper;
    @Resource
    private RoomLeaseTermMapper roomLeaseTermMapper;
    @Resource
    private LeaseTermMapper leaseTermMapper;

    @Override
    public RoomDetailVo getDetailById(Long id) {
        RoomInfo roomInfo = roomInfoMapper.selectById(id);
        if (roomInfo==null){
            return null;
        }
        RoomDetailVo roomDetailVo = new RoomDetailVo();
        BeanUtil.copyProperties(roomInfo,roomDetailVo);

        //公寓信息
        ApartmentItemVo apartmentItemVo = getApartmentItemVo(roomInfo);
        roomDetailVo.setApartmentItemVo(apartmentItemVo);

        //图片列表 List<GraphVo> graphVoList
        LambdaQueryWrapper<GraphInfo> graphInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemId,id);
        List<GraphInfo> graphInfos = graphInfoMapper.selectList(graphInfoLambdaQueryWrapper);
        List<GraphVo> graphVoList = BeanUtil.copyToList(graphInfos, GraphVo.class);
        roomDetailVo.setGraphVoList(graphVoList);

        //属性信息列表
        LambdaQueryWrapper<RoomAttrValue> attrValueVoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        attrValueVoLambdaQueryWrapper.eq(RoomAttrValue::getRoomId,id);
        List<RoomAttrValue> roomAttrValues = roomAttrValueMapper.selectList(attrValueVoLambdaQueryWrapper);
        Set<Long> attrValueIds = roomAttrValues.stream().map(RoomAttrValue::getAttrValueId).collect(Collectors.toSet());
        List<AttrValue> attrValues = attrValueMapper.selectBatchIds(attrValueIds);
        List<AttrValueVo> attrValueVos = attrValues.stream().map(
                attrValue -> {
                    AttrValueVo attrValueVo = new AttrValueVo();
                    BeanUtil.copyProperties(attrValue, attrValueVo);
                    //通过attrKeyid去attr-key表查attrKeyName
                    AttrKey attrKey = attrKeyMapper.selectById(attrValue.getAttrKeyId());
                    attrValueVo.setAttrKeyName(attrKey.getName());
                    return attrValueVo;
                }
        ).toList();

        roomDetailVo.setAttrValueVoList(attrValueVos);

        //  配套信息列表 FacilityInfo
        LambdaQueryWrapper<RoomFacility> roomFacilityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        roomFacilityLambdaQueryWrapper.eq(RoomFacility::getRoomId,id);
        List<RoomFacility> roomFacilities = roomFacilityMapper.selectList(roomFacilityLambdaQueryWrapper);
        List<Long> facilityIds = roomFacilities.stream().map(RoomFacility::getFacilityId).collect(Collectors.toList());
        List<FacilityInfo> facilityInfos = facilityInfoMapper.selectBatchIds(facilityIds);
        roomDetailVo.setFacilityInfoList(facilityInfos);
        //标签信息列表
        LambdaQueryWrapper<RoomLabel> roomLabelLambdaQueryWrapper = new LambdaQueryWrapper<>();
        roomLabelLambdaQueryWrapper.eq(RoomLabel::getRoomId,id);
        List<RoomLabel> roomLabels = roomLabelMapper.selectList(roomLabelLambdaQueryWrapper);
        List<Long> labelIds = roomLabels.stream().map(RoomLabel::getLabelId).toList();
        List<LabelInfo> labelInfos = labelInfoMapper.selectBatchIds(labelIds);
        roomDetailVo.setLabelInfoList(labelInfos);

        //支付方式列表
        LambdaQueryWrapper<RoomPaymentType> roomPaymentTypeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        roomPaymentTypeLambdaQueryWrapper.eq(RoomPaymentType::getRoomId,id);
        List<RoomPaymentType> roomPaymentTypes = roomPaymentTypeMapper.selectList(roomPaymentTypeLambdaQueryWrapper);
        List<Long> paymentTypeIds = roomPaymentTypes.stream().map(RoomPaymentType::getPaymentTypeId).toList();
        List<PaymentType> paymentTypes = paymentTypeMapper.selectBatchIds(paymentTypeIds);
        roomDetailVo.setPaymentTypeList(paymentTypes);
        //杂费列表
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(roomInfo.getApartmentId());
        LambdaQueryWrapper<ApartmentFeeValue> apartmentFeeValueLambdaQueryWrapper = new LambdaQueryWrapper<>();
        apartmentFeeValueLambdaQueryWrapper.eq(ApartmentFeeValue::getApartmentId,roomInfo.getApartmentId());
        List<ApartmentFeeValue> apartmentFeeValues = apartmentFeeValueMapper.selectList(apartmentFeeValueLambdaQueryWrapper);
        List<Long> feevalueIds = apartmentFeeValues.stream().map(ApartmentFeeValue::getFeeValueId).toList();
        List<FeeValue> feeValues = feeValueMapper.selectBatchIds(feevalueIds);
        List<FeeValueVo> feeValueVos = feeValues.stream().map(
                feeValue -> {
                    FeeValueVo feeValueVo = new FeeValueVo();
                    BeanUtil.copyProperties(feeValue, feeValueVo);
                    FeeKey feeKey = feeKeyMapper.selectById(feeValue.getFeeKeyId());
                    feeValueVo.setFeeKeyName(feeKey.getName());
                    return feeValueVo;
                }
        ).toList();
        roomDetailVo.setFeeValueVoList(feeValueVos);

        //租期列表
        LambdaQueryWrapper<RoomLeaseTerm> roomLeaseTermLambdaQueryWrapper = new LambdaQueryWrapper<>();
        roomLeaseTermLambdaQueryWrapper.eq(RoomLeaseTerm::getRoomId,id);
        List<RoomLeaseTerm> roomLeaseTerms = roomLeaseTermMapper.selectList(roomLeaseTermLambdaQueryWrapper);
        List<Long> leaseTermIds = roomLeaseTerms.stream().map(RoomLeaseTerm::getLeaseTermId).toList();
        List<LeaseTerm> leaseTerms = leaseTermMapper.selectBatchIds(leaseTermIds);
        roomDetailVo.setLeaseTermList(leaseTerms);


        return roomDetailVo;
    }

    private ApartmentItemVo getApartmentItemVo(RoomInfo roomInfo) {
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(roomInfo.getApartmentId());
        ApartmentItemVo apartmentItemVo = new ApartmentItemVo();
        BeanUtil.copyProperties(apartmentInfo,apartmentItemVo);
        //公寓的labelInfoList，graphVoList，minRent
        LambdaQueryWrapper<GraphInfo> graphVoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        graphVoLambdaQueryWrapper.eq(GraphInfo::getItemType,1);
        graphVoLambdaQueryWrapper.eq(GraphInfo::getItemId,apartmentInfo.getId());
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
        apartmentLabelLambdaQueryWrapper.eq(ApartmentLabel::getApartmentId,apartmentInfo.getId());
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




