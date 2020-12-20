package com.codexsoft.sas.connections.iom.search.criterias;

import com.codexsoft.sas.connections.iom.search.models.SASDetailedObject;

import java.util.Arrays;
import java.util.stream.Collectors;

public class TableSearchCriteria implements ISearchCriteria {
    private static final String[] tableObjectTypes = { "DataTable", "ExternalTable", "JoinTable", "PhysicalTable", "QueryTable", "RelationalTable", "SecuredTable", "TableCollection", "WorkTable" };
    private static final String[] tablePublicTypes = { "Table" };

    private FolderIdsSearchCriteria folderIdsSearchCriteria;
    private String searchLibref;
    private String searchDBMS;


    public TableSearchCriteria(
            FolderIdsSearchCriteria folderIdsSearchCriteria,
            String objectType,
            String[] publicTypes,
            String searchLibref,
            String searchDBMS
    ) throws Exception {
        this.folderIdsSearchCriteria = folderIdsSearchCriteria;
        this.searchLibref = searchLibref;
        this.searchDBMS = searchDBMS;

        if (objectType != null && !Arrays.stream(tableObjectTypes).anyMatch(type -> type.equalsIgnoreCase(objectType))) {
            throw new Exception("Object type should be subtype of DataTable for using libref or DBMS search options");
        }
        if (publicTypes != null) {
            boolean publicTypeMatch = Arrays.stream(tablePublicTypes).anyMatch(type -> {
                for (String publicType : publicTypes) {
                    if (publicType.equalsIgnoreCase(type)) return true;
                }
                return false;
            });
            if (!publicTypeMatch) {
                throw new Exception("PublicType should have at least one intersection with these public types: " + Arrays.stream(tablePublicTypes).collect(Collectors.joining(", ")));
            }
        }
    }

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    public int getType() {
        return CRITERIA_PATH;
    }

    @Override
    public String getXmlselectCriteria() {
        String foldersXmlselect = folderIdsSearchCriteria != null
                ? folderIdsSearchCriteria.getXmlselectCriteria()
                : "";

        if (searchLibref == null && searchDBMS == null) {
            return foldersXmlselect;
        }
        if (searchLibref != null && searchDBMS == null) {
            return String.format(
                    "%1$s[TablePackage/DeployedDataPackage/UsedByPackages/SASLibrary[@Libref='%2$s']] OR %1$s[TablePackage/SASLibrary[@Libref='%2$s']]",
                    foldersXmlselect,
                    searchLibref
            );
        }
        if (searchLibref == null && searchDBMS != null) {
            return String.format(
                    "%1$s[TablePackage/DeployedDataPackage/UsedByPackages/SASLibrary[@IsDBMSLibname='1' AND @Engine='%2$s']]",
                    foldersXmlselect,
                    searchDBMS
            );
        }
        // no need to search for direct Table <-> Library relation when searching for DBMS-only tables
        return String.format(
                "%1$s[TablePackage/DeployedDataPackage/UsedByPackages/SASLibrary[@IsDBMSLibname='1' AND @Engine='%2$s' AND @Libref='%3$s']]",
                foldersXmlselect,
                searchDBMS,
                searchLibref
        );
    }

    @Override
    public boolean checkCriteria(SASDetailedObject object) {
        // need to get deep associations in order to check the criteria, so just skipping it
        return true;
    }
}
