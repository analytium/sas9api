package com.codexsoft.sas.service;

import com.codexsoft.sas.secure.LicenseChecker;
import com.codexsoft.sas.secure.models.LicenseCapabilities;

import java.util.List;

public interface LicenseService {
    void checkLicense() throws Exception;

    void checkLicense(LicenseChecker licenseChecker) throws Exception;

    List<LicenseCapabilities> getLicenseCapabilities() throws Exception;
}
