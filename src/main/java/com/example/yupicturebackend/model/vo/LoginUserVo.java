package com.example.yupicturebackend.model.vo;


import lombok.Data;

import java.io.Serializable;

/**
 * 返回给前端的用户信息 脱敏后的
 */
@Data
public class LoginUserVo implements Serializable {
    private Long id;
    private String userAccount;
    private String userName;
    /**
     * 用户头像
     */
    private String userAvatar;
    /**
     * 用户简介
     */
    private String userProfile;
    private String userRole;
    private Data createTime;
    private Data updateTime;
    private static final long serialVersionUID = 1L;
}
