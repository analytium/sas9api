package com.codexsoft.sas.connections.iom.models;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "Repository object")
public class Repository implements Serializable {
    @ApiModelProperty(value = "Metadata object SAS ID")
    private String id;

    @ApiModelProperty(value = "Repository name")
    private String name;

    @ApiModelProperty(value = "Repository description")
    private String desc;

    @ApiModelProperty(value = "Default repository namespace")
    private String defaultNS;
}
