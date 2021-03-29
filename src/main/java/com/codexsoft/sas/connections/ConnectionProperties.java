package com.codexsoft.sas.connections;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.beans.ConstructorProperties;

@Data
@EqualsAndHashCode
@ToString
public class ConnectionProperties {
    private String host;
    private int port;
    private String userName;
    private String password;
    private String key;
    private boolean apikeyEnabled;
    private boolean basicAuthEnabled;

    @ConstructorProperties({"host", "port", "userName", "password", "key", "apikeyEnabled", "basicAuthEnabled"})
    public ConnectionProperties(String host, int port, String userName, String password, String key, boolean apikeyEnabled, boolean basicAuthEnabled) {
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.key = key;
        this.apikeyEnabled = apikeyEnabled;
        this.basicAuthEnabled = basicAuthEnabled;
    }

    protected boolean canEqual(Object other) {
        return other instanceof ConnectionProperties;
    }
}
