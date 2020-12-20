package com.codexsoft.sas.connections.iom.search.criterias;

import com.codexsoft.sas.connections.iom.search.models.SASDetailedObject;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PublicTypeSearchCriteria implements ISearchCriteria {
    private List<String> searchPublicTypes;

    public PublicTypeSearchCriteria(String[] searchPublicTypes) {
        this.searchPublicTypes = Arrays.stream(searchPublicTypes).collect(Collectors.toList());
    }

    public int getPriority() { return 1; }

    public int getType() { return CRITERIA_ATTRIBUTE; }

    public String getXmlselectCriteria() {
        String template = "@PublicType='%s'";
        String result = searchPublicTypes.stream()
                .map((type) -> String.format(template, type))
                .collect(Collectors.joining(" OR "));
        return "(" + result + ")";
    }

    public boolean checkCriteria(SASDetailedObject object) {
        String publicType = object.getAttribute("PublicType");
        if (publicType == null) return false;
        return searchPublicTypes.stream()
                .anyMatch((searchPublicType) -> publicType.matches(searchPublicType));
    }
}
