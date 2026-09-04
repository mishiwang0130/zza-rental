package com.wxy.zzarental.web.app.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxy.zzarental.model.entity.*;
import com.wxy.zzarental.web.app.mapper.ApartmentInfoMapper;
import com.wxy.zzarental.web.app.mapper.BrowsingHistoryMapper;
import com.wxy.zzarental.web.app.mapper.GraphInfoMapper;
import com.wxy.zzarental.web.app.mapper.RoomInfoMapper;
import com.wxy.zzarental.web.app.service.ApartmentInfoService;
import com.wxy.zzarental.web.app.service.BrowsingHistoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxy.zzarental.web.app.service.GraphInfoService;
import com.wxy.zzarental.web.app.vo.graph.GraphVo;
import com.wxy.zzarental.web.app.vo.history.HistoryItemVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author liubo
 * @description 针对表【browsing_history(浏览历史)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
public class BrowsingHistoryServiceImpl extends ServiceImpl<BrowsingHistoryMapper, BrowsingHistory>
        implements BrowsingHistoryService {
    @Resource
    private BrowsingHistoryMapper browsingHistoryMapper;
    @Resource
    private RoomInfoMapper roomInfoMapper;
    @Resource
    private GraphInfoMapper graphInfoMapper;
    @Resource
    private ApartmentInfoMapper apartmentInfoMapper;
    @Resource
    private ApartmentInfoService apartmentInfoService;
    @Resource
    private GraphInfoService graphInfoService;
    @Override
    public IPage<HistoryItemVo> pageItemByUserId(Page<HistoryItemVo> page, Long userId) {
        LambdaQueryWrapper<BrowsingHistory> browsingHistoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        browsingHistoryLambdaQueryWrapper.eq(BrowsingHistory::getUserId,userId);
        List<BrowsingHistory> browsingHistories = browsingHistoryMapper.selectList(browsingHistoryLambdaQueryWrapper);
        if (CollUtil.isEmpty(browsingHistories)) {
            return page;
        }
        List<Long> roomIdList = browsingHistories.stream().map(BrowsingHistory::getRoomId).toList();
        List<RoomInfo> roomInfoList = roomInfoMapper.selectBatchIds(roomIdList);
        Map<Long, RoomInfo> roomInfoMap = roomInfoList.stream().collect(Collectors.toMap(BaseEntity::getId, Function.identity(), (key1, key2) -> key1));

        //图片列表
        Map<Long, List<GraphInfo>> graphMap = graphInfoService.mapByItemIds(2,roomIdList);

        List<Long> apartmentIds = roomInfoList.stream().map(RoomInfo::getApartmentId).toList();
        List<ApartmentInfo> apartmentInfos = apartmentInfoMapper.selectBatchIds(apartmentIds);
        Map<Long, ApartmentInfo> apartmentInfoMap = apartmentInfos.stream().collect(Collectors.toMap(BaseEntity::getId, Function.identity(), (key1, key2) -> key1));

        List<HistoryItemVo> historyItemVos = browsingHistories.stream().map(
                browsingHistory -> {
                    HistoryItemVo historyItemVo = new HistoryItemVo();
                    BeanUtil.copyProperties(browsingHistory, historyItemVo);
                    Long roomId = browsingHistory.getRoomId();

                    List<GraphInfo> graphInfos = graphMap.get(roomId);
                    if (CollUtil.isNotEmpty(graphInfos)) {
                        List<GraphVo> graphVos = graphInfos.stream()
                                .map(item -> new GraphVo(item.getName(), item.getUrl())).toList();
                        historyItemVo.setRoomGraphVoList(graphVos);
                    }

                    RoomInfo roomInfo = roomInfoMap.get(roomId);
                    if (roomInfo !=null) {
                        historyItemVo.setRoomNumber(roomInfo.getRoomNumber());
                        historyItemVo.setRent(roomInfo.getRent());
                        ApartmentInfo apartmentInfo = apartmentInfoMap.get(roomInfo.getApartmentId());
                        if (apartmentInfo != null) {
                            historyItemVo.setApartmentName(apartmentInfo.getName());
                            historyItemVo.setProvinceName(apartmentInfo.getProvinceName());
                            historyItemVo.setCityName(apartmentInfo.getCityName());
                            historyItemVo.setDistrictName(apartmentInfo.getDistrictName());
                        }
                    }
                    return historyItemVo;
                }
        ).toList();
        page.setRecords(historyItemVos);
        return page;
    }
}