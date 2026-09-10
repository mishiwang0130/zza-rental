package com.wxy.zzarental.web.app.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wxy.zzarental.common.constant.MQConstant;
import com.wxy.zzarental.common.login.LoginUserHolder;
import com.wxy.zzarental.common.util.RedisKeyUtil;
import com.wxy.zzarental.common.util.RedisUtil;
import com.wxy.zzarental.common.util.VOConverter;
import com.wxy.zzarental.model.entity.*;
import com.wxy.zzarental.model.enums.ReleaseStatus;
import com.wxy.zzarental.web.app.mapper.*;
import com.wxy.zzarental.web.app.mq.message.SaveBrowsHistoryMQMsg;
import com.wxy.zzarental.web.app.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxy.zzarental.web.app.vo.apartment.ApartmentItemRespVO;
import com.wxy.zzarental.web.app.vo.common.FacilityRespVO;
import com.wxy.zzarental.web.app.vo.common.LabelRespVO;
import com.wxy.zzarental.web.app.vo.graph.GraphRespVO;
import com.wxy.zzarental.web.app.vo.leaseterm.LeaseTermRespVO;
import com.wxy.zzarental.web.app.vo.payment.PaymentTypeRespVO;
import com.wxy.zzarental.web.app.vo.room.RoomDetailRespVO;
import com.wxy.zzarental.web.app.vo.room.RoomItemRespVO;
import com.wxy.zzarental.web.app.vo.room.RoomPageReqVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
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
    @Resource
    private BrowsingHistoryService browsingHistoryService;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public IPage<RoomItemRespVO> pageItem(Page<RoomItemRespVO> page, RoomPageReqVO queryVo) {

        //缓存
        if(queryVo.getProvinceId() ==null && queryVo.getCityId() == null && queryVo.getDistrictId() == null &&
        queryVo.getPaymentTypeId() == null && StrUtil.isBlank(queryVo.getOrderType()) && queryVo.getMaxRent() == null &&
        queryVo.getMinRent() == null) {
            String jsonStr = redisUtil.get(RedisKeyUtil.getRoomPageKey(page.getCurrent(), page.getSize()));
            if (StrUtil.isNotBlank(jsonStr)) {
                Gson gson = new Gson();
                return gson.fromJson(jsonStr, new TypeToken<IPage<RoomItemRespVO>>() {
                }.getType());
            }
        }
        IPage<RoomItemRespVO> roomInfoPage = null;

//同步代码块欧克，我想起了一点点
        synchronized (Object.class) {
            List<Long> payRoomIds = new ArrayList<>();
            if (queryVo.getPaymentTypeId() != null) {
                LambdaQueryWrapper<RoomPaymentType> paymentTypeLambdaQueryWrapper = new LambdaQueryWrapper<>();
                paymentTypeLambdaQueryWrapper.eq(RoomPaymentType::getPaymentTypeId, queryVo.getPaymentTypeId());
                List<RoomPaymentType> paymentTypes = roomPaymentTypeMapper.selectList(paymentTypeLambdaQueryWrapper);
                if (CollUtil.isEmpty(paymentTypes)) {
                    Page<RoomItemRespVO> pageEmpty = new Page<>(page.getCurrent(), page.getSize(), 0);
                    pageEmpty.setRecords(new ArrayList<>());
                    return pageEmpty;
                }
                payRoomIds = paymentTypes.stream().map(RoomPaymentType::getRoomId).distinct().toList();
            }
            roomInfoPage = roomInfoMapper.pageItem(page, queryVo, payRoomIds);

            // 如果分页返回空，则直接返回
            // 比如说现在这个roomInfoPage的地址值是0x1111，他里面的records的地址值是0x1234，我们这样去get相当于
            // 将records对象的引用指向roomInfoPage里面的records的地址值，也就是0x1234，此时records也是0x1234
            List<RoomItemRespVO> records = roomInfoPage.getRecords();
            if (CollUtil.isEmpty(records)) {
                return roomInfoPage;
            }

            // 因为他是对象，所以这里会直接将地址值传递给这个方法
            // 方法内部是对records做了处理，但是没有改变他的地址值

            //最开始的不是默认的吗，那应该不需要key吧
            setRoomItemVo(records);
            if (queryVo.getProvinceId() == null && queryVo.getCityId() == null && queryVo.getDistrictId() == null &&
                    queryVo.getPaymentTypeId() == null && StrUtil.isBlank(queryVo.getOrderType()) && queryVo.getMaxRent() == null &&
                    queryVo.getMinRent() == null) {
                Gson gson = new Gson();
                String json = gson.toJson(roomInfoPage);
                redisUtil.set(RedisKeyUtil.getRoomPageKey(page.getCurrent(), page.getSize()), json, 60 * 60, TimeUnit.SECONDS);
            }
        }


        return roomInfoPage;
    }

    @Override
    public RoomDetailRespVO getDetailById(Long id) {
        // 1,2,3 数据库id
        // 定义布隆过滤器，那你怎么知道应该容量选多大
//        Integer arr[] = new Integer[100]; // 只会存0和1
        // 1 -> 1 3 5
        // arr -> 0 1 0 1 0 1 0000
        // 2 -> 2 4 6
        // arr -> 0 1 1 1 1 1 1  000
        // 3 -> 1 6 9
        // arr -> 0 1 1 1 1 1 1  0 0 1
        // 5 -> 0 7 8
        // arr -> 1 1 1 1 1 1 1  1 1 1
        // 布隆过滤器的判断,等一下，这个是对redis的查还是数就是这样，没有错呀只有7是不等于1，所以是true据库的查，那它返回空，说明是哪没有数据，||是或不就是任意满足一个就放回true
        // 4 -> 1 3 7
//
//        if (arr[1] != 1 || arr[3] != 1 || arr[7] != 1) {
//            return null;
//        }
        String jsonStr = redisUtil.get(RedisKeyUtil.getRoomKey(id));
        if (StrUtil.isNotBlank(jsonStr)) {
            Gson gson = new Gson();
            return gson.fromJson(jsonStr, RoomDetailRespVO.class);
        }
        ReentrantLock lock = new ReentrantLock();
        RoomDetailRespVO roomDetailVo = new RoomDetailRespVO();
        // 公寓
        try {
            lock.lock();
            jsonStr = redisUtil.get(RedisKeyUtil.getRoomKey(id));
            if (StrUtil.isNotBlank(jsonStr)) {
                Gson gson = new Gson();
                return gson.fromJson(jsonStr, RoomDetailRespVO.class);
            }
            RoomInfo roomInfo = roomInfoMapper.selectById(id);
            if (roomInfo == null) {
                return null;
            }
            BeanUtil.copyProperties(roomInfo, roomDetailVo);

            //公寓信息
            ApartmentItemRespVO apartmentItemVo = apartmentInfoService.getInfoById(roomInfo.getApartmentId());
            roomDetailVo.setApartmentItemVo(apartmentItemVo);

            //图片列表 List<GraphRespVO> graphVoList
            LambdaQueryWrapper<GraphInfo> graphInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
            graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemId, id);
            List<GraphInfo> graphInfos = graphInfoMapper.selectList(graphInfoLambdaQueryWrapper);
            List<GraphRespVO> graphVoList = BeanUtil.copyToList(graphInfos, GraphRespVO.class);
            roomDetailVo.setGraphVoList(graphVoList);

            //属性信息列表
            roomDetailVo.setAttrValueVoList(attrValueService.listByRoomId(id));

            // 配套信息列表 FacilityInfo
            roomDetailVo.setFacilityInfoList(VOConverter.toList(facilityInfoService.listByRoomId(id), FacilityRespVO.class));

            //标签信息列表
            roomDetailVo.setLabelInfoList(VOConverter.toList(labelInfoService.listByRoomId(id), LabelRespVO.class));

            //支付方式列表
            roomDetailVo.setPaymentTypeList(VOConverter.toList(paymentTypeService.getPaymentTypeByRoomId(id), PaymentTypeRespVO.class));

            //杂费列表
            roomDetailVo.setFeeValueVoList(feeValueService.listByApartmentId(roomInfo.getApartmentId()));
            //很多缓存一起失效，我知道了，刚刚那个是一个key100000同时查，现在是10key10000人查，也加锁呗，等一下
            //租期列表
            roomDetailVo.setLeaseTermList(VOConverter.toList(leaseTermService.listByRoomId(id), LeaseTermRespVO.class));
            // 放进缓存中,缓存也可以根据key更新吗，这样不是会遍慢吗，等一下，那个查redis的不是也要进来吗，那十万个，只有第一个还需进来创新的，其他的直接走缓存里面的了
            Gson gson = new Gson();
            String json = gson.toJson(roomDetailVo);
            redisUtil.set(RedisKeyUtil.getRoomKey(id), json, 60*60, TimeUnit.SECONDS);
        }
        finally {
            lock.unlock();
        }

        // 发送保存浏览记录的消息到mq,让消费者异步处理, 所以我们这里只需要发送消息
        // 思考我们的消费者需要用到什么参数:
        // 1. 消费者的逻辑就是保存浏览记录,所以直接查看保存浏览记录会需要什么参数
        // 2. 观察发现保存浏览记录需要两个参数,分别是用户ID和房间ID
        // 3. 由于mq传递参数只能传递一个,所以需要将两个参数封装到一个对象中,转为json字符串发送
        // 1. 封装参数, 得到json字符串
        // 2.查看rocketmq如何发送消息, 发送, 然后结束
        SaveBrowsHistoryMQMsg saveBrowsHistoryMQMsg = new SaveBrowsHistoryMQMsg(IdUtil.simpleUUID(), LoginUserHolder.getLoginUser().getUserId(), id);
        rocketMQTemplate.convertAndSend(MQConstant.SAVE_BROWSING_HISTORY_TOPIC,saveBrowsHistoryMQMsg);



        return roomDetailVo;
    }

    @Override
    public IPage<RoomItemRespVO> pageItemByApartmentId(Page<RoomItemRespVO> page, Long id) {
        IPage<RoomItemRespVO> roomItemVoIPage = roomInfoMapper.pageItemByApartmentId(page, id);
        // 如果分页返回空，则直接返回
        List<RoomItemRespVO> records = roomItemVoIPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return roomItemVoIPage;
        }
        // 以下代码都一样，都是组装RoomItemRespVO的其他属性，所以我们可以提取成方法
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
    private void setRoomItemVo(List<RoomItemRespVO> records) {
        // 这里传递进来的参数的地址值就是0x1234
        List<Long> roomIdList = records.stream().map(RoomItemRespVO::getId).toList();
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
            List<GraphRespVO> graphList = graphMap.getOrDefault(roomItemVo.getId(), new ArrayList<>()).stream().map(
                    graphInfo -> {
                        GraphRespVO graphVo = new GraphRespVO();
                        graphVo.setName(graphInfo.getName());
                        graphVo.setUrl(graphInfo.getUrl());
                        return graphVo;
                    }).toList();

            roomItemVo.setGraphVoList(graphList);

            //标签
            roomItemVo.setLabelInfoList(VOConverter.toList(labelMap.get(roomItemVo.getId()), LabelRespVO.class));
        });
    }
}




