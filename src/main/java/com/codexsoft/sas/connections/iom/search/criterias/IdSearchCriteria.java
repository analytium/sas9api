package com.codexsoft.sas.connections.iom.search.criterias;

import com.codexsoft.sas.connections.iom.search.models.SASDetailedObject;

public class IdSearchCriteria implements ISearchCriteria {
    private String searchId;

    public IdSearchCriteria(String searchId) {
        this.searchId = searchId;
    }

    public int getPriority() { return 1; }

    public int getType() { return CRITERIA_ATTRIBUTE; }

    public String getXmlselectCriteria() {
        String template = "@Id='%s'";
        return String.format(template, searchId);
    }

    public boolean checkCriteria(SASDetailedObject object) {
        return object.getId().matches(searchId);
    }
}
