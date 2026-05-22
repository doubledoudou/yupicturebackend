package com.example.yupicturebackend.controller;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yupicturebackend.annotation.AuthCheck;
import com.example.yupicturebackend.common.BaseResponse;
import com.example.yupicturebackend.common.DeleteRequest;
import com.example.yupicturebackend.common.ResultUtils;
import com.example.yupicturebackend.constant.UserConstant;
import com.example.yupicturebackend.exception.BusinessException;
import com.example.yupicturebackend.exception.ErrorCode;
import com.example.yupicturebackend.exception.ThrowUtils;
import com.example.yupicturebackend.model.dto.picture.*;
import com.example.yupicturebackend.model.entity.Picture;
import com.example.yupicturebackend.model.entity.Space;
import com.example.yupicturebackend.model.entity.User;
import com.example.yupicturebackend.model.enums.PictureReviewStatusEnum;
import com.example.yupicturebackend.model.vo.PictureVO;
import com.example.yupicturebackend.service.PictureService;
import com.example.yupicturebackend.service.SpaceService;
import com.example.yupicturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/picture")
public class PictureController {
    @Resource
    private PictureService pictureService;
    @Resource
    private UserService userService;
    @Resource
    private SpaceService spaceService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 上传图片接口，之前仅管理员可用，现在开放给用户
     * @param multipartFile  是Spring用来处理上传文件的接口，表示用户上传的图片文件
     * @param pictureUploadRequest
     * @param request
     * @return
     */
    @PostMapping("/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, HttpServletRequest request){
//        获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile,pictureUploadRequest,loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 删除图片接口，仅本人/管理员可用
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request){
        if (deleteRequest == null || deleteRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
//        获取当前登录的用户对象
        User loginUser = userService.getLoginUser(request);
//        拿到要删除的id
        long id = deleteRequest.getId();
//        调用Service层删除图片方法
        pictureService.deletePicture(id, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 更改图片，仅管理员可用
     * 接受：pictureUpdateRequest请求参数，当前登录信息
     * 返回：成功返回true 失败抛出错误
     * @param pictureUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request){
        if (pictureUpdateRequest == null || pictureUpdateRequest.getId()<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
//        将实体类和DTO进行转换
//        创建一个新的Picture对象
        Picture picture = new Picture();
//        使用BeanUtils进行属性复制 将pictureUpdateRequest对象属性复制到picture对象中 实现DTO到实体类的转换
        BeanUtils.copyProperties(pictureUpdateRequest,picture);
//        注意将list转为string
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
//        调用参数校验方法
        pictureService.validPicture(picture);
//        判断是否存在
        long id = pictureUpdateRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
//        补充审核参数
        User loginUser = userService.getLoginUser(request);
        pictureService.fillReviewParams(picture,loginUser);
//        操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据id获取图片（仅管理员可用）
     * @param id
     * @param request
     * @return 返回封装后的
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> getPictureById(@RequestParam Long id, HttpServletRequest request){
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
//        查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null,ErrorCode.NOT_FOUND_ERROR, "图片不存在");
//        空间权限校验（即使是管理员也需要遵循空间权限规则）
        Long spaceId = picture.getSpaceId();
        if (spaceId != null) {
            User loginUser = userService.getLoginUser(request);
            pictureService.checkPictureAuth(loginUser, picture);
        }
//        获取封装类
        return ResultUtils.success(PictureVO.objToVo(picture));
    }


    /**
     * 根据id获取图片VO-所有人可用
     * @param id
     * @param request
     * @return 返回封装后的
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(@RequestParam Long id, HttpServletRequest request){
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
//        查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null,ErrorCode.NOT_FOUND_ERROR, "图片不存在");
//        空间权限校验
        Long spaceId = picture.getSpaceId();
        if (spaceId != null) {
            User loginUser = userService.getLoginUser(request);
            pictureService.checkPictureAuth(loginUser, picture);
        }
//        获取封装类
        return ResultUtils.success(pictureService.getPictureVO(picture,request));
    }


    /**
     * 分页获取图片列表（仅管理员可用）
     * 默认只能查看已过审的数据
     * @param pictureQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<PictureVO>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
//        当前页码
        long current = pictureQueryRequest.getCurrent();
//        当前页大小
        long size = pictureQueryRequest.getPageSize();
//        普通用户默认只能查看已过审的数据
//        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        // 返回 VO 分页
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }


    /**
     * 分页获取图片列表（封装类）
     * 接收:pictureQueryRequest查询条件对象（包括页码，大小，查询条件），request当前登录信息
     * 返回：封装后的图片对象，分页信息
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                             HttpServletRequest request) {
//        拿到当前页码和页大小
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫-控制页大小
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        
        // 空间权限校验
        Long spaceId = pictureQueryRequest.getSpaceId();
        if (spaceId != null) {
            // 查询特定空间的图片，必须是空间管理员
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            // 校验是否为空间管理员
            if (!loginUser.getId().equals(space.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
            }
            // 私有空间不需要指定审核条件（私有空间没有审核机制）
            pictureQueryRequest.setReviewStatus(null);
        } else {
            // 查询公共图库，只能查看过审的数据
            pictureQueryRequest.setNullSpaceId(true);
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        }
        
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }


    /**
     * 编辑图片（仅本人或管理员可用）
     * @param pictureEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request){
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用Service层编辑图片方法
        pictureService.editPicture(pictureEditRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 获取图片标签和分类
     * @return
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory(){
//        首先创建一个PictureTagCategory对象
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
//        设置预定的标签列表和分类列表
        List<String> tagList = Arrays.asList("热门","搞笑","生活","高清","艺术","校园","背景","简历","创意");
        List<String> categoryList = Arrays.asList("模板","电商","表情包","素材","海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
//        成功
        return ResultUtils.success(pictureTagCategory);
    }

    /**
     * 图片审核接口，注意权限设置为仅管理员可用
     * 接收：图片审核请求，当前登录者信息
     * 返回：成功返回true，失败就报错
     * @param pictureReviewRequest
     * @param request
     * @return
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest, HttpServletRequest request){
        //首先校验请求参数pictureReviewRequest是否为空
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
//        获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);
//        调用doPictureReview方法执行审核操作
        pictureService.doPictureReview(pictureReviewRequest,loginUser);
//        成功
        return ResultUtils.success(true);
    }

    /**
     * 通过url上传图片(可重新上传)
     * @param pictureUploadRequest
     * @param request
     * @return
     */
    @PostMapping("/upload/url")
    public BaseResponse<PictureVO> uploadPictureByUrl(@RequestBody PictureUploadRequest pictureUploadRequest, HttpServletRequest request){
          User loginUser = userService.getLoginUser(request);
          String fileUrl = pictureUploadRequest.getFileUrl();
          PictureVO  pictureVO = pictureService.uploadPicture(fileUrl,pictureUploadRequest,loginUser);
          return ResultUtils.success(pictureVO);
    }

    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(
            @RequestBody PictureUploadByBatchRequest pictureUploadBatchRequest,
            HttpServletRequest request
    ){
        ThrowUtils.throwIf(pictureUploadBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        int uploadCount = pictureService.uploadPictureByBatch(pictureUploadBatchRequest,loginUser);
        return ResultUtils.success(uploadCount);
    }

    /**
     * 分页获取图片列表（封装类，带缓存）- 已废弃
     * 由于私有空间的权限控制逻辑复杂，缓存策略难以统一，暂不使用此接口
     * @deprecated 请使用 {@link #listPictureVOByPage(PictureQueryRequest, HttpServletRequest)} 代替
     */
    @Deprecated
    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                                      HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 普通用户默认只能查看已过审的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());

        // 构建缓存 key，转成Json
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        // md5加密
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        //用md5生产指纹，拼接完整的key
        String redisKey = "yupicture:listPictureVOByPage:" + hashKey;
        // 从 Redis 缓存中查询
        ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
        String cachedValue = valueOps.get(redisKey);
        if (cachedValue != null) {
            // 如果缓存命中，返回结果
            Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachedPage);
        }

        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        // 获取封装类
        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);

        // 存入 Redis 缓存
        String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
        // 5 - 10 分钟随机过期，防止雪崩
        int cacheExpireTime = 300 +  RandomUtil.randomInt(0, 300);
        valueOps.set(redisKey, cacheValue, cacheExpireTime, TimeUnit.SECONDS);

        // 返回结果
        return ResultUtils.success(pictureVOPage);
    }

}























