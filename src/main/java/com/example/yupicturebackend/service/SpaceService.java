package com.example.yupicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yupicturebackend.model.dto.space.SpaceAddRequest;
import com.example.yupicturebackend.model.dto.space.SpaceQueryRequest;
import com.example.yupicturebackend.model.entity.Space;
import com.example.yupicturebackend.model.entity.User;
import com.example.yupicturebackend.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;


/**
* @author 19814
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2026-05-13 13:55:12
*/
public interface SpaceService extends IService<Space> {

    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 根据空间级别填充空间信息
     *
     * @param space 空间对象
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 校验空间信息
     *
     * @param space 空间对象
     * @param add   是否为新增操作
     */
    void validSpace(Space space, boolean add);
    
    /**
     * 删除空间
     * @param spaceId 空间ID
     * @param loginUser 当前登录用户
     */
    void deleteSpace(long spaceId, User loginUser);
    
    /**
     * 编辑空间（仅空间创建人可用）
     * @param spaceEditRequest 编辑请求参数
     * @param loginUser 当前登录用户
     */
    void editSpace(com.example.yupicturebackend.model.dto.space.SpaceEditRequest spaceEditRequest, User loginUser);
    
    /**
     * 获取查询条件
     * @param spaceQueryRequest 查询条件
     * @return 查询条件
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);
    
    /**
     * 获取空间VO
     * @param space 空间对象
     * @param request HTTP请求
     * @return 空间VO
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);
    
    /**
     * 分页获取空间VO列表
     * @param spacePage 空间分页对象
     * @param request HTTP请求
     * @return 空间VO分页对象
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);
}
