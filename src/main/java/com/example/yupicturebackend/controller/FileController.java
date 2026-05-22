package com.example.yupicturebackend.controller;

import com.example.yupicturebackend.annotation.AuthCheck;
import com.example.yupicturebackend.common.BaseResponse;
import com.example.yupicturebackend.common.ResultUtils;
import com.example.yupicturebackend.constant.UserConstant;
import com.example.yupicturebackend.exception.BusinessException;
import com.example.yupicturebackend.exception.ErrorCode;
import com.example.yupicturebackend.manager.CosManager;

import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/file")
@Slf4j  //启用日志功能
public class FileController {
    @Resource
    private CosManager cosManager;

    /**
     * 测试上传文件-----测试接口
     * @param multipartFile 上传的文件
     * @return
     * 用户选择文件 → 点击上传
     *     ↓
     * 浏览器发送文件到后端
     *     ↓
     * 后端创建临时文件（中转站）
     *     ↓
     * 把文件内容复制到临时文件
     *     ↓
     * 上传到腾讯云 COS（云端仓库）
     *     ↓
     * 删除临时文件（清理中转站）
     *     ↓
     * 返回文件路径给前端
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")
    public BaseResponse<String> testUploadFile(@RequestPart("file") MultipartFile multipartFile){
        //文件目录
        String filename = multipartFile.getOriginalFilename();
        String filepath = String.format("/test/%s", filename);
        File file = null;
        try{
            //上传文件
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file);
            //返回可访问地址
            return ResultUtils.success(filepath);
        }catch (Exception e){
            log.error("文件上传失败，filepath={}", filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }finally{
            if(file!=null){
                //删除临时文件
                boolean delete = file.delete();
                if (!delete){
                    log.error("文件删除失败，filepath={}", filepath);
                }
            }
        }
    }

    /**
     * 测试下载文件
     * @param filepath
     * @param response
     * @throws IOException
     * 你点击下载链接
     *     ↓
     * 保安查身份（是管理员吗？）✅
     *     ↓
     * 告诉后端要哪个文件
     *     ↓
     * 去腾讯云仓库取文件
     *     ↓
     * 用桶接住文件内容
     *     ↓
     * 贴上"请下载"的标签
     *     ↓
     * 装进快递袋，发给浏览器
     *     ↓
     * 浏览器弹出"保存到哪儿？"
     *     ↓
     * 你选择位置，保存完成 ✅
     *     ↓
     * 后端清理现场（关闭流）
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/test/download")
    public void testDownloadFile(String filepath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInput = null;
        try{
            COSObject cosObject = cosManager.getObject(filepath);
            cosObjectInput = cosObject.getObjectContent();
            //处理下载到的流
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            //设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + filepath);
            //写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        }catch (Exception e){
            log.error("文件下载失败，filepath="+filepath,e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        }finally {
            if (cosObjectInput != null){
                cosObjectInput.close();
            }
        }
    }
}
