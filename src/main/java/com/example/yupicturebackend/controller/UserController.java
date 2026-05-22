package com.example.yupicturebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yupicturebackend.annotation.AuthCheck;
import com.example.yupicturebackend.common.BaseResponse;
import com.example.yupicturebackend.common.DeleteRequest;
import com.example.yupicturebackend.common.ResultUtils;
import com.example.yupicturebackend.constant.UserConstant;
import com.example.yupicturebackend.exception.BusinessException;
import com.example.yupicturebackend.exception.ErrorCode;
import com.example.yupicturebackend.exception.ThrowUtils;
import com.example.yupicturebackend.model.dto.user.*;
import com.example.yupicturebackend.model.entity.User;
import com.example.yupicturebackend.model.vo.LoginUserVo;
import com.example.yupicturebackend.model.vo.UserVO;
import com.example.yupicturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;


@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        //拿到信息
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
//        把拿到的信息给数据库
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
//        返回结果
        return ResultUtils.success(result);
    }

    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVo> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        //拿到传过来的账户和密码
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
//        执行登录操作 request用于管理会话状态
        LoginUserVo loginUserVo = userService.userLogin(userAccount,userPassword,request);
//        登录成功后就把用户信息返回给前端
        return ResultUtils.success(loginUserVo);
    }

    /**
     * 获取当前登录用户的接口
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVo> getLoginUser(HttpServletRequest request){
//        User loginUser = userService.getLoginUser(request);
//        return ResultUtils.success(userService.getLoginUserVo(loginUser));
//        改为脱敏后的用户信息
        User user = userService.getLoginUser(request);
//        调用getLoginUserVO方法获取脱敏后的用户信息
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    /**
     * 用户退出登录接口
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout (HttpServletRequest request){
        ThrowUtils.throwIf(request == null,ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 管理员添加用户
     * @param userAddRequest
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest){
        //参数校验 如果请求参数为null，则抛出参数错误异常
        ThrowUtils.throwIf(userAddRequest == null,ErrorCode.PARAMS_ERROR);
        //将请求参数userAddRequest对象属性拷贝到实体类对象User对象中
        User user = new User();
        BeanUtils.copyProperties(userAddRequest,user);
        //默认密码123456
        final String DEFAULT_PASSWORD = "123456";
        String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        //保存用户到数据库
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        //成功返回包含用户id的成功响应
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据id获取用户，仅管理员
     * @param id
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(@RequestParam Long id){
        //如果id小于等于0，则返回参数错误
        ThrowUtils.throwIf(id == null || id <= 0,ErrorCode.PARAMS_ERROR);
        //通过UserService的getById方法获取用户
        User user = userService.getById(id);
        //如果用户不存在，抛出未找到异常
        ThrowUtils.throwIf(user == null,ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        //返回成功
        return ResultUtils.success(user);
    }

    /**
     * 通过用户id获取用户信息 并将其转化为UserVO
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(@RequestParam Long id){
        //调用getUserById方法获取包含用户实体的响应对象
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        //通过ResultUtils.success包装成成功的响应返回
        return ResultUtils.success(userService.getUserVO( user));
    }

    /**
     * 删除用户
     * @param deleteRequest
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest){
        //
        if (deleteRequest == null || deleteRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        //通过ResultUtils.success包装成成功的响应返回
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     * @param userUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest){
        if (userUpdateRequest == null || userUpdateRequest.getId() == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        //调用userService更新用户
        boolean result = userService.updateById(user);
        //如果更新失败 则抛出异常
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 实现了用户信息的分页查询功能 返回封装后的用户数据（UserVO）列表
     * @param userQueryRequest
     * @return
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    //设置分页结果并返回统一响应格式 BaseResponse<Page<UserVO>>
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest){
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        //获取当前页码和每页大小
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();
        //调用 userService.page 查询用户数据（User实体类）。
        Page<User> userPage = userService.page(new Page<>(current, pageSize), userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

}

