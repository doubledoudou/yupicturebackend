package com.example.yupicturebackend.model.dto.picture;

import lombok.Data;

@Data
public class PictureUploadByBatchRequest {

    /**
     * 搜索关键词
     */
    private String searchText;

    /**
     * 抓取数量
     */
    private Integer count = 10 ;
}
