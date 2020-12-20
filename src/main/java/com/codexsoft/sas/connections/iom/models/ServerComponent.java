package com.codexsoft.sas.connections.iom.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "ServerComponent metadata object representation")
public class ServerComponent {
    @ApiModelProperty(value = "Metadata object SAS ID")
    private String id;

    @ApiModelProperty(value = "Name of ServerContext object the ServerComponent is assigned to")
    private String name;

    @ApiModelProperty(value = "Type of the metadata object. 'Server.Workspace' for workspace server objects")
    private String publicType;

    @ApiModelProperty(value = "List of TCP connections to the server")
    private List<TCPConnection> connections;

    @JsonIgnore
    public TCPConnection getConnection() throws Exception {
        List<TCPConnection> serverConnections = getConnections();
        if (serverConnections.size() == 0) {
            throw new Exception("No TCP connections defined for server '" + name + "'");
        }

        return serverConnections.get(0);
    }
}
