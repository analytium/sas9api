package com.codexsoft.sas.connections.iom.search.criterias;

import com.codexsoft.sas.connections.iom.search.models.SASDetailedObject;
import com.google.re2j.Pattern;

public class DescriptionRegexSearchCriteria implements ISearchCriteria {
    private String searchPattern;
    private Pattern searchPatternCompiled;

    public DescriptionRegexSearchCriteria(String searchPattern) {
        this.searchPattern = searchPattern;
        this.searchPatternCompiled = Pattern.compile(searchPattern, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public int getType() {
        return CRITERIA_ATTRIBUTE;
    }

    @Override
    public String getXmlselectCriteria() {
        // always the same, the real check is done later in checkCriteria
        // the check is needed just to make sure at least one criteria is passed to XMLSELECT
        // in case no other criterias exist
        return "@Desc NE ''";
    }

    @Override
    public boolean checkCriteria(SASDetailedObject object) {
        String description = object.getAttribute("Desc");
        return searchPatternCompiled.matches(description);
    }
}
