package com.wxy.zzarental.web.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wxy.zzarental.web.admin.entity.FeeKey;
import com.wxy.zzarental.web.admin.service.dto.FeeKeyDTO;
import java.util.List;

/**
* @author liubo
* @description 针对表【fee_key(杂项费用名称表)】的数据库操作Service
* @createDate 2023-07-24 15:48:00
*/
public interface FeeKeyService extends IService<FeeKey> {

    List<FeeKeyDTO> feeInfoList();

    void deleteFeeKeyById(Long feeKeyId);
}
