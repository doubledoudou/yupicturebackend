package com.example.yupicturebackend.model.vo;

import cn.hutool.json.JSONUtil;
import com.example.yupicturebackend.model.entity.Picture;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class PictureVO implements Serializable {
    @ApiModelProperty(value = "图片ID")
    private Long id;
    private String url;
    private String name;
    private String introduction;
    /**
     * 图片标签
     */
    private List<String> tags;
    /**
     * 图片分类
     */
    private String category;
    /**
     * 文件体积
     */
    @ApiModelProperty(value = "文件体积")
    private Long picSize;
    private Integer picWidth;
    private Integer picHeight;
    /**
     * 图片比例
     */
    private Double picScale;
    /**
     * 图片格式
     */
    private String picFormat;


    @ApiModelProperty(value = "创建用户ID")
    private Long userId;
    /**
     * 空间 id（null 表示公共图库）
     */
    @ApiModelProperty(value = "空间ID")
    private Long spaceId;
    private Date createTime;
    private Date editTime;
    private Date updateTime;
    /**
     *  用户 创建用户信息
     */
    private UserVO user;
    private static final long serialVersionUID = 1L;

    /**
     * 封装类转对象
     * @param pictureVO
     * @return
     */
    public static Picture voToObj(PictureVO pictureVO ){
        if (pictureVO == null){
            return null;
        }
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureVO,picture);
//        类型不同 需要转换
        picture.setTags(JSONUtil.toJsonStr(pictureVO.getTags()));
        return picture;
    }

    /**
     * picture对象转换成pictureVO  对象类转封装类
     * @param picture
     * @return
     */
    public static PictureVO objToVo(Picture picture){
        if (picture == null){
            return null;
        }
        PictureVO pictureVO = new PictureVO();
        BeanUtils.copyProperties(picture,pictureVO);
//        类型不同 需要转换
        pictureVO.setTags(JSONUtil.toList(picture.getTags(), String.class));
        return pictureVO;
    }
}
