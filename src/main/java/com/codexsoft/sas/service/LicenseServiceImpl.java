package com.codexsoft.sas.service;

import com.codexsoft.sas.secure.LicenseChecker;
import com.codexsoft.sas.secure.LicenseCheckerFactory;
import com.codexsoft.sas.secure.models.LicenseInfo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class LicenseServiceImpl implements LicenseService {
    private final LicenseCheckerFactory licenseCheckerFactory;
    public static final String INVALID_LICENSE_ERROR_MESSAGE =
            "The endpoint is not available for use with current license";

    public LicenseServiceImpl(LicenseCheckerFactory licenseCheckerFactory) {
        this.licenseCheckerFactory = licenseCheckerFactory;
    }

    @Override
    public void checkLicense(Integer multiplier, Integer targetLevel) throws Exception {
        final LicenseChecker licenseChecker = licenseCheckerFactory.getLicenseChecker();
        checkLicense(multiplier, targetLevel, licenseChecker);
    }

    @Override
    public void checkLicense(Integer multiplier, Integer targetLevel, LicenseChecker licenseChecker) throws Exception {
        String licenseCheckerErrors = licenseChecker.getErrors();

        if (StringUtils.hasLength(licenseCheckerErrors))
            throw new Exception(licenseCheckerErrors);

        if (!licenseChecker.check(multiplier, targetLevel))
            throw new Exception(INVALID_LICENSE_ERROR_MESSAGE);
    }

    @Override
    public List<LicenseInfo> getLicenseCapabilities(Integer multiplier, Integer targetLevel) throws Exception {
        final LicenseChecker licenseChecker = licenseCheckerFactory.getLicenseChecker();
        checkLicense(multiplier, targetLevel, licenseChecker);
        return licenseChecker.getLicenseInfo();
    }
}
