package com.codexsoft.sas.connections.iom.search.criterias;

import com.codexsoft.sas.connections.iom.search.models.SASDetailedObject;

public class NameContainsCriteria implements ISearchCriteria {
    private String searchNamePart;

    public NameContainsCriteria(String searchNamePart) {
        this.searchNamePart = searchNamePart;
    }

    public int getPriority() { return 2; }

    public int getType() { return CRITERIA_ATTRIBUTE; }

    public String getXmlselectCriteria() {
        String template = "@Name CONTAINS '%s'";
        return String.format(template, searchNamePart);
    }

    public boolean checkCriteria(SASDetailedObject object) {
        return object.getName().toLowerCase().contains(searchNamePart.toLowerCase());
    }
}
