package com.wxy.zzarental.web.app.controller.history;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxy.zzarental.common.login.LoginUserHolder;
import com.wxy.zzarental.common.result.Result;
import com.wxy.zzarental.model.entity.BrowsingHistory;
import com.wxy.zzarental.web.app.service.BrowsingHistoryService;
import com.wxy.zzarental.web.app.vo.history.HistoryItemRespVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "浏览历史管理")
@RequestMapping("/app/history")
public class BrowsingHistoryController {
    @Resource
    private BrowsingHistoryService browsingHistoryService;
    @Operation(summary = "获取浏览历史")
    @GetMapping("pageItem")
    public Result<IPage<HistoryItemRespVO>> page(@RequestParam long current, @RequestParam long size) {
        Page<BrowsingHistory> page = new Page<>(current, size);
        Long userId = LoginUserHolder.getLoginUser().getUserId();
        IPage<HistoryItemRespVO> resultPage =  browsingHistoryService.pageItemByUserId(page,userId);
        return Result.ok(resultPage);
    }
}
