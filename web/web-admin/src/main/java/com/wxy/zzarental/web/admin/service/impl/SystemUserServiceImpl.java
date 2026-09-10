package com.wxy.zzarental.web.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxy.zzarental.model.entity.SystemPost;
import com.wxy.zzarental.model.entity.SystemUser;
import com.wxy.zzarental.web.admin.mapper.SystemPostMapper;
import com.wxy.zzarental.web.admin.mapper.SystemUserMapper;
import com.wxy.zzarental.web.admin.service.SystemUserService;
import com.wxy.zzarental.web.admin.service.dto.SystemUserItemDTO;
import com.wxy.zzarental.web.admin.service.query.SystemUserQuery;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * @author liubo
 * @description 针对表【system_user(员工信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class SystemUserServiceImpl extends ServiceImpl<SystemUserMapper, SystemUser>
        implements SystemUserService {
    @Resource
    private  SystemUserMapper systemUserMapper;
    @Resource
    private SystemPostMapper systemPostMapper;

    @Override
    public IPage<SystemUserItemDTO> pageUser(Page<SystemUser> systemUserPage, SystemUserQuery queryVo) {
//        //联表查，多了一个岗位名称，可以根据SystemUser里面的岗位id查，条件是员工姓名和手机号码
        //先查自带的部分，让它分页
        //自带部分的条件是员工姓名和手机号码，都用like吧
        LambdaQueryWrapper<SystemUser> systemUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 字符串的判断，一ban我知道用上面c开头的一个，不是，一般不仅要判断是否为null，还要判断是否为空字符串"",字符串的比较不是用=和!=
        // 是用equals, null例外，但是如果每次都写两个条件，又长又容易漏，所以我们要用hutool，等一下，这个不是不等于吗equal不是等于吗
        // 是的，所以前面加个取反,hutool的工具类有个特点，操作xx的就叫xxUtil,比如集合的就叫CollUtil,
        // 字符串用isNotBlank和isBlank来判断是否非空和为空，工具类就是StrUtil,只有我们用不上的，没有他没有的，一般要干什么都可以用hutool
        systemUserLambdaQueryWrapper.like(StrUtil.isNotBlank(queryVo.getName()),SystemUser::getName,queryVo.getName());
        systemUserLambdaQueryWrapper.like(StrUtil.isNotBlank(queryVo.getPhone()),SystemUser::getPhone,queryVo.getPhone());
        //应该要分页了
        Page<SystemUser> resultPage = systemUserMapper.selectPage(systemUserPage, systemUserLambdaQueryWrapper);
        //要从实体类转vo
        // 1. 取出分页中的SystemUser列表（resultPage.getRecords()）
        List<SystemUser> userList = resultPage.getRecords();
        // 3. 得到一个List<SystemUserItemDTO>
        List<SystemUserItemDTO> voList = new ArrayList<>();
        List<Long> postIds = userList.stream().map(SystemUser::getPostId).distinct().toList();
        // 一次数据库查询
        List<SystemPost> systemPosts = systemPostMapper.selectBatchIds(postIds);
        // 将postList转换为map
        Map<Long,String> postNameMap = systemPosts.stream().collect(Collectors.toMap(SystemPost::getId,SystemPost::getName)) ;
        // 这里你遍历的是一个空的voList,我要用这个存放转换好的vo对象呀,现在对了
        for(SystemUser user : userList ){
            SystemUserItemDTO vo = new SystemUserItemDTO();
            // 2. 将每一个SystemUser转换为SystemUserItemDTO，得到vo对象
            BeanUtils.copyProperties(user,vo);
            //我要把名称的写这里面，这里正好可以一个一个处理
            // 整个循环受益
            vo.setPostName(postNameMap.get(user.getPostId()));

            voList.add(vo);
        }
        // 以上全对

        // 4. 创建一个新的空Page对象，Page<SystemUserItemDTO>，填充分页结果的current、size、total以及转换后的List<SystemUserItemDTO>
        // 我们这里实际上就是要把我们的resultPage(数据库查出来的分页)转换为我们要的泛型，所以要挨个转换然后赋值,所以是要用resultPage
        Page<SystemUserItemDTO> page = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        page.setRecords(voList);
        return page;
    }

    @Override
    public SystemUserItemDTO getSystemUserById(Long id) {
        SystemUser systemUser = systemUserMapper.selectById(id);
        SystemPost systemPost = systemPostMapper.selectById(systemUser.getPostId());
        SystemUserItemDTO systemUserItemVo = new SystemUserItemDTO();
        BeanUtils.copyProperties(systemUser,systemUserItemVo);
        systemUserItemVo.setPostName(systemPost.getName());
        return systemUserItemVo;
    }
}
