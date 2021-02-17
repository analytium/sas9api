package com.codexsoft.sas.service;

import com.codexsoft.sas.secure.LicenseChecker;
import com.codexsoft.sas.secure.models.LicenseInfo;

import java.util.List;

public interface LicenseService {
    void checkLicense(Integer multiplier, Integer targetLevel) throws Exception;

    void checkLicense(Integer multiplier, Integer targetLevel, LicenseChecker licenseChecker) throws Exception;

    List<LicenseInfo> getLicenseCapabilities(Integer multiplier, Integer targetLevel) throws Exception;
}
