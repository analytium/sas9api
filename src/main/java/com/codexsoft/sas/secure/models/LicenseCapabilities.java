package com.codexsoft.sas.secure.models;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
@AllArgsConstructor
public enum LicenseCapabilities {
    LEVEL1(1, "SAS Data Read Only", new EndpointDescription[] {
            new EndpointDescription("GET", "/sas/"),
            new EndpointDescription("GET", "/sas/license"),
            new EndpointDescription("GET", "/sas/servers"),
            new EndpointDescription("GET", "/sas/servers/{serverName}"),
            new EndpointDescription("GET", "/sas/servers/{serverName}/libraries"),
            new EndpointDescription("GET", "/sas/libraries"),
            new EndpointDescription("GET", "/sas/servers/{serverName}/libraries/{libraryName}"),
            new EndpointDescription("GET", "/sas/libraries/{libraryName}"),
            new EndpointDescription("GET", "/sas/servers/{serverName}/libraries/{libraryName}/datasets"),
            new EndpointDescription("GET", "/sas/libraries/{libraryName}/datasets"),
            new EndpointDescription("GET", "/sas/servers/{serverName}/libraries/{libraryName}/datasets/{datasetName}"),
            new EndpointDescription("GET", "/sas/libraries/{libraryName}/datasets/{datasetName}"),
            new EndpointDescription("GET", "/sas/servers/{serverName}/libraries/{libraryName}/datasets/{datasetName}/data"),
            new EndpointDescription("GET", "/sas/libraries/{libraryName}/datasets/{datasetName}/data"),
            new EndpointDescription("GET", "/sas/user"),
            new EndpointDescription("GET", "/sas/stp"),
            new EndpointDescription("GET", "/sas/stp/{serverName}")
    }),
    LEVEL2(2, "SAS Meta Read Only and SAS Data Modify", new EndpointDescription[] {
            new EndpointDescription("GET", "/sas/meta/users"),
            new EndpointDescription("GET", "/sas/meta/users/{userName}"),
            new EndpointDescription("GET", "/sas/meta/groups"),
            new EndpointDescription("GET", "/sas/meta/groups/{groupName}"),
            new EndpointDescription("GET", "/sas/meta/roles"),
            new EndpointDescription("GET", "/sas/meta/roles/{roleName}"),
            new EndpointDescription("PUT", "/sas/cmd"),
    		new EndpointDescription("PUT", "/sas/servers/{serverName}/cmd"),
            new EndpointDescription("PUT", "/sas/servers/{serverName}/libraries/{libraryName}/datasets/{datasetName}/data"),
            new EndpointDescription("PUT", "/sas/libraries/{libraryName}/datasets/{datasetName}/data"),
            new EndpointDescription("POST", "/sas/servers/{serverName}/libraries/{libraryName}/datasets/{datasetName}/data"),
            new EndpointDescription("POST", "/sas/libraries/{libraryName}/datasets/{datasetName}/data"),
            new EndpointDescription("DELETE", "/sas/servers/{serverName}/libraries/{libraryName}/datasets/{datasetName}/data"),
            new EndpointDescription("DELETE", "/sas/libraries/{libraryName}/datasets/{datasetName}/data"),
            new EndpointDescription("DELETE", "/sas/servers/{serverName}/libraries/{libraryName}"),
            new EndpointDescription("POST", "/sas/servers/{serverName}/libraries/{libraryName}")
    }),
    LEVEL3(3, "SAS Meta Search and Modify", new EndpointDescription[] {
    		new EndpointDescription("GET", "/sas/meta/search"),
            new EndpointDescription("POST", "/sas/meta/objects/move"),
            new EndpointDescription("POST", "/sas/meta/objects/delete"),
            new EndpointDescription("POST", "/sas/meta/objects/copy")
    });

    private int level;
    private String name;
    private EndpointDescription[] availableEndpoints;

    public static LicenseCapabilities byLevel(int level) {
        if (level < 1 || level > 3) return null;
        return LicenseCapabilities.values()[level - 1];
    }
}
