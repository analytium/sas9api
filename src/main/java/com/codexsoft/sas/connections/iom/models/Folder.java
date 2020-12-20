package com.codexsoft.sas.connections.iom.models;

import lombok.Data;

import java.util.List;

@Data
public class Folder {
    private String id;
    private String name;
    private String location;
    private String description;
    private List<Permission> permissions;
    private List<IdentifyingMeta> folderContent;
}
