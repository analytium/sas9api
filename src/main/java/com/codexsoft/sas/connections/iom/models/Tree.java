package com.codexsoft.sas.connections.iom.models;

import lombok.Data;

import java.util.List;

@Data
public class Tree {
    private String id;
    private String name;
    private String description;
    private String publicType;
    private String treeType;
    private List<Tree> children;
}
