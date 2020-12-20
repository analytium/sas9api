package com.codexsoft.sas.connections.iom.search.criterias;

import com.codexsoft.sas.connections.iom.search.models.SASDetailedObject;

public class TypeSearchCriteria implements ISearchCriteria {
    private String searchType;

    public TypeSearchCriteria(String type) {
        this.searchType = type;
    }

    public int getPriority() { return 0; }

    public int getType() { return CRITERIA_TYPE; }

    public String getXmlselectCriteria() {
        return searchType;
    }

    public boolean checkCriteria(SASDetailedObject object) {
        return object.getType().matches(searchType);
    }
}
