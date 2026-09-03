package com.wxy.zzarental.web.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wxy.zzarental.model.entity.LeaseTerm;
import com.wxy.zzarental.model.entity.RoomLeaseTerm;
import com.wxy.zzarental.web.app.mapper.LeaseTermMapper;
import com.wxy.zzarental.web.app.mapper.RoomLeaseTermMapper;
import com.wxy.zzarental.web.app.service.LeaseTermService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author liubo
 * @description 针对表【lease_term(租期)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
public class LeaseTermServiceImpl extends ServiceImpl<LeaseTermMapper, LeaseTerm>
        implements LeaseTermService {
    @Resource
    private RoomLeaseTermMapper roomLeaseTermMapper;
    @Resource
    private LeaseTermMapper leaseTermMapper;

    @Override
    public List<LeaseTerm> listByRoomId(Long id) {
        LambdaQueryWrapper<RoomLeaseTerm> roomLeaseTermLambdaQueryWrapper = new LambdaQueryWrapper<>();
        roomLeaseTermLambdaQueryWrapper.eq(RoomLeaseTerm::getRoomId, id);
        List<RoomLeaseTerm> roomLeaseTerms = roomLeaseTermMapper.selectList(roomLeaseTermLambdaQueryWrapper);
        List<Long> leaseTermIds = roomLeaseTerms.stream().map(RoomLeaseTerm::getLeaseTermId).toList();
        return leaseTermMapper.selectBatchIds(leaseTermIds);

    }
}




