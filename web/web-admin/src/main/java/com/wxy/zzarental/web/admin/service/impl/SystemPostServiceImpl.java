package com.wxy.zzarental.web.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxy.zzarental.model.entity.BaseEntity;
import com.wxy.zzarental.model.entity.SystemPost;
import com.wxy.zzarental.model.entity.SystemUser;
import com.wxy.zzarental.web.admin.mapper.SystemPostMapper;
import com.wxy.zzarental.web.admin.mapper.SystemUserMapper;
import com.wxy.zzarental.web.admin.service.SystemPostService;
import com.wxy.zzarental.web.admin.service.dto.SystemPostItemDTO;
import com.wxy.zzarental.web.admin.service.dto.SystemUserItemDTO;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* @author liubo
* @description 针对表【system_post(岗位信息表)】的数据库操作Service实现
* @createDate 2023-07-24 15:48:00
*/
@Service
public class SystemPostServiceImpl extends ServiceImpl<SystemPostMapper, SystemPost>
    implements SystemPostService{
    @Resource
    private SystemPostMapper systemPostMapper;
    @Resource
    private SystemUserMapper systemUserMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public IPage<SystemPostItemDTO> page1(IPage<SystemPost> systemPostPage, String postName) {
        //根据岗位名称（可甜可不填）来翻页
        //查询条件
        LambdaQueryWrapper<SystemPost> systemPostWrapper = new LambdaQueryWrapper<>();
        systemPostWrapper.like(StrUtil.isNotBlank(postName),SystemPost::getName,postName);
        //基础分页
        IPage<SystemPost> systemPostIPage = systemPostMapper.selectPage(systemPostPage, systemPostWrapper);
        //岗位列表
        List<SystemPost> postRecords = systemPostIPage.getRecords();
        //岗位id的集合
        List<Long> postIdList = postRecords.stream().map(BaseEntity::getId).distinct().toList();
        //岗位id查用户
        LambdaQueryWrapper<SystemUser> systemUserWrapper = new LambdaQueryWrapper<>();
        systemUserWrapper.in(SystemUser::getPostId,postIdList);
        List<SystemUser> systemUsers = systemUserMapper.selectList(systemUserWrapper);
        //key是岗位id，value是用户
        Map<Long,List<SystemUser>> userMap = systemUsers.stream().collect(Collectors.groupingBy(SystemUser::getPostId));
        // 将数据库中返回的数据组装成前端所需要的格式
        Page<SystemPostItemDTO> page = new Page<>(systemPostIPage.getCurrent(), systemPostIPage.getSize(), systemPostIPage.getTotal());
        List<SystemPostItemDTO> voList = postRecords.stream().map(post ->{
            SystemPostItemDTO vo = new SystemPostItemDTO();
            BeanUtils.copyProperties(post,vo);
            List<SystemUser> users = userMap.get(post.getId());
            List<SystemUserItemDTO> systemUsersList = users == null ? null
                    : BeanUtil.copyToList(users, SystemUserItemDTO.class);
            vo.setSystemUsers(systemUsersList);
            return vo;
        }).collect(Collectors.toList());
        page.setRecords(voList);

        return page;
    }
}
