package com.codexsoft.sas.connections.iom.search.criterias;

import com.codexsoft.sas.connections.iom.search.models.SASDetailedObject;

public class DescriptionContainsCriteria implements ISearchCriteria {
    private String searchDescriptionPart;

    public DescriptionContainsCriteria(String searchDescriptionPart) {
        this.searchDescriptionPart = searchDescriptionPart;
    }

    public int getPriority() { return 2; }

    public int getType() { return CRITERIA_ATTRIBUTE; }

    public String getXmlselectCriteria() {
        String template = "@Description CONTAINS '%s'";
        return String.format(template, searchDescriptionPart);
    }

    public boolean checkCriteria(SASDetailedObject object) {
        String description = object.getAttribute("Description");
        if (description == null) return false;
        return description.toLowerCase().contains(searchDescriptionPart.toLowerCase());
    }
}
