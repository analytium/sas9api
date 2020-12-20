package com.codexsoft.sas.connections.iom.search.criterias;

import com.codexsoft.sas.connections.iom.search.models.SASDetailedObject;

public class NameEqualsCriteria implements ISearchCriteria {
    private String searchName;

    public NameEqualsCriteria(String searchName) {
        this.searchName = searchName;
    }

    public int getPriority() { return 2; }

    public int getType() { return CRITERIA_ATTRIBUTE; }

    public String getXmlselectCriteria() {
        String template = "@Name EQ '%s'";
        return String.format(template, searchName);
    }

    public boolean checkCriteria(SASDetailedObject object) {
        return object.getName().equalsIgnoreCase(searchName);
    }
}
