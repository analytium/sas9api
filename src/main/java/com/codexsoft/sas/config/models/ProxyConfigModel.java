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
    private boolean apikeyEnabled;
    private boolean basicAuthEnabled;
    private ConnectionProperties connectionProperties;

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
        if (connectionProperties == null)
            return getConnectionProperties(serverName, userName);

        ServerConfigModel server;
        server = getServerConfigModelByNameOrDefault(serverName);
        return new ConnectionProperties(
                server.getHost(),
                server.getPort(),
                connectionProperties.getUserName(),
                connectionProperties.getPassword(),
                connectionProperties.getKey(),
                apikeyEnabled,
                basicAuthEnabled);
    }

    private ConnectionProperties getConnectionProperties(String serverName, String userName) throws Exception {
        ServerConfigModel server;
        ServerConfigModel.UsersConfigModel user;
        server = getServerConfigModelByNameOrDefault(serverName);
        user = getUsersConfigModelByNameOrDefault(userName, server);

        return new ConnectionProperties(
            server.getHost(),
            server.getPort(),
            user.getName(),
            user.getPassword(),
            user.getKey(),
            apikeyEnabled,
            basicAuthEnabled);
    }

    private ServerConfigModel.UsersConfigModel getUsersConfigModelByNameOrDefault(String userName, ServerConfigModel server) throws Exception {
        ServerConfigModel.UsersConfigModel user;
        if (userName == null) {
            user = server.getUsers().get(0);
        } else {
            user = server.getUser(userName);
        }
        return user;
    }

    private ServerConfigModel getServerConfigModelByNameOrDefault(String serverName) throws Exception {
        ServerConfigModel server;
        if (serverName == null) {
            server = getServers().get(0);
        } else {
            server = getServerByName(serverName);
        }
        return server;
    }

    public void setConnection(ConnectionProperties connectionProperties) {
        this.connectionProperties = connectionProperties;
    }
}
