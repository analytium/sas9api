package com.codexsoft.sas.service;

import com.codexsoft.sas.secure.LicenseChecker;
import com.codexsoft.sas.secure.models.LicenseCapabilities;

import java.util.List;

public interface LicenseService {
    void checkLicense(Integer multiplier, Integer targetLevel) throws Exception;

    void checkLicense(Integer multiplier, Integer targetLevel, LicenseChecker licenseChecker) throws Exception;

    List<LicenseCapabilities> getLicenseCapabilities(Integer multiplier, Integer targetLevel) throws Exception;
}
