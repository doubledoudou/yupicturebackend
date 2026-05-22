package com.example.yupicturebackend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.example.yupicturebackend.config.CosClientConfig;
import com.example.yupicturebackend.exception.BusinessException;
import com.example.yupicturebackend.exception.ErrorCode;
import com.example.yupicturebackend.exception.ThrowUtils;
import com.example.yupicturebackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.net.URL;

/**
 * 更贴合业务的文件上传服务 该服务提供一个上传图片并返回图片解析信息的方法
 *
 * 已废弃。改为使用upload包里的模板优化
 */
@Deprecated
@Service
@Slf4j
public class FileManager {
    @Resource
    private CosClientConfig cosClientConfig;
    @Resource
    private CosManager cosManager;

    public UploadPictureResult uploadPicture(MultipartFile multipartFile,String uploadPathPrefix){
//        校验图片
        validPicture(multipartFile);
//        图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originFilename = multipartFile.getOriginalFilename();
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
        File file = null;
        try{
//          创建临时文件
            file = File.createTempFile(uploadPath, null);
            multipartFile.transferTo(file);
//          上传图片
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
//          封装返回结果
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
            uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setUrl(cosClientConfig.getHost() +"/"+ uploadPath);
            return uploadPictureResult;
        }catch (Exception e){
            log.error("上传图片失败",e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传图片失败");
        }finally {
            this.deleteTempFile(file);
        }
    }


    /**
     * 校验图片---由于文件校验复杂 单独抽象为validPicture方法，对文件大小、类型进行校验
     * @param multipartFile
     */
    private void validPicture(MultipartFile multipartFile){
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
//        校验文件大小
        long fileSize = multipartFile.getSize();
        final long ONE_M = 1024 * 1024;
        ThrowUtils.throwIf(fileSize > 2* ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过2M");
//        校验文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
//        允许上传的文件后缀
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpg", "jpeg", "png", "webp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件格式错误");
    }

    /**
     * 校验url图片---由于文件校验复杂 单独抽象为validPicture方法，对文件大小、类型进行校验
     * @param fileUrl
     */
    private void validPicture(String fileUrl){
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件不能为空");
        try{
//          1、验证是否是合法的URL
            new URL(fileUrl);
        }catch (MalformedURLException  e){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
        }
//        2、校验URL协议
        ThrowUtils.throwIf(!(fileUrl.startsWith("http://") || fileUrl.startsWith("https://")),
                ErrorCode.PARAMS_ERROR, "仅支持HTTP或HTTPS协议的文件地址");
//        3、发送HEAD请求以验证文件是否存在
        HttpResponse response = null;
        try{
            // 思考：如何在不下载文件的情况下获取文件信息？
//              答案：HTTP HEAD 请求只获取响应头，不下载文件体
            response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
//            未正常返回，无需执行其他判断
            if (response.getStatus()!= HttpStatus.HTTP_OK){
                return;
            }
//            4、校验文件类型---服务器会在响应头中告诉客户端文件类型
            String contentType = response.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)){
//                允许的图片类型
                final List<String> ALLOW_FORMAT_LIST = Arrays.asList("image/jpeg", "image/jpg","image/png", "image/webp");
                ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(contentType.toLowerCase()), ErrorCode.PARAMS_ERROR, "文件格式错误");
            }
//            5、校验文件大小
            String contentLengthStr = response.header("Content-Length");
            if(StrUtil.isNotBlank(contentLengthStr)){
                try{
                    long contentLength = Long.parseLong(contentLengthStr);
                    final long TWO_MB = 1024 * 1024 * 2;  //限制文件大小为2MB
                    ThrowUtils.throwIf(contentLength > TWO_MB, ErrorCode.PARAMS_ERROR, "文件大小不能超过2MB");
                }catch (NumberFormatException e){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件大小转换错误");
                }
            }
        }finally {
            if (response != null){
                response.close();  //关闭 HTTP 连接，释放资源
            }
        }
    }

    /**
     * 删除临时文件
     * @param file
     */
    public void deleteTempFile(File file){
        if (file != null){
            return;
        }
//        删除临时文件
        boolean deleteResult = file.delete();
        if (!deleteResult){
            log.error("文件删除失败，filepath ={}", file.getAbsolutePath());
        }
    }

    public UploadPictureResult uploadPictureByUrl(String fileUrl, String uploadPathPrefix){
//        校验图片
        validPicture(fileUrl);
//        图片上传地址
        String uuid = RandomUtil.randomString(16);
//        String originFilename = multipartFile.getOriginalFilename();
        String originFilename = FileUtil.getName(fileUrl);
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
        File file = null;
        try{
//            创建临时文件
            file = File.createTempFile(uploadPath, null);
            HttpUtil.downloadFile(fileUrl, file);
//            上传图片
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
//            解析图片，提取元信息
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
//            封装一下
//            先new一个对象出来装
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
//            计算宽高比，并保留两位小数，又转回double型
            double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
            uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setUrl(cosClientConfig.getHost() +"/"+ uploadPath);
            return uploadPictureResult;
        }catch (Exception e){
            log.error("上传图片失败",e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传图片失败");
        }finally {
            this.deleteTempFile(file);
        }
    }
}
