package com.codexsoft.sas.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "swagger.auth")
public class SwaggerAuthProperties {
    private boolean enabled=true;
    private Map<String, String> users = new HashMap<>();

    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public Map<String, String> getUsers() {
        return users;
    }
    public void setUsers(Map<String, String> users) {
        this.users = users;
    }
}
