package com.codexsoft.sas.connections.iom.search.models;

import lombok.Data;

@Data
public class SASObject {
    protected String type;
    protected String id;
    protected String name;

    public String toXml() {
        return String.format("<%s Id=\"%s\" Name=\"%s\" />", type, id, name);
    }

    public String toObjRefXml() {
        return String.format("<%s ObjRef=\"%s\" />", type, id);
    }
}
