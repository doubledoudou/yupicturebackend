package com.example.yupicturebackend.model.dto.picture;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用于接受与图片相关的其他请求参数（标题、描述、标签）
 */
@Data
public class PictureUploadRequest implements Serializable {
    /**
     * 图片id
     */
    @ApiModelProperty(value = "图片ID")
    private Long id;
    /**
     * 图片url
     */
    private String fileUrl;

    /**
     * 空间 id（null 表示公共图库）
     */
    @ApiModelProperty(value = "空间ID")
    private Long spaceId;
    private static final long serialVersionUID = 1L;
}
