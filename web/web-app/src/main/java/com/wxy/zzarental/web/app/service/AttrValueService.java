package com.wxy.zzarental.web.app.service;

import com.wxy.zzarental.model.entity.AttrValue;
import com.baomidou.mybatisplus.spring.service.IService;
import com.wxy.zzarental.web.app.vo.attr.AttrValueVo;

import java.util.List;

/**
* @author liubo
* @description 针对表【attr_value(房间基本属性值表)】的数据库操作Service
* @createDate 2023-07-26 11:12:39
*/
public interface AttrValueService extends IService<AttrValue> {
    /**
     * 按房间id列出
     *
     * @param roomId 房间号
     * @return {@code List<AttrValueVo> }
     * @author wxy
     * @date 2026/09/03
     */
    List<AttrValueVo> listByRoomId(Long roomId);
}
