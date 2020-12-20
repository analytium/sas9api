package com.codexsoft.sas.connections.iom.models;

import lombok.Data;

import java.util.List;

@Data
public class Person {
    private String id;
    private String name;
    private String displayName;
    private int isHidden;
    private String publicType;
    private List<IdentityGroup> groups;
    private List<IdentityGroup> roles;
}
