package com.example.yupicturebackend.service.Impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yupicturebackend.common.BaseResponse;
import com.example.yupicturebackend.config.CosClientConfig;
import com.example.yupicturebackend.exception.BusinessException;
import com.example.yupicturebackend.exception.ErrorCode;
import com.example.yupicturebackend.exception.ThrowUtils;
import com.example.yupicturebackend.manager.CosManager;
import com.example.yupicturebackend.manager.FileManager;
import com.example.yupicturebackend.manager.upload.FilePictureUpload;
import com.example.yupicturebackend.manager.upload.PictureUploadTemplate;
import com.example.yupicturebackend.manager.upload.UrlPictureUpload;
import com.example.yupicturebackend.mapper.PictureMapper;
import com.example.yupicturebackend.model.dto.file.UploadPictureResult;
import com.example.yupicturebackend.model.dto.picture.PictureEditRequest;
import com.example.yupicturebackend.model.dto.picture.PictureQueryRequest;
import com.example.yupicturebackend.model.dto.picture.PictureReviewRequest;
import com.example.yupicturebackend.model.dto.picture.PictureUploadByBatchRequest;
import com.example.yupicturebackend.model.dto.picture.PictureUploadRequest;
import com.example.yupicturebackend.model.entity.Picture;
import com.example.yupicturebackend.model.entity.Space;
import com.example.yupicturebackend.model.entity.User;
import com.example.yupicturebackend.model.enums.PictureReviewStatusEnum;
import com.example.yupicturebackend.model.vo.PictureVO;
import com.example.yupicturebackend.model.vo.UserVO;
import com.example.yupicturebackend.service.PictureService;
import com.example.yupicturebackend.service.SpaceService;
import com.example.yupicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author 19814
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-07-31 10:07:18
*/
@Slf4j
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {
    private final FileManager fileManager;
    private final UserService userService;
    private final SpaceService spaceService;

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Resource
    private CosManager cosManager;

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private TransactionTemplate transactionTemplate;

    public PictureServiceImpl(FileManager fileManager, UserService userService, SpaceService spaceService) {
        this.fileManager = fileManager;
        this.userService = userService;
        this.spaceService = spaceService;
    }

    /**
     * 上传图片
     * 接收：用户上传的图片文件，图片上传的请求参数，登录用户
     * 返回：脱敏后的图片对象
     * @param inputSource
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        if (inputSource == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片为空");
        }
//        先判断有没有登录，没登陆就提示未登录
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
//        校验空间是否存在和权限
        Long spaceId = pictureUploadRequest != null ? pictureUploadRequest.getSpaceId() : null;
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            // 必须空间创建人（管理员）才能上传
            if (!loginUser.getId().equals(space.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
            }
            // 校验额度
            if (space.getTotalCount() >= space.getMaxCount()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间条数不足");
            }
            if (space.getTotalSize() >= space.getMaxSize()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间大小不足");
            }
        }
//        初始化pictureId为空 用于存储待操作的图片id
        Long pictureId = null;
//        如果不为空，则表示为已有图片
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }
//        如果是更新图片，需要校验图片是否存在
        if (pictureId != null){
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
//            仅本人或管理员可编辑 自己只能编辑自己的图 管理员可以编辑所有人的
//            如果不是管理员和本人就报错-无权限
            if(!oldPicture.getUserId().equals(loginUser.getId())&&!userService.isAdmin(loginUser))
            {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
//            校验空间是否一致
//            没传 spaceId，则复用原有图片的 spaceId
            if (spaceId == null) {
                if (oldPicture.getSpaceId() != null) {
                    spaceId = oldPicture.getSpaceId();
                }
            } else {
//                传了 spaceId，必须和原有图片一致
                if (ObjUtil.notEqual(spaceId, oldPicture.getSpaceId())) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间 id 不一致");
                }
            }
        }
//        上传图片，得到信息
//        按照空间划分目录
        String uploadPathPrefix;
        if (spaceId == null) {
            uploadPathPrefix = String.format("public/%s", loginUser.getId());
        } else {
            uploadPathPrefix = String.format("space/%s", spaceId);
        }
//        根据inputSource类型区分上传方式
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
//        构造要入库的图片信息
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
//        设置空间 id
        picture.setSpaceId(spaceId);
//        补充审核参数
        fillReviewParams(picture, loginUser);
//        如果pictureId不为空，则表示更新，否则是新增
        if (pictureId != null){
//            如果是更新，需要补充Id和编辑时间
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        
        // 开启事务，保存图片记录并更新额度
        Long finalSpaceId = spaceId;
        transactionTemplate.execute(status -> {
            boolean result = this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
            if (finalSpaceId != null) {
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, finalSpaceId)
                        .setSql("totalSize = totalSize + " + picture.getPicSize())
                        .setSql("totalCount = totalCount + 1")
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "额度更新失败");
            }
            return picture;
        });
        
        return PictureVO.objToVo(picture);
    }




    /**
     * 对于分页查询接口 需要根据用户传入的参数来构造SQL查询
     * 由于使用MyBatisPlus，不用自己拼接SQL，通过QueryWrapper构造对象来生成SQL
     * 这个方法是 将查询请求转为QueryWrapper对象
     * @param pictureQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest){
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null){
            return queryWrapper;
        }
//        从对象中取值
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        Long spaceId = pictureQueryRequest.getSpaceId();
        boolean nullSpaceId = pictureQueryRequest.isNullSpaceId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
//        支持根据审核字段查询
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
//        从多字段中搜索
        if (StrUtil.isNotBlank(searchText)){
//        需要拼接查询条件  eq是等于 like是模糊匹配
            queryWrapper.and(qw->qw.like("name", searchText).or().like("introduction", searchText));
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.isNull(nullSpaceId, "spaceId");
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
//        JSON数组查询
        if (CollUtil.isNotEmpty( tags)){
            for (String tag : tags){
                queryWrapper.like("tags","\""+ tag+"\"");
            }
        }
//        排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField),sortOrder.equals("ascend"),sortField);
        return queryWrapper;
    }

    /**
     * 封装类--获取单个图片的封装
     * @param picture
     * @param request
     * @return
     */
    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request){
//        对象转封装类
        PictureVO pictureVO = PictureVO.objToVo(picture);
//        关联查询用户信息
        Long userId = picture.getUserId();
        if (userId != null && userId > 0){
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    /**
     * 分页获取图片封装类，不是针对每条数据都查询一次用户，而是先获取到要查询的用户id列表，
     * 只发送一次用户表的查询请求，再将查询到的值设置到图片对象中
     * 将Picture->PictureVO
     * @param picturePage
     * @param request
     * @return
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request){
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)){
            return pictureVOPage;
        }
//        对象列表=》封装对象列表
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
//       提取所有用户id集合   每个图片都关联了一个上传用户
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
//       批量查询所有相关用户信息
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
//        填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)){
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    /**
     * 图片数据校验方法，防止不合法的数据
     * 接收：待校验的图片对象
     * @param picture
     */
    @Override
    public void validPicture(Picture picture){
//        空对象检查
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
//        拿到id,url,introduction
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
//        id检查，修改数据时，id不能为空，有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull( id), ErrorCode.PARAMS_ERROR, "id不能为空");
//        url长度检查 <1024
        if (StrUtil.isNotBlank(url)){
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url过长");
        }
//        简介长度检查 >800
        if (StrUtil.isNotBlank(introduction)){
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }


    /**
     * 审核操作
     * 接受待审核的id，和目标状态，当前登录者信息
     * 无返回值，直接修改数据库的记录
     * @param pictureReviewRequest
     * @param loginUser
     */
    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
//        1.先拿到图片id和要改成的状态
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
//        2.根据值看到枚举状态 0审核中 1审核通过 2审核拒绝
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
//        验空，验证拿到的图片id和状态是否为空 状态不能为审核中，审核中也报错，审核中为初始状态
        if (id==null || reviewStatusEnum == null ||PictureReviewStatusEnum.REVIEWING.equals(reviewStatusEnum)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
//        根据id去数据库查询对象，并返回图片信息
        Picture oldPicture = this.getById(id);
//        从数据库查不到就要报-请求数据不存在
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
//        检查图片当前的审核状态是否与要设置的审核状态相同
//        如果相同则抛出业务异常，提示"请勿重复审核"
        if(oldPicture.getReviewStatus().equals(reviewStatus)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请勿重复审核");
        }
//        创建一个新对象用于更新
        Picture updatePicture = new Picture();
//        复制
        BeanUtils.copyProperties(pictureReviewRequest, updatePicture);
//        设置审核人为当前登录者
        updatePicture.setReviewerId(loginUser.getId());
//        设置审核时间为当前时间
        updatePicture.setReviewTime(new Date());
//        根据id更新图片记录
        boolean result = this.updateById(updatePicture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    /**
     * 构建审核状态
     * @param loginUser
     * @param oldPicture
     * @param reviewStatus
     * @param reviewMessage
     * @return
     */
    private Picture buildUpdateReviewPicture(User loginUser, Picture oldPicture, Integer reviewStatus, String reviewMessage){
//        构建审核状态的图片对象
        Picture updatePicture = new Picture();
//        复制原图片到新对象
        BeanUtils.copyProperties(oldPicture, updatePicture);
//        设置状态
        updatePicture.setReviewStatus(reviewStatus);
//        审核信息
        updatePicture.setReviewMessage(reviewMessage);
//        审核人ID
        updatePicture.setReviewerId(loginUser.getId());
//        审核时间
        updatePicture.setReviewTime(new Date());
//        返回更新后的图片对象
        return updatePicture;
    }

    /**
     * 检查图片操作权限
     * 如果要删除的图片有空间id，表示是用户上传到私有空间中的图片，那么登录用户必须是空间的管理员(也就是创建者)，系统管理员也不能随意删除私有空间的图片。
     * @param loginUser 当前登录用户
     * @param picture 要操作的图片
     */
    @Override
    public void checkPictureAuth(User loginUser, Picture picture) {
        Long spaceId = picture.getSpaceId();
        if (spaceId == null) {
            // 公共图库，仅本人或管理员可操作
            if (!picture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        } else {
            // 私有空间，仅空间管理员可操作
            if (!picture.getUserId().equals(loginUser.getId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
    }

    /**
     * 删除图片
     * @param pictureId 图片ID
     * @param loginUser 当前登录用户
     */
    @Override
    public void deletePicture(long pictureId, User loginUser) {
        ThrowUtils.throwIf(pictureId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 判断是否存在
        Picture oldPicture = this.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 校验权限
        checkPictureAuth(loginUser, oldPicture);
        
        // 开启事务，删除图片并释放额度
        transactionTemplate.execute(status -> {
            // 操作数据库
            boolean result = this.removeById(pictureId);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
            // 释放额度
            Long spaceId = oldPicture.getSpaceId();
            if (spaceId != null) {
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, spaceId)
                        .setSql("totalSize = totalSize - " + oldPicture.getPicSize())
                        .setSql("totalCount = totalCount - 1")
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "额度更新失败");
            }
            return true;
        });
        
        // 异步清理文件
        this.clearPictureFile(oldPicture);
    }

    /**
     * 清理图片文件（异步）
     * @param picture 图片对象
     */
    @Override
    public void clearPictureFile(Picture picture) {
        if (picture == null || StrUtil.isBlank(picture.getUrl())) {
            return;
        }
        try {
            // 从 URL 中提取 COS 对象的 key
            String url = picture.getUrl();
            String host = cosClientConfig.getHost();
            // 如果 URL 以 host 开头，提取 key
            if (url.startsWith(host)) {
                String key = url.substring(host.length());
                // 删除原图
                cosManager.deleteObject(key);
                // 删除压缩图（webp格式）
                String webpKey = FileUtil.mainName(key) + ".webp";
                cosManager.deleteObject(webpKey);
                log.info("图片文件清理成功，key={}", key);
            }
        } catch (Exception e) {
            // 记录错误日志，但不影响主流程
            log.error("清理图片文件失败，pictureId={}", picture.getId(), e);
        }
    }

    /**
     * 编辑图片
     * @param pictureEditRequest 编辑请求参数
     * @param loginUser 当前登录用户
     */
    @Override
    public void editPicture(PictureEditRequest pictureEditRequest, User loginUser) {
        // 在此处将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        // 设置编辑时间
        picture.setEditTime(new Date());
        // 数据校验
        this.validPicture(picture);
        // 判断是否存在
        long id = pictureEditRequest.getId();
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 校验权限
        checkPictureAuth(loginUser, oldPicture);
        // 补充审核参数
        this.fillReviewParams(picture, loginUser);
        // 操作数据库
        boolean result = this.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    /**
     * 通用"补充审核参数"方法
     * 图片上传、用户编辑、管理员更新这三个操作都需要审核状态
     * 接受：图片对象，当前登录用户信息
     * 返回：无
     * @param picture
     * @param loginUser
     */
    @Override
    public void fillReviewParams(Picture picture, User loginUser){
        if(userService.isAdmin(loginUser)){
//            如果登录者是管理员则自动过审
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewMessage("管理员自动过审");
            picture.setReviewTime(new Date());
        }
        else{
//            非管理员则默认待审核
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        String searchText = pictureUploadByBatchRequest.getSearchText();
//        格式化数量
        Integer count = pictureUploadByBatchRequest.getCount();
        ThrowUtils.throwIf(count > 30, ErrorCode.PARAMS_ERROR, "数量不能超过30");
        log.info("========== 开始批量抓取图片 ==========");
        log.info("搜索关键词：{}，目标数量：{}", searchText, count);
//        要抓取的地址
        String fetchUrl = String.format("https://cn.bing.com/image/search?q=%s&mmasync=1",searchText);
        log.info("请求URL：{}", fetchUrl);
        Document document;
        try{
            document = Jsoup.connect(fetchUrl).get();
        }catch (IOException e){
            log.error("抓取图片失败",fetchUrl, e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "抓取图片失败");
        }
        Element div = document.getElementsByClass("div.collage_p_coldt[src]").first();
        if(ObjUtil.isNull( div)){
            log.error("未找到 div.collage_p_coldt[src] 元素，Bing页面结构可能已改变");
            log.info("尝试其他选择器...");
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未找到图片");
        }
        Elements imgElementList = div.select("img.ming");
        int uploadCount = 0;
        for (Element imgElement : imgElementList) {
            String fileUrl = imgElement.attr("src");
            if (StrUtil.isBlank(fileUrl)){
                log.info("当前链接为空，已跳过：{}", fileUrl);
                continue;
            }
//            处理图片上传地址，防止出现转义问题
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex != -1){
                fileUrl = fileUrl.substring(0, questionMarkIndex);
            }
            //        上传图片
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            try {
                PictureVO pictureVO = this.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
                log.info("图片上传成功，id={}", pictureVO.getId());
                uploadCount++;
            }catch (Exception e){
                log.error("图片上传失败", e);
                continue;
            }
            if (uploadCount >= count){
                break;
            }
        }
        return uploadCount;
    }


}




