package com.example.yupicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.yupicturebackend.model.dto.user.UserQueryRequest;
import com.example.yupicturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yupicturebackend.model.vo.LoginUserVo;
import com.example.yupicturebackend.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 19814
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-07-17 16:03:12
*/
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     */
    LoginUserVo userLogin(String userAccount,String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息 将完整的user信息转化为脱敏后的LoginUserVO
     */
    LoginUserVo getLoginUserVO(User user);

    /**
     * 用户注销 可以从request请求对象对应的Session中获取到之前保存的登录用户信息，完成注销，无需其他请求参数。
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取用户脱敏信息
     * @param user 脱敏前的信息
     * @return 脱敏后的信息
     */
    UserVO getUserVO(User user);

    /**
     * 批量获取用户脱敏信息
     * @param userList 脱敏前的信息
     * @return 脱敏后的信息
     */
//    List<UserVO> getUserVO(List<User> userList);

    /**
     * 获取查询条件
     * @param userQueryRequest 查询条件
     * @return 查询条件
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 获取加密后的密码
     * @param userPassword
     * @return
     */
    String getEncryptPassword(String userPassword);

    /**
     * 获取用户列表
     * @param records
     * @return
     */
    List<UserVO> getUserVOList(List<User> records);

    /**
     * 判断是否为管理员
     * @param user
     * @return
     */
    boolean isAdmin(User user);
}
