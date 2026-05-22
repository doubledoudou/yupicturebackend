package com.example.yupicturebackend.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

@Getter
public enum PictureReviewStatusEnum {

    REVIEWING("审核中", 0),

    PASS("审核通过", 1),

    REJECT("审核拒绝", 2);

    private final String text;
    private final int value;

    PictureReviewStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value获取枚举值-看到枚举状态
     * @param value
     * @return
     */
    public static PictureReviewStatusEnum getEnumByValue(Integer value){
        if (ObjectUtil.isEmpty( value)){
            return null;
        }
        for(PictureReviewStatusEnum pictureReviewStatusEnum : PictureReviewStatusEnum.values()){
            if(pictureReviewStatusEnum.value== value){
                return pictureReviewStatusEnum;
            }
        }
        return null;
    }
}
