package com.example.yupicturebackend.aop;

import com.example.yupicturebackend.annotation.AuthCheck;
import com.example.yupicturebackend.exception.BusinessException;
import com.example.yupicturebackend.exception.ErrorCode;
import com.example.yupicturebackend.model.entity.User;
import com.example.yupicturebackend.model.enums.UserRoleEnum;
import com.example.yupicturebackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 权限校验AOP--拦截带有@AuthCheck注解的方法并进行权限校验
 * 用户调用方法
 *     ↓
 * 方法上有 @AuthCheck 注解吗？
 *     ↓ 有
 * 保安（AuthInterceptor）拦截
 *     ↓
 * 读取注解：需要什么角色？（mustRole）
 *     ↓
 * 获取当前登录用户的角色
 *     ↓
 * ┌─────────────────────┐
 * │  角色匹配吗？         │
 * └─────────────────────┘
 *     ↓              ↓
 *   匹配 ✅        不匹配 ❌
 *     ↓              ↓
 *   放行！       抛出异常："权限不足"
 */
public class AuthInterceptor {
    @Resource
    private UserService userService;
    @Around("@annotation(authCheck)")
    //ProceedingJoinPoint 这是AOP切面方法，用于实现权限校验功能
    //该方法在@AuthCheck注解的方法执行时拦截，根据注解中指定的角色判断当前登录用户是否有权限访问，若无权限则抛出异常，否则放行
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck)throws Throwable{
        //获取注解中要求的角色mustRole
        String mustRole = authCheck.mustRole();
        //RequestContextHolder用于获取当前请求的上下文信息（如HTTP请求对象）
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 当前登录用户 获取当前登录用户及其角色
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        //不需要权限，放行  如果无需权限mustRole为空就放行
        if (mustRoleEnum == null){
            return joinPoint.proceed();
        }
        //以下为必须有该权限才通过
        //获取当前用户具有的权限
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        //没有权限，拒绝  如果用户无角色或角色不匹配，抛出无权限异常
        if (userRoleEnum == null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //要求必须有管理员权限，但用户没有管理员权限，拒绝  若要求管理员权限但用户不是管理员就拒绝访问
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum)&& !UserRoleEnum.ADMIN.equals(userRoleEnum)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //通过权限校验，放行
        return joinPoint.proceed();
    }

}
