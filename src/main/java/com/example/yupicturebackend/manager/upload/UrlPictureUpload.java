package com.example.yupicturebackend.manager.upload;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.example.yupicturebackend.exception.BusinessException;
import com.example.yupicturebackend.exception.ErrorCode;
import com.example.yupicturebackend.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

//新增 URL 图片上传子类 UrlPictureUpload（子）
@Service
@Slf4j
public class UrlPictureUpload extends PictureUploadTemplate{

    /**
     * 验证图片
     * @param inputSource
     */
    @Override
    protected void validPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
        ThrowUtils.throwIf(fileUrl == null, ErrorCode.PARAMS_ERROR, "文件 url 不能为空");
        
        // 1、验证是否是合法的 URL
        try{
            new URL(fileUrl);
        }catch (MalformedURLException e){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
        }
        
        // 2、校验 URL 协议
        ThrowUtils.throwIf(!(fileUrl.startsWith("http://") || fileUrl.startsWith("https://")),
                ErrorCode.PARAMS_ERROR, "仅支持 HTTP 或 HTTPS 协议的文件地址");
        
        // 3、发送 HEAD 请求以验证文件是否存在
        HttpResponse response = null;
        try{
            // HTTP HEAD 请求只获取响应头，不下载文件体
            response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            // 未正常返回，无需执行其他判断
            if (response.getStatus() != HttpStatus.HTTP_OK){
                return;
            }
            
            // 4、校验文件类型---服务器会在响应头中告诉客户端文件类型
            String contentType = response.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)){
                // 允许的图片类型
                final List<String> ALLOW_FORMAT_LIST = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
                // 如果 Content-Type 不符合，尝试通过 URL 后缀名校验
                if (!ALLOW_FORMAT_LIST.contains(contentType.toLowerCase())) {
                    // 从 URL 中提取文件后缀进行二次校验
                    String fileSuffix = FileUtil.getSuffix(fileUrl).toLowerCase();
                    final List<String> ALLOW_SUFFIX_LIST = Arrays.asList("jpg", "jpeg", "png", "webp", "gif");

                    // 如果后缀也不匹配，才抛出异常
                    if (!ALLOW_SUFFIX_LIST.contains(fileSuffix)) {
                        log.warn("图片格式校验失败，contentType={}, url={}", contentType, fileUrl);
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件格式错误，当前类型：" + contentType);
                    }
                }
            }
            
            // 5、校验文件大小
            String contentLengthStr = response.header("Content-Length");
            if(StrUtil.isNotBlank(contentLengthStr)){
                try{
                    long contentLength = Long.parseLong(contentLengthStr);
                    final long TWO_MB = 1024 * 1024 * 2;  //限制文件大小为 2MB
                    ThrowUtils.throwIf(contentLength > TWO_MB, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2MB");
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
     * 获取文件名
     * @param inputSource
     * @return
     */
    @Override
    protected String getOriginFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
//        从url中获取文件名
        return FileUtil.mainName(fileUrl);
    }

    /**
     * 下载图片
     * @param inputSource
     * @param file
     */
    @Override
    protected void processFile(Object inputSource, File file){
        String fileUrl = (String) inputSource;
//        下载文件到临时目录
        HttpUtil.downloadFile(fileUrl, file);
    }
}
