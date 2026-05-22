package com.example.yupicturebackend.common;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    @ApiModelProperty(value = "ID")
    private Long id;

    private static final long serialVersionUID = 1L;
}

