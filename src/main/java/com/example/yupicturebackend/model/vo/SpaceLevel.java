package com.example.yupicturebackend.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 空间级别封装类
 */
@Data
@AllArgsConstructor
public class SpaceLevel {

    /**
     * 级别值
     */
    private int value;

    /**
     * 级别文本
     */
    private String text;

    /**
     * 最大图片数量
     */
    private long maxCount;

    /**
     * 最大图片总大小（字节）
     */
    private long maxSize;
}
