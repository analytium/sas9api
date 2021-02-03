package com.codexsoft.sas.connections;

import lombok.Builder;

import java.util.Objects;

@Builder
public class ConnectionProperties {
    private String host;
    private int port;
    private String userName;
    private String password;
    private String key;

    @java.beans.ConstructorProperties({"host", "port", "userName", "password"})
    public ConnectionProperties(String host, int port, String userName, String password, String key) {
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.key = key;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionProperties that = (ConnectionProperties) o;
        return port == that.port && host.equals(that.host) && userName.equals(that.userName) && password.equals(that.password) && key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, userName, password, key);
    }

    protected boolean canEqual(Object other) {
        return other instanceof ConnectionProperties;
    }

    public String toString() {
        return "com.codexsoft.sas.connections.ConnectionProperties(host=" + this.getHost() + ", port=" + this.getPort() + ", userName=" + this.getUserName() + ", password=" + this.getPassword() + ", key=" + this.getKey()+ ")";
    }
}
