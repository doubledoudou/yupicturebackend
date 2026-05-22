package com.example.yupicturebackend.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
public class UserVO implements Serializable {
    /**
     *  id
     */
    @ApiModelProperty(value = "用户ID")
    private Long id;
    /**
     *  用户账号
     */
    private String userAccount;
    /**
     *  用户姓名
     */
    private String userName;
    /**
     *  用户头像
     */
    private String userAvatar;
    /**
     *  用户简介
     */
    private String userProfile;
    /**
     *  用户角色：user/admin
     */
    private String userRole;
    /**
     *  创建时间
     */
    private Date createTime;
    private static final long serialVersionUID = 1L;
}
