package com.codexsoft.sas.secure;

import com.codexsoft.sas.secure.models.LicenseInfo;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class LicenseCheckerFacade {
    private final LicenseCheckerFactory licenseCheckerFactory;
    public static final String INVALID_LICENSE_ERROR_MESSAGE =
            "The endpoint is not available for use with current license";

    public LicenseCheckerFacade(LicenseCheckerFactory licenseCheckerFactory) {
        this.licenseCheckerFactory = licenseCheckerFactory;
    }

    public void checkLicense(Integer multiplier, Integer targetLevel) throws Exception {
        final LicenseChecker licenseChecker = licenseCheckerFactory.getLicenseChecker();
        checkLicense(multiplier, targetLevel, licenseChecker);
    }

    public void checkLicense(Integer multiplier, Integer targetLevel, LicenseChecker licenseChecker) throws Exception {
        String licenseCheckerErrors = licenseChecker.getErrors();

        if (StringUtils.hasLength(licenseCheckerErrors))
            throw new Exception(licenseCheckerErrors);

        if (!licenseChecker.check(multiplier, targetLevel))
            throw new Exception(INVALID_LICENSE_ERROR_MESSAGE);
    }

    public List<LicenseInfo> getLicenseCapabilities(Integer multiplier, Integer targetLevel) throws Exception {
        final LicenseChecker licenseChecker = licenseCheckerFactory.getLicenseChecker();
        checkLicense(multiplier, targetLevel, licenseChecker);
        return licenseChecker.getLicenseInfo();
    }
}
