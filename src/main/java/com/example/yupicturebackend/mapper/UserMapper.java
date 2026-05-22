package com.example.yupicturebackend.mapper;

import com.example.yupicturebackend.model.entity.Picture;
import com.example.yupicturebackend.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;


/**
* @author 19814
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2025-07-17 16:03:12
* @Entity com.example.yupicturebackend.entity.User
*/
public interface UserMapper extends BaseMapper<User> {

    /**
    * @author 19814
    * @description 针对表【picture(图片)】的数据库操作Mapper
    * @createDate 2025-07-31 10:07:18
    * @Entity generator.domain.Picture
    */
    interface PictureMapper extends BaseMapper<Picture> {

    }
}




