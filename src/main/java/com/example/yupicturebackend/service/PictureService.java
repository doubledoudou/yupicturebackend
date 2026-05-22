package com.example.yupicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yupicturebackend.model.dto.picture.PictureEditRequest;
import com.example.yupicturebackend.model.dto.picture.PictureQueryRequest;
import com.example.yupicturebackend.model.dto.picture.PictureReviewRequest;
import com.example.yupicturebackend.model.dto.picture.PictureUploadByBatchRequest;
import com.example.yupicturebackend.model.dto.picture.PictureUploadRequest;
import com.example.yupicturebackend.model.entity.Picture;
import com.example.yupicturebackend.model.entity.User;
import com.example.yupicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;


/**
* @author 19814
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-07-31 10:07:18
*/
public interface PictureService extends IService<Picture> {

    PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);

    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    void validPicture(Picture picture);
    
    /**
     * 检查图片操作权限
     * @param loginUser 当前登录用户
     * @param picture 要操作的图片
     */
    void checkPictureAuth(User loginUser, Picture picture);
    
    /**
     * 删除图片
     * @param pictureId 图片ID
     * @param loginUser 当前登录用户
     */
    void deletePicture(long pictureId, User loginUser);
    
    /**
     * 清理图片文件（异步）
     * @param picture 图片对象
     */
    void clearPictureFile(Picture picture);
    
    /**
     * 编辑图片
     * @param pictureEditRequest 编辑请求参数
     * @param loginUser 当前登录用户
     */
    void editPicture(PictureEditRequest pictureEditRequest, User loginUser);
    
    /**
     * 图片审核
     * @param pictureReviewRequest
     * @param loginUser
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 填充审核参数
     * @param picture
     * @param loginUser
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量抓取和创建图片
     * @param pictureUploadByBatchRequest
     * @param loginUser
     * @return 成功抓取的图片数
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);
}
