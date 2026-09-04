package com.wxy.zzarental.web.app.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxy.zzarental.model.entity.*;
import com.wxy.zzarental.model.enums.ReleaseStatus;
import com.wxy.zzarental.web.app.mapper.*;
import com.wxy.zzarental.web.app.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxy.zzarental.web.app.vo.apartment.ApartmentItemVo;
import com.wxy.zzarental.web.app.vo.graph.GraphVo;
import com.wxy.zzarental.web.app.vo.room.RoomDetailVo;
import com.wxy.zzarental.web.app.vo.room.RoomItemVo;
import com.wxy.zzarental.web.app.vo.room.RoomQueryVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    @Resource
    private ApartmentInfoService apartmentInfoService;
    @Resource
    private AttrValueService attrValueService;
    @Resource
    private FacilityInfoService facilityInfoService;
    @Resource
    private LabelInfoService labelInfoService;
    @Resource
    private PaymentTypeService paymentTypeService;
    @Resource
    private FeeValueService feeValueService;
    @Resource
    private LeaseTermService leaseTermService;
    @Resource
    private GraphInfoService graphInfoService;

    @Override
    public IPage<RoomItemVo> pageItem(Page<RoomItemVo> page, RoomQueryVo queryVo) {
        //条件有：省市区，最大租金，最小租金，支付方式，排序
        // 由于查询条件涉及到公寓表的省市区，房间表的租金，支付方式表的支付方式，所以lambdawrapper无法写分页，必须写sql
        // 分页查询出IPage<RoomItemVo>，但是只查询出RoomItemVo中的房间id、房间号、房间租金
        //支付方式，PaymentTyped的id就是queryvo里面有paymentTypeId,如果要用的就是id，为什么还需要查出来，这个条件是不是拼接再要啊
        List<Long> payRoomIds = new ArrayList<>();
        if (queryVo.getPaymentTypeId() != null) {
            LambdaQueryWrapper<RoomPaymentType> paymentTypeLambdaQueryWrapper = new LambdaQueryWrapper<>();
            paymentTypeLambdaQueryWrapper.eq(RoomPaymentType::getPaymentTypeId, queryVo.getPaymentTypeId());
            List<RoomPaymentType> paymentTypes = roomPaymentTypeMapper.selectList(paymentTypeLambdaQueryWrapper);
            if (CollUtil.isEmpty(paymentTypes)) {
                Page<RoomItemVo> pageEmpty = new Page<>(page.getCurrent(), page.getSize(), 0);
                pageEmpty.setRecords(new ArrayList<>());
                return pageEmpty;
            }
            payRoomIds = paymentTypes.stream().map(RoomPaymentType::getRoomId).distinct().toList();
        }
        IPage<RoomItemVo> roomInfoPage = roomInfoMapper.pageItem(page, queryVo, payRoomIds);

        // 如果分页返回空，则直接返回
        // 比如说现在这个roomInfoPage的地址值是0x1111，他里面的records的地址值是0x1234，我们这样去get相当于
        // 将records对象的引用指向roomInfoPage里面的records的地址值，也就是0x1234，此时records也是0x1234
        List<RoomItemVo> records = roomInfoPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return roomInfoPage;
        }

        // 因为他是对象，所以这里会直接将地址值传递给这个方法
        // 方法内部是对records做了处理，但是没有改变他的地址值
        setRoomItemVo(records);
        return roomInfoPage;
    }

    @Override
    public RoomDetailVo getDetailById(Long id) {
        RoomInfo roomInfo = roomInfoMapper.selectById(id);
        if (roomInfo == null) {
            return null;
        }
        RoomDetailVo roomDetailVo = new RoomDetailVo();
        BeanUtil.copyProperties(roomInfo, roomDetailVo);

        //公寓信息
        ApartmentItemVo apartmentItemVo = apartmentInfoService.getInfoById(roomInfo.getApartmentId());
        roomDetailVo.setApartmentItemVo(apartmentItemVo);

        //图片列表 List<GraphVo> graphVoList
        LambdaQueryWrapper<GraphInfo> graphInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemId, id);
        List<GraphInfo> graphInfos = graphInfoMapper.selectList(graphInfoLambdaQueryWrapper);
        List<GraphVo> graphVoList = BeanUtil.copyToList(graphInfos, GraphVo.class);
        roomDetailVo.setGraphVoList(graphVoList);

        //属性信息列表
        roomDetailVo.setAttrValueVoList(attrValueService.listByRoomId(id));

        // 配套信息列表 FacilityInfo
        roomDetailVo.setFacilityInfoList(facilityInfoService.listByRoomId(id));

        //标签信息列表
        roomDetailVo.setLabelInfoList(labelInfoService.listByRoomId(id));

        //支付方式列表
        roomDetailVo.setPaymentTypeList(paymentTypeService.getPaymentTypeByRoomId(id));

        //杂费列表
        roomDetailVo.setFeeValueVoList(feeValueService.listByApartmentId(roomInfo.getApartmentId()));

        //租期列表
        roomDetailVo.setLeaseTermList(leaseTermService.listByRoomId(id));

        return roomDetailVo;
    }

    @Override
    public IPage<RoomItemVo> pageItemByApartmentId(Page<RoomItemVo> page, Long id) {
        IPage<RoomItemVo> roomItemVoIPage = roomInfoMapper.pageItemByApartmentId(page, id);
        // 如果分页返回空，则直接返回
        List<RoomItemVo> records = roomItemVoIPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return roomItemVoIPage;
        }
        // 以下代码都一样，都是组装RoomItemVo的其他属性，所以我们可以提取成方法
        // 由于使用到的地方都是在我们这个类里面，所以直接提取成本类的私有方法
        // Java分为值传递和引用传递，我们这里是引用传递，所以直接传records，实际上传的就是roomItemVoIPage里面的，而不是新对象
        setRoomItemVo(records);

        return roomItemVoIPage;
    }

    /**
     * 设置房间项目vo
     *
     * @param records 记录
     * @author wxy
     * @date 2026/09/04
     */
    private void setRoomItemVo(List<RoomItemVo> records) {
        // 这里传递进来的参数的地址值就是0x1234
        List<Long> roomIdList = records.stream().map(RoomItemVo::getId).toList();
        Map<Long, List<GraphInfo>> graphMap = graphInfoService.mapByItemIds(2,roomIdList);


        //标签
        LambdaQueryWrapper<RoomLabel> roomLabelLambdaQueryWrapper = new LambdaQueryWrapper<>();
        roomLabelLambdaQueryWrapper.in(RoomLabel::getRoomId, roomIdList);
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
        //组装，用set，有三个map,roomitemvo是一个集合，我们应该是给其中的每一个组装
        // 这里通过for循环去对records进行赋值操作，只是遍历了他，所以records还是0x1234
        records.forEach(roomItemVo -> {
            //图片
            List<GraphVo> graphList = graphMap.getOrDefault(roomItemVo.getId(), new ArrayList<>()).stream().map(
                    graphInfo -> {
                        GraphVo graphVo = new GraphVo();
                        graphVo.setName(graphInfo.getName());
                        graphVo.setUrl(graphInfo.getUrl());
                        return graphVo;
                    }).toList();

            roomItemVo.setGraphVoList(graphList);

            //标签
            roomItemVo.setLabelInfoList(labelMap.get(roomItemVo.getId()));
        });
    }
}




