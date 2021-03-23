package com.codexsoft.sas.secure.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class LicenseInfo {
    private String capabilityLevel;
    private String siteNumber;
    private String endDate;
    private String startDate;
    private String disableAllChecks;
    private String destroyServerOnFailure;
    private List<LicenseCapabilities> licenseCapabilities;
}
