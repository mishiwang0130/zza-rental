package com.wxy.zzarental.web.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wxy.zzarental.model.entity.AttrKey;
import com.wxy.zzarental.web.admin.service.dto.AttrKeyDTO;
import java.util.List;

/**
* @author liubo
* @description 针对表【attr_key(房间基本属性表)】的数据库操作Service
* @createDate 2023-07-24 15:48:00
*/
public interface AttrKeyService extends IService<AttrKey> {

    List<AttrKeyDTO> listAttrInfo();

    void removeAttrKeyById(Long attrKeyId);
}
