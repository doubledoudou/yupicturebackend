package com.example.yupicturebackend.service.Impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yupicturebackend.exception.BusinessException;
import com.example.yupicturebackend.exception.ErrorCode;
import com.example.yupicturebackend.model.dto.user.UserQueryRequest;
import com.example.yupicturebackend.model.entity.User;
import com.example.yupicturebackend.model.enums.UserRoleEnum;
import com.example.yupicturebackend.model.vo.LoginUserVo;
import com.example.yupicturebackend.model.vo.UserVO;
import com.example.yupicturebackend.service.UserService;
import com.example.yupicturebackend.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.example.yupicturebackend.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author 19814
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-07-17 16:03:12
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{
    /**
     * 用户注册
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 2. 检查是否重复
        //构建查询对象 创建了一个对象queryWrapper 检查数据库中是否已存在相同的用户账号
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        //baseMapper提供基本数据库操作方法 selectCount 方法统计符合条件的记录数
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 3. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 4. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        //getValue() 获取枚举中定义的用户角色值 "user"
        user.setUserRole(UserRoleEnum.USER.getValue());
        //this.save(user) 是 MyBatis-Plus 提供的通用插入方法
        // 用于执行插入操作，返回布尔值表示是否插入成功。若插入失败，则抛出系统异常。
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        //
        return user.getId();
    }

    /**
     * 用来加密的 返回的是加密后的 上面有调用
     * @param userPassword
     * @return
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值，混淆密码
        final String SALT = "yupi";
//        使用DigestUtils.md5DigestAsHex() 方法对密码进行 MD5 加密，并返回加密后的字符串。
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    /**
     * 用户登录
     * 账号、密码、request管理session状态
     */
    public LoginUserVo userLogin(String userAccount, String userPassword, HttpServletRequest request){
        //1.校验 校验用户账号和密码是否为空，长度是否符合要求
        if (StrUtil.hasBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if(userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号错误");
        }
        if (userPassword.length()<8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码错误");
        }
//        2.进行加密 对密码进行加盐加密处理
        String encryptPassword = getEncryptPassword(userPassword);
//        3.查询用户是否存在 查询数据库验证用户账号和密码是否匹配
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        queryWrapper.eq("userPassword",encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        //用户不存在
        if (user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在或密码错误");
        }
//        3.记录用户的登录态 登录成功，将用户信息存入会话
        request.getSession().setAttribute(USER_LOGIN_STATE,user);
//        4返回脱敏后的用户信息LoginUserVO
        return this.getLoginUserVO(user);
    }

    /**
     * 可以从request对象对应的Session中获取到之前保存的登录用户信息，无需其他参数
     * 获取当前登录用户信息
     * 为了保障获取到的数据是最新的
     * 先从Session中获取登录用户的id,然后从数据库中查询最新的结果
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request){
        //先判断是否已登录 从session中获取用户对象  也就是说先看session中有没有
//        注意：使用 Object 是因为 getAttribute 方法返回的是 Object 类型，需要强制转换为 User
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User)userObj;
        //如果用户未登录或用户ID为空，则抛出未登录异常
        if (currentUser == null || currentUser.getId()==null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        //从数据库查询 使用用户ID查询最新用户信息===>替换掉原来存储的用户信息
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
//        如果查询为空，则抛出未登录异常
        if (currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        //返回查询到的用户对象
        return currentUser;
    }

    /**
     * 用于User实体对象转换为LoginUserVo实体对象 脱敏
     * @param user
     * @return
     */
    @Override
    public LoginUserVo getLoginUserVO(User user) {
        if (user == null){
            return null;
        }
//        先new一个LoginUserVo对象
        LoginUserVo loginUserVo = new LoginUserVo();
        //将User对象的属性复制给loginUserVo对象
        BeanUtils.copyProperties(user, loginUserVo);
        return loginUserVo;
    }

    /**
     * 退出登录--移除登录态
     * 从Session中移除当前用户的登录态即可
     * 操作只有成功和失败两种状态
     */
    @Override
    public boolean userLogout(HttpServletRequest request){
        //从Session中获取用户登录状态   先判断是否登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"未登录");
        }
        //移除登录态=表示登出
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    /**
     * 获取脱敏后的用户信息
     * @param user 脱敏前的信息
     * @return
     */
    @Override
    public UserVO getUserVO(User user) {
        //将用户实体类User转化为视图对象UserVO 属性值相同 用于屏蔽敏感信息
        if (user == null){
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user,userVO);
        return userVO;
    }

    /**
     * 获取脱敏后的用户列表
     * @param userList
     * @return
     */
    @Override
    //通过流式操作逐个调用单个对象的转换方法
    public List<UserVO> getUserVOList(List<User> userList) {
        //如果传入的用户列表为空或大小为0，则返回一个空的ArrayList
        if (CollUtil.isEmpty(userList)){
            return new ArrayList<>();
        }
        //通过stream()方法将用户列表转换为流，并使用map()方法将每个用户转换为UserVO对象，最后将结果转换为List并返回
//        this::getUserVO 等价于 user -> this.getUserVO(user)，表示引用当前类中的 getUserVO 方法。
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    /**
     * 判断当前用户是否是管理员
     * @param user
     * @return
     */
    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 这个方法专门用于将查询请求转为QueryWrapper对象
     * 根据UserQueryRequest构建一个QueryWrapper查询条件，用于数据库查询操作
     * 对于分页查询接口，需要根据用户传入的参数来构造SQL查询。由于使用MyBatisPlus框架不用自己拼接SQL
     * 而是通过构造QueryWrapper对象来构造SQL查询
     * @param userQueryRequest 查询条件
     * @return
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        //拿到用户查询的数据吗？？？
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //当 id 非空时，添加 id 等于指定值的查询条件。
        queryWrapper.eq(Objects.nonNull(id), "id", id);
        //当 userRole 非空时，添加 userRole 等于指定值的查询条件。
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        //动态构建模糊查询条件 用于数据库模糊匹配
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        //动态添加排序条件 根据 sortOrder 是否为 "ascend" 来决定升序或降序排列。
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField),sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }


}




