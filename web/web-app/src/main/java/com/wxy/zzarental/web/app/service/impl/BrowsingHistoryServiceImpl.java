package com.wxy.zzarental.web.app.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxy.zzarental.common.login.LoginUserHolder;
import com.wxy.zzarental.web.app.entity.*;
import com.wxy.zzarental.web.app.mapper.ApartmentInfoMapper;
import com.wxy.zzarental.web.app.mapper.BrowsingHistoryMapper;
import com.wxy.zzarental.web.app.mapper.GraphInfoMapper;
import com.wxy.zzarental.web.app.mapper.RoomInfoMapper;
import com.wxy.zzarental.web.app.service.ApartmentInfoService;
import com.wxy.zzarental.web.app.service.BrowsingHistoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxy.zzarental.web.app.service.GraphInfoService;
import com.wxy.zzarental.web.app.service.dto.GraphDTO;
import com.wxy.zzarental.web.app.service.dto.HistoryItemDTO;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
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
    private ApartmentInfoMapper apartmentInfoMapper;
    @Resource
    private GraphInfoService graphInfoService;
    @Override
    public IPage<HistoryItemDTO> pageItemByUserId(Page<BrowsingHistory> page, Long userId) {
        LambdaQueryWrapper<BrowsingHistory> browsingHistoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        browsingHistoryLambdaQueryWrapper.eq(BrowsingHistory::getUserId,userId);
        // 对浏览记录分页，需要传两个参数，浏览记录的page和浏览记录的wrapper
        // 调用什么的mapper，就要传什么泛型，这里是调用BrowsingHistory的mapper，page和wrapper都要是BrowsingHistory的
        IPage<BrowsingHistory> resultPage = browsingHistoryMapper.selectPage(page, browsingHistoryLambdaQueryWrapper);
        List<BrowsingHistory> records = resultPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        }
        List<Long> roomIdList = records.stream().map(BrowsingHistory::getRoomId).toList();
        List<RoomInfo> roomInfoList = roomInfoMapper.selectBatchIds(roomIdList);
        Map<Long, RoomInfo> roomInfoMap = roomInfoList.stream().collect(Collectors.toMap(BaseEntity::getId, Function.identity(), (key1, key2) -> key1));

        //图片列表
        Map<Long, List<GraphInfo>> graphMap = graphInfoService.mapByItemIds(2,roomIdList);

        List<Long> apartmentIds = roomInfoList.stream().map(RoomInfo::getApartmentId).toList();
        List<ApartmentInfo> apartmentInfos = apartmentInfoMapper.selectBatchIds(apartmentIds);
        Map<Long, ApartmentInfo> apartmentInfoMap = apartmentInfos.stream().collect(Collectors.toMap(BaseEntity::getId, Function.identity(), (key1, key2) -> key1));

        List<HistoryItemDTO> historyItemVos = records.stream().map(
                browsingHistory -> {
                    HistoryItemDTO historyItemVo = new HistoryItemDTO();
                    BeanUtil.copyProperties(browsingHistory, historyItemVo);
                    Long roomId = browsingHistory.getRoomId();

                    List<GraphInfo> graphInfos = graphMap.get(roomId);
                    if (CollUtil.isNotEmpty(graphInfos)) {
                        List<GraphDTO> graphVos = graphInfos.stream()
                                .map(item -> new GraphDTO(item.getName(), item.getUrl())).toList();
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

        Page<HistoryItemDTO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        voPage.setRecords(historyItemVos);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveHistory(Long userId, Long id) {
        LambdaQueryWrapper<BrowsingHistory> browsingHistoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        browsingHistoryLambdaQueryWrapper.eq(BrowsingHistory::getUserId,userId);
        browsingHistoryLambdaQueryWrapper.eq(BrowsingHistory::getRoomId,id);
        BrowsingHistory browsingHistory = browsingHistoryMapper.selectOne(browsingHistoryLambdaQueryWrapper);
        if (browsingHistory != null){
            Date nowDate = new Date();
            browsingHistory.setBrowseTime(nowDate);
            browsingHistoryMapper.updateById(browsingHistory);
            return;
        }
        BrowsingHistory newBrowsingHistory = new BrowsingHistory();
        Date date = new Date();
        newBrowsingHistory.setBrowseTime(date);
        newBrowsingHistory.setUserId(userId);
        newBrowsingHistory.setRoomId(id);
        browsingHistoryMapper.insert(newBrowsingHistory);
    }
}
