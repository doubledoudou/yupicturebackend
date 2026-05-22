package com.example.yupicturebackend.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.example.yupicturebackend.config.CosClientConfig;
import com.example.yupicturebackend.exception.BusinessException;
import com.example.yupicturebackend.exception.ErrorCode;
import com.example.yupicturebackend.manager.CosManager;
import com.example.yupicturebackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

//新建图片上传模板(父)--抽象类PictureUploadTemplate
@Slf4j
public abstract class PictureUploadTemplate {
    @Resource
    protected CosManager cosManager;
    @Resource
    protected CosClientConfig cosClientConfig;

//  inputSource上传的输入源->有可能是表单上传的文件、url、本地file---所以采用Object
//  uploadPathPrefix是上传路径的前缀->存储在COS中的路径的前缀

    /*图片上传模板
     * @param inputSource
     * @param uploadPathPrefix
     * @return
     */
    public final UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix){
//             1、校验图片
        validPicture(inputSource);
//             2、图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originFilename = getOriginFilename(inputSource);
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
        File file = null;
        try {
            // 创建临时文件
            file = File.createTempFile(uploadPath, null);
            // 处理文件来源（本地或 URL）
            processFile(inputSource, file);
            // 上传图片到对象存储
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            if (CollUtil.isNotEmpty(objectList)) {
                CIObject compressedCiObject = objectList.get(0);
                // 封装压缩图返回结果
                return buildResult(originFilename, compressedCiObject);
            }
            // 封装原图返回结果
            return buildResult(originFilename, file, uploadPath, imageInfo);
        } catch (CosServiceException e) {
            log.error("云存储服务异常，uploadPath={}", uploadPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "云存储服务异常：" + e.getMessage());
        } catch (CosClientException e) {
            log.error("图片上传到对象存储失败，uploadPath={}", uploadPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传到云存储失败：" + e.getMessage());
        } catch (IOException e) {
            log.error("文件读写失败，uploadPath={}", uploadPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件读写失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("图片上传到对象存储失败，uploadPath={}, 错误类型={}", uploadPath, e.getClass().getName(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传图片失败：" + e.getMessage());
        } finally {
//            6、删除临时文件
            deleteTempFile(file);
        }
    }

    /**
     * 校验图片--校验输入源（本地文件或URL）
     * @param inputSource
     */
    protected abstract void validPicture(Object inputSource);

    /**
     * 获取原始文件名--获取输入源的原始文件名
     * @param inputSource
     * @return
     */
    protected abstract String getOriginFilename(Object inputSource);

    /**
     * 处理文件来源（本地文件或URL）-处理输入源的原始文件夹
     * @param inputSource
     * @param file
     */
    protected abstract void processFile(Object inputSource, File file);

    /**
     * 构建上传结果--封装返回结果（压缩图）
     * @param originFilename 原始文件名
     * @param compressedCiObject 压缩后的图片对象
     * @return 上传结果
     */
    private UploadPictureResult buildResult(String originFilename, CIObject compressedCiObject) {
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        int picWidth = compressedCiObject.getWidth();
        int picHeight = compressedCiObject.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(compressedCiObject.getFormat());
        uploadPictureResult.setPicSize(compressedCiObject.getSize().longValue());
        // 设置图片为压缩后的地址
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + compressedCiObject.getKey());
        return uploadPictureResult;
    }

    /**
     * 构建上传结果--封装返回结果（原图）
     * @param originFilename 原始文件名
     * @param file 临时文件
     * @param uploadPath 上传路径
     * @param imageInfo 图片信息
     * @return 上传结果
     */
    private UploadPictureResult buildResult(String originFilename, File file, String uploadPath, ImageInfo imageInfo) {
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        uploadPictureResult.setPicSize(file.length());
        // 设置图片为原图地址
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
        return uploadPictureResult;
    }


    /**
     * 删除临时文件
     * @param file
     */
    protected void deleteTempFile(File file){
            if (file == null){
                return;
            }
            boolean deleteResult = file.delete();
            if (!deleteResult){
                log.error("文件删除失败，filepath={}", file.getAbsolutePath());
            }
        }
}
