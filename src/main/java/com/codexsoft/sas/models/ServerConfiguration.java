package com.codexsoft.sas.models;

import com.codexsoft.sas.connections.iom.models.Repository;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@ApiModel(description = "Server configuration object")
public class ServerConfiguration {
    @ApiModelProperty(value = "Metadata server host URL")
    private String metadataHost;

    @ApiModelProperty(value = "Metadata server port")
    private int metadataPort;

    @ApiModelProperty(value = "Metadata server name")
    private String metadataServerName;

    @ApiModelProperty(value = "List of available repositories for the metadata server")
    private List<Repository> repositories;
}
