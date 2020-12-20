package com.codexsoft.sas.connections.iom.models;

import lombok.Data;

import java.util.List;

@Data
public class IdentityGroup {
    private String id;
    private String name;
    private String displayName;
    private String description;
    private String groupType;
    private String publicType;
    private List<IdentityGroup> groups;
    private List<IdentityGroup> roles;
    private List<Person> users;

}
