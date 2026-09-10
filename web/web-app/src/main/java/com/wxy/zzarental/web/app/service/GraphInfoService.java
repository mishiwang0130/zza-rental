package com.wxy.zzarental.web.app.service;

import com.wxy.zzarental.model.entity.GraphInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
* @author liubo
* @description 针对表【graph_info(图片信息表)】的数据库操作Service
* @createDate 2023-07-26 11:12:39
*/
public interface GraphInfoService extends IService<GraphInfo> {
    Map<Long, List<GraphInfo>> mapByItemIds(int i, List<Long> idList);
}
