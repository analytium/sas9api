package com.codexsoft.sas.connections;

public class ConnectionProperties {
    private String host;
    private int port;
    private String userName;
    private String password;

    @java.beans.ConstructorProperties({"host", "port", "userName", "password"})
    public ConnectionProperties(String host, int port, String userName, String password) {
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
    }

    public static ConnectionPropertiesBuilder builder() {
        return new ConnectionPropertiesBuilder();
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

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ConnectionProperties)) return false;
        final ConnectionProperties other = (ConnectionProperties) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$host = this.getHost();
        final Object other$host = other.getHost();
        if (this$host == null ? other$host != null : !this$host.equals(other$host)) return false;
        if (this.getPort() != other.getPort()) return false;
        final Object this$userName = this.getUserName();
        final Object other$userName = other.getUserName();
        if (this$userName == null ? other$userName != null : !this$userName.equals(other$userName)) return false;
        final Object this$password = this.getPassword();
        final Object other$password = other.getPassword();
        if (this$password == null ? other$password != null : !this$password.equals(other$password)) return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $host = this.getHost();
        result = result * PRIME + ($host == null ? 43 : $host.hashCode());
        result = result * PRIME + this.getPort();
        final Object $userName = this.getUserName();
        result = result * PRIME + ($userName == null ? 43 : $userName.hashCode());
        final Object $password = this.getPassword();
        result = result * PRIME + ($password == null ? 43 : $password.hashCode());
        return result;
    }

    protected boolean canEqual(Object other) {
        return other instanceof ConnectionProperties;
    }

    public String toString() {
        return "com.codexsoft.sas.connections.ConnectionProperties(host=" + this.getHost() + ", port=" + this.getPort() + ", userName=" + this.getUserName() + ", password=" + this.getPassword() + ")";
    }

    public static class ConnectionPropertiesBuilder {
        private String host;
        private int port;
        private String userName;
        private String password;

        ConnectionPropertiesBuilder() {
        }

        public ConnectionProperties.ConnectionPropertiesBuilder host(String host) {
            this.host = host;
            return this;
        }

        public ConnectionProperties.ConnectionPropertiesBuilder port(int port) {
            this.port = port;
            return this;
        }

        public ConnectionProperties.ConnectionPropertiesBuilder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public ConnectionProperties.ConnectionPropertiesBuilder password(String password) {
            this.password = password;
            return this;
        }

        public ConnectionProperties build() {
            return new ConnectionProperties(host, port, userName, password);
        }

        public String toString() {
            return "com.codexsoft.sas.connections.ConnectionProperties.ConnectionPropertiesBuilder(host=" + this.host + ", port=" + this.port + ", userName=" + this.userName + ", password=" + this.password + ")";
        }
    }
}
