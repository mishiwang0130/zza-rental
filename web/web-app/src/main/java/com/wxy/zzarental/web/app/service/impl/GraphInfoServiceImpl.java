package com.wxy.zzarental.web.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxy.zzarental.web.app.entity.GraphInfo;
import com.wxy.zzarental.web.app.service.GraphInfoService;
import com.wxy.zzarental.web.app.mapper.GraphInfoMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author liubo
* @description 针对表【graph_info(图片信息表)】的数据库操作Service实现
* @createDate 2023-07-26 11:12:39
*/
@Service
public class GraphInfoServiceImpl extends ServiceImpl<GraphInfoMapper, GraphInfo>
    implements GraphInfoService{

    @Resource
    private GraphInfoMapper graphInfoMapper;

    @Override
    public Map<Long, List<GraphInfo>> mapByItemIds(int i, List<Long> idList) {
        LambdaQueryWrapper<GraphInfo> graphInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemType, i);
        graphInfoLambdaQueryWrapper.in(GraphInfo::getItemId, idList);
        List<GraphInfo> graphInfoList = graphInfoMapper.selectList(graphInfoLambdaQueryWrapper);
        // 将graphInfoList收集为map， 1对多使用groupingBy
        Map<Long, List<GraphInfo>> graphMap = graphInfoList.stream().collect(Collectors.groupingBy(GraphInfo::getItemId));
        return graphMap;
    }
}




