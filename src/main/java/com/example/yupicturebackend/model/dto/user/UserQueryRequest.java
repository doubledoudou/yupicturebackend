package com.example.yupicturebackend.model.dto.user;

import com.example.yupicturebackend.common.PageRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户查询请求类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {
    @ApiModelProperty(value = "用户ID")
    private Long id;
    private String userName;
    private String userAccount;
    private String userProfile;
    private String userRole;
    private static final long serialVersionUID = 1L;
}
