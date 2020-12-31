package com.codexsoft.sas.service;

import com.codexsoft.sas.secure.LicenseChecker;
import com.codexsoft.sas.secure.LicenseCheckerFactory;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static com.codexsoft.sas.service.LicenseServiceImpl.INVALID_LICENSE_ERROR_MESSAGE;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class LicenseServiceTest extends TestCase {

    private LicenseService licenseService;

    @MockBean
    private LicenseCheckerFactory licenseCheckerFactory;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private String accessDeniedErrorMessage = "Access Denied.";

    @Before
    public void setup() {
        licenseService = new LicenseServiceImpl(licenseCheckerFactory);
    }

    @Test
    public void checkLicense_valid_Test() throws Exception {
        when(licenseCheckerFactory.getLicenseChecker())
                .thenReturn(getLicenseCheckerWithValidLicense());

        licenseService.checkLicense();
    }

    @Test
    public void checkLicense_Invalid_Test() throws Exception {
        when(licenseCheckerFactory.getLicenseChecker())
                .thenReturn(getLicenseCheckerWithInvalidLicense());

        expectedException.expect(Exception.class);
        expectedException.expectMessage(INVALID_LICENSE_ERROR_MESSAGE);

        licenseService.checkLicense();
    }

    @Test
    public void checkLicense_AccessDenied_Test() throws Exception {
        when(licenseCheckerFactory.getLicenseChecker())
                .thenReturn(getLicenseCheckerWithAccessDeniedError());

        expectedException.expect(Exception.class);
        expectedException.expectMessage(accessDeniedErrorMessage);

        licenseService.checkLicense();
    }

    private LicenseChecker getLicenseCheckerWithValidLicense() {
        return LicenseChecker.builder()
                .capabilities(1).errors(null).build();
    }

    private LicenseChecker getLicenseCheckerWithInvalidLicense() {
        return LicenseChecker.builder()
                .capabilities(0).errors(null).build();
    }

    private LicenseChecker getLicenseCheckerWithAccessDeniedError() {
        return LicenseChecker.builder()
                .capabilities(0).errors(accessDeniedErrorMessage).build();
    }

}