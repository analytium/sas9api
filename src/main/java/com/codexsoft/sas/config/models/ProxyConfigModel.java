package com.codexsoft.sas.config.models;

import com.codexsoft.sas.connections.ConnectionProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties("proxy")
@Data
public class ProxyConfigModel {
    private SASConfigModel sas;
    private String defaultWorkspaceServerName;
    private List<ServerConfigModel> servers;

    private ServerConfigModel getServerByName(String serverName) throws Exception {
        return servers.stream()
                .filter(server -> server.getName().matches(serverName))
                .findAny()
                .orElseThrow(() -> new Exception("Server with name '" + serverName + "' was not found in configuration"));
    }

    public ConnectionProperties getConnection() throws Exception {
        return getConnection(null, null);
    }

    public ConnectionProperties getConnection(String serverName, String userName) throws Exception {
        ServerConfigModel server;
        ServerConfigModel.UsersConfigModel user;
        if (serverName == null) {
            server = getServers().get(0);
        } else {
            server = getServerByName(serverName);
        }
        if (userName == null) {
            user = server.getUsers().get(0);
        } else {
            user = server.getUser(userName);
        }
        return new ConnectionProperties(
                server.getHost(),
                server.getPort(),
                user.getName(),
                user.getPassword(),
                user.getKey(),
                server.isApikeyEnabled(),
                server.isBasicAuthEnabled()
        );
    }
}
