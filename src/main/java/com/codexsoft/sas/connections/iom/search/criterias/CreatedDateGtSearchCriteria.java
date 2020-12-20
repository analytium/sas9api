package com.codexsoft.sas.connections.iom.search.criterias;

import com.codexsoft.sas.connections.iom.search.models.SASDetailedObject;
import com.codexsoft.sas.utils.DateTimeUtils;

import java.time.LocalDateTime;

public class CreatedDateGtSearchCriteria implements ISearchCriteria {
    private LocalDateTime searchDateStart;

    public CreatedDateGtSearchCriteria(LocalDateTime searchDateStart) {
        this.searchDateStart = searchDateStart;
    }

    public int getPriority() { return 2; }

    public int getType() { return CRITERIA_ATTRIBUTE; }

    public String getXmlselectCriteria() {
        String template = "@MetadataCreated GT '%d'";
        long sasDate = DateTimeUtils.toSasDate(searchDateStart);
        return String.format(template, sasDate);
    }

    public boolean checkCriteria(SASDetailedObject object) {
        String dateString = object.getAttribute("MetadataCreated");
        LocalDateTime date = DateTimeUtils.fromSasDateString(dateString);
        if (date == null) {
            double objDate = Double.parseDouble(dateString);
            date = DateTimeUtils.fromSasDate((long)objDate);
        }
        return date.compareTo(searchDateStart) >= 0;
    }
}
