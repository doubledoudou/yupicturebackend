package com.example.yupicturebackend.model.dto.picture;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PictureEditRequest implements Serializable {

    /**
     * id
     */
    @ApiModelProperty(value = "图片ID")
    private Long id;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 空间 id（null 表示公共图库）
     */
    @ApiModelProperty(value = "空间ID")
    private Long spaceId;

    private static final long serialVersionUID = 1L;
}

