package com.wxy.zzarental.web.app.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxy.zzarental.web.app.entity.BrowsingHistory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wxy.zzarental.web.app.service.dto.HistoryItemDTO;

/**
* @author liubo
* @description 针对表【browsing_history(浏览历史)】的数据库操作Service
* @createDate 2023-07-26 11:12:39
*/
public interface BrowsingHistoryService extends IService<BrowsingHistory> {
    IPage<HistoryItemDTO> pageItemByUserId(Page<BrowsingHistory> page, Long userId);

    /**
     * 保存用户浏览房间记录
     *
     * @param userId 用户ID
     * @param id     ID
     * @author wxy
     * @date 2026/09/04
     */
    void saveHistory(Long userId, Long id);
}
