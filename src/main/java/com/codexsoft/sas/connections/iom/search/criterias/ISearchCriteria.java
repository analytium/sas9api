package com.codexsoft.sas.connections.iom.search.criterias;

import com.codexsoft.sas.connections.iom.search.models.SASDetailedObject;

public interface ISearchCriteria {
    int CRITERIA_PATH = 0;
    int CRITERIA_ATTRIBUTE = 1;
    int CRITERIA_TYPE = 2;

    int getPriority();
    int getType();
    String getXmlselectCriteria();
    boolean checkCriteria(SASDetailedObject object);
}
