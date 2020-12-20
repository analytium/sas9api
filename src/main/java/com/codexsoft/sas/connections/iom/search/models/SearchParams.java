package com.codexsoft.sas.connections.iom.search.models;

import com.codexsoft.sas.connections.iom.search.criterias.*;
import lombok.Data;
import lombok.val;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class SearchParams {
    private String[] locationFolderIds;
    private boolean locationRecursive;
    private String id;
    private String type;
    private String[] publicTypes;
    private String nameEquals;
    private String nameStarts;
    private String nameContains;
    private String nameRegex;
    private String descriptionContains;
    private String descriptionRegex;
    private LocalDateTime createdGt;
    private LocalDateTime createdLt;
    private LocalDateTime modifiedLt;
    private LocalDateTime modifiedGt;
    private boolean includeAssociations;
    private boolean includePermissions;
    private String tableLibref;
    private String tableDBMS;

    public List<ISearchCriteria> getSearchCriterias() throws Exception {
        val result = new ArrayList<ISearchCriteria>();
        if (id != null) {
            result.add(new IdSearchCriteria(id));
        }
        if (type != null) {
            result.add(new TypeSearchCriteria(type));
        }
        if (publicTypes != null) {
            result.add(new PublicTypeSearchCriteria(publicTypes));
        }
        if (nameEquals != null) {
            result.add(new NameEqualsCriteria(nameEquals));
        }
        if (nameStarts != null) {
            result.add(new NameStartsCriteria(nameStarts));
        }
        if (nameRegex != null) {
            result.add(new NameRegexSearchCriteria(nameRegex));
        }
        if (nameContains != null) {
            result.add(new NameContainsCriteria(nameContains));
        }
        if (descriptionContains != null) {
            result.add(new DescriptionContainsCriteria(descriptionContains));
        }
        if (descriptionRegex != null) {
            result.add(new DescriptionRegexSearchCriteria(descriptionRegex));
        }
        if (createdGt != null) {
            result.add(new CreatedDateGtSearchCriteria(createdGt));
        }
        if (createdLt != null) {
            result.add(new CreatedDateLtSearchCriteria(createdLt));
        }
        if (modifiedGt != null) {
            result.add(new ModifiedDateGtSearchCriteria(modifiedGt));
        }
        if (modifiedLt != null) {
            result.add(new ModifiedDateLtSearchCriteria(modifiedLt));
        }
        FolderIdsSearchCriteria folderIdsCriteria = null;
        if (locationFolderIds != null) {
            folderIdsCriteria = new FolderIdsSearchCriteria(type, publicTypes, locationFolderIds);
        }
        if (tableLibref != null || tableDBMS != null) {
            result.add(new TableSearchCriteria(folderIdsCriteria, type, publicTypes, tableLibref, tableDBMS));
        } else if (folderIdsCriteria != null) {
            result.add(folderIdsCriteria);
        }
        if (result.size() == 0) {
            throw new Exception("Should be at least one search criteria");
        }
        return result;
    }
}
