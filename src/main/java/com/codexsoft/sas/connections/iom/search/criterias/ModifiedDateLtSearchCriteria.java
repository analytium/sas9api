package com.codexsoft.sas.connections.iom.search.criterias;

import com.codexsoft.sas.connections.iom.search.models.SASDetailedObject;
import com.codexsoft.sas.utils.DateTimeUtils;

import java.time.LocalDateTime;

public class ModifiedDateLtSearchCriteria implements ISearchCriteria {
    private LocalDateTime searchDateEnd;

    public ModifiedDateLtSearchCriteria(LocalDateTime searchDateStart) {
        this.searchDateEnd = searchDateStart;
    }

    public int getPriority() { return 2; }

    public int getType() { return CRITERIA_ATTRIBUTE; }

    public String getXmlselectCriteria() {
        String template = "@MetadataUpdated GT '%d'";
        long sasDate = DateTimeUtils.toSasDate(searchDateEnd);
        return String.format(template, sasDate);
    }

    public boolean checkCriteria(SASDetailedObject object) {
        String dateString = object.getAttribute("MetadataUpdated");
        LocalDateTime date = DateTimeUtils.fromSasDateString(dateString);
        if (date == null) {
            double objDate = Double.parseDouble(dateString);
            date = DateTimeUtils.fromSasDate((long)objDate);
        }
        return date.compareTo(searchDateEnd) < 0;
    }
}
