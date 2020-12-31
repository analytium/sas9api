package com.codexsoft.sas.service;

import com.codexsoft.sas.secure.LicenseChecker;
import com.codexsoft.sas.secure.LicenseCheckerFactory;
import com.codexsoft.sas.secure.models.LicenseCapabilities;
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
    public void checkLicense() throws Exception {
        final LicenseChecker licenseChecker = licenseCheckerFactory.getLicenseChecker();
        checkLicense(licenseChecker);
    }

    @Override
    public void checkLicense(LicenseChecker licenseChecker) throws Exception {
        String licenseCheckerErrors = licenseChecker.getErrors();

        if (StringUtils.hasLength(licenseCheckerErrors))
            throw new Exception(licenseCheckerErrors);

        if (!licenseChecker.check(1147905, 1))
            throw new Exception(INVALID_LICENSE_ERROR_MESSAGE);
    }

    @Override
    public List<LicenseCapabilities> getLicenseCapabilities() throws Exception {
        final LicenseChecker licenseChecker = licenseCheckerFactory.getLicenseChecker();
        checkLicense(licenseChecker);
        return licenseChecker.getCapabilities();
    }
}
