package com.codexsoft.sas.connections.iom.search.criterias;

import com.codexsoft.sas.connections.iom.search.models.SASDetailedObject;

import java.util.Arrays;
import java.util.stream.Collectors;

public class FolderIdsSearchCriteria implements ISearchCriteria {
    private String[] searchFolderIds;
    private String objectType;
    private String[] publicTypes;


    public FolderIdsSearchCriteria(String objectType, String[] publicTypes, String[] searchFolderIds) {
        this.searchFolderIds = searchFolderIds;
        this.objectType = objectType;
        this.publicTypes = publicTypes;
    }

    public int getPriority() { return 2; }

    public int getType() { return CRITERIA_PATH; }

    public String getXmlselectCriteria() {
        // anything else here?
        String template = "[Trees/Tree[%s]]";
        if (objectType != null && objectType.equalsIgnoreCase("Tree") ||
            publicTypes != null && Arrays.stream(publicTypes).anyMatch(type -> type.equalsIgnoreCase("Folder")))
        {
            template = "[ParentTree/Tree[%s]]";
        }
        String ids = Arrays.stream(searchFolderIds)
            .map(folderId -> String.format("@Id='%s'", folderId))
            .collect(Collectors.joining(" OR "));
        return String.format(template, ids);
    }

    public boolean checkCriteria(SASDetailedObject object) {
        // could be checked in associations, but need to force assotiations flag - TBD
        return true;
    }
}
