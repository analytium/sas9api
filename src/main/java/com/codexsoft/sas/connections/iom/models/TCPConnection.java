package com.codexsoft.sas.connections.iom.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@ApiModel(description = "Workspace server TCP connection object")
public class TCPConnection {

    @ApiModelProperty(value = "Metadata object SAS ID")
    private String id;

    @ApiModelProperty(value = "Workspace server host URL")
    private String host;

    @ApiModelProperty(value = "Workspace server port")
    private int port;

    @ApiModelProperty(value = "Connection description")
    private String description;

    @JsonIgnore
    public String getConnectionString() {
        return  "jdbc:sasiom://"
                + getHost()
                + ":"
                + getPort();
    }
}
