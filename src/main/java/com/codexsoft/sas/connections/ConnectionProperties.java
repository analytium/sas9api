package com.codexsoft.sas.connections;

import lombok.Builder;

import java.beans.ConstructorProperties;
import java.util.Objects;

@Builder
public class ConnectionProperties {
    private String host;
    private int port;
    private String userName;
    private String password;
    private String key;
    private boolean apikeyEnabled;

    @ConstructorProperties({"host", "port", "userName", "password", "key", "apikeyEnabled"})
    public ConnectionProperties(String host, int port, String userName, String password, String key, boolean apikeyEnabled) {
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.key = key;
        this.apikeyEnabled = apikeyEnabled;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getPassword() {
        return this.password;
    }

    public String getKey() {
        return key;
    }

    public boolean isApikeyEnabled() {
        return apikeyEnabled;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setApikeyEnabled(boolean apikeyEnabled) {
        this.apikeyEnabled = apikeyEnabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionProperties that = (ConnectionProperties) o;
        return port == that.port && apikeyEnabled == that.apikeyEnabled && Objects.equals(host, that.host) && Objects.equals(userName, that.userName) && Objects.equals(password, that.password) && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, userName, password, key, apikeyEnabled);
    }

    protected boolean canEqual(Object other) {
        return other instanceof ConnectionProperties;
    }

    public String toString() {
        return "com.codexsoft.sas.connections.ConnectionProperties(host=" + this.getHost() + ", port=" + this.getPort() + ", userName=" + this.getUserName() + ", password=" + this.getPassword() + ", key=" + this.getKey() + ", apikeyEnabled=" + this.isApikeyEnabled() + ")";
    }
}
