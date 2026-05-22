package com.example.yupicturebackend.model.dto.picture;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 请求包装类
 */
@Data
public class PictureReviewRequest implements Serializable {
    /**
     *  id
     */
    @ApiModelProperty(value = "图片ID")
    private Long id;
    /**
     * 审核状态 0-审核中 1-审核通过 2-审核拒绝
     */
    private Integer reviewStatus;
    /**
     * 审核信息
     */
    private String reviewMessage;

    private static final long serialVersionUID = 1L;
}
