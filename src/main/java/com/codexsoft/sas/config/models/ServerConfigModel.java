package com.codexsoft.sas.config.models;

import lombok.Data;

import java.util.List;

@Data
public class ServerConfigModel {
    private String name;
    private String host;
    private int port;
    private List<UsersConfigModel> users;
    private boolean apikeyEnabled;
    private boolean basicAuthEnabled;

    @Data
    public static class UsersConfigModel {
        private String name;
        private String password;
        private String key;
    }

    public UsersConfigModel getUser(String userName) throws Exception {
        return getUsers().stream()
                .filter(user -> user.getName().matches(userName))
                .findAny()
                .orElseThrow(() -> new Exception("User '" + userName + "' was not found in configuration of server '" + name));
    }
}
