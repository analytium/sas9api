package com.codexsoft.sas.connections.iom.search.criterias;

import com.codexsoft.sas.connections.iom.search.models.SASDetailedObject;

public class NameStartsCriteria implements ISearchCriteria {
    private String searchNameStart;

    public NameStartsCriteria(String searchNameStart) {
        this.searchNameStart = searchNameStart;
    }

    public int getPriority() { return 2; }

    public int getType() { return CRITERIA_ATTRIBUTE; }

    public String getXmlselectCriteria() {
        String template = "@Name =: '%s'";
        return String.format(template, searchNameStart);
    }

    public boolean checkCriteria(SASDetailedObject object) {
        return object.getName().toLowerCase().startsWith(searchNameStart.toLowerCase());
    }
}
