package com.codexsoft.sas.models;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Generic API response")
public class APIResponse<T> {
    @ApiModelProperty(value = "Response status code")
    private int status;

    @ApiModelProperty(value = "Response error message. Null for no error")
    private String error;

    @ApiModelProperty(value = "Response payload")
    private T payload;
}
