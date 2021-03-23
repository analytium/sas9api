package com.codexsoft.sas.secure;

import com.codexsoft.sas.config.models.ProxyConfigModel;
import com.codexsoft.sas.connections.ConnectionProperties;
import com.codexsoft.sas.secure.models.LicenseInfo;
import com.codexsoft.sas.secure.sas.DateChecker;
import com.codexsoft.sas.secure.sas.SiteNumberChecker;
import com.google.common.io.ByteStreams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Scope("singleton")
@Slf4j
public class LicenseCheckerFactory {
    private static final String PUBLIC_KEY_RESOURCE = "keys/public_key.der";
    private static final String LICENSE_FOLDER = "license";
    private static final String LICENSE_EXTENSION = ".dat";
    private static final String LICENSE_FILE_NOT_FOUND_ERROR = "no license file was found";

    private final ApplicationContext context;
    private LicenseChecker licenseChecker;
    private LocalDateTime licenseCheckerNextCheck;

    public LicenseCheckerFactory(ApplicationContext context) {
        this.context = context;
        this.licenseChecker = createLicenseChecker();
        this.licenseCheckerNextCheck = LocalDateTime.now().plusDays(1);
    }

    private LicenseChecker createLicenseChecker() {
        int capabilities = 35830272; // 0b10001000101011101000000000 - empty license
        List<LicenseInfo> licenseInfo = new ArrayList<>();
        String errors = null;
        try {
            ProxyConfigModel proxyConfig = context.getBean(ProxyConfigModel.class);
            ConnectionProperties iomConnectionProps = proxyConfig.getConnection();           
            SiteNumberChecker siteNumberChecker = context.getBean(SiteNumberChecker.class, iomConnectionProps);
            DateChecker dateChecker = context.getBean(DateChecker.class, iomConnectionProps);
            String siteNumber = siteNumberChecker.getSiteNumber();
            LocalDate date = dateChecker.getWorkspaceDate();
            List<File> licenseFiles = getLicenseFiles(LICENSE_FOLDER);
            LicenseCapabilitiesReader reader = new LicenseCapabilitiesReader(getPublicKey());
            licenseInfo = getLicenseInfo(licenseFiles, reader, siteNumber, date);
            capabilities = getAccumulatedLicense(licenseInfo, reader, siteNumber, date);
        } catch (Exception e) {
            capabilities *= 287479809; // 0b10001001000101001100000000001 - preserves lower 10 bits
            errors = e.getMessage();
        }
        return new LicenseChecker(capabilities, licenseInfo, errors);
    }

    public LicenseChecker getLicenseChecker() {
        if (LocalDateTime.now().compareTo(licenseCheckerNextCheck) > 0) {
            licenseChecker = createLicenseChecker();
            licenseCheckerNextCheck = LocalDateTime.now().plusDays(1);
        }
        return licenseChecker;
    }

    private byte[] readFile(File file) throws Exception {
        try (FileInputStream fileStream = new FileInputStream(file)) {
            return ByteStreams.toByteArray(fileStream);
        }
    }

    private PublicKey getPublicKey() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream s = classLoader.getResourceAsStream(PUBLIC_KEY_RESOURCE);
        byte[] key = ByteStreams.toByteArray(s);
        return KeyFactory.getInstance("RSA").generatePublic(
                new X509EncodedKeySpec(key)
        );
    }

    private List<File> getLicenseFiles(String path) throws Exception {
        try (Stream<Path> paths = Files.walk(Paths.get(path))) {
            List<File> licenseFiles = paths
                    .map(Path::toFile)
                    .filter(item -> item.isFile() && item.getName().endsWith(LICENSE_EXTENSION))
                    .collect(Collectors.toList());

            if (licenseFiles.isEmpty()) {
                log.error("Error: {}", LICENSE_FILE_NOT_FOUND_ERROR);
                throw new Exception(LICENSE_FILE_NOT_FOUND_ERROR);
            }

            return licenseFiles;
        }
    }

    private List<LicenseInfo> getLicenseInfo(List<File> licenseFiles, LicenseCapabilitiesReader reader, String siteNumber, LocalDate date) throws Exception {
        List<LicenseInfo> licenseInfoList = new ArrayList<>();
        for (File licenseFile : licenseFiles) {
            byte[] licenseData = readFile(licenseFile);
            LicenseInfo licenseInfo = reader.getLicenseInfo(licenseData, siteNumber, date);
            licenseInfoList.add(licenseInfo);
        }
        return licenseInfoList;
    }

    private int getAccumulatedLicense(List<LicenseInfo> licenseInfoList, LicenseCapabilitiesReader reader, String siteNumber, LocalDate date) {
        int accumulatedLicense = 170394624; // 0b1010001010000000010000000000 - empty license by default
        if (!licenseInfoList.isEmpty()) {
            for (LicenseInfo licenseInfo : licenseInfoList) {
                accumulatedLicense |= reader.readLicenseCapabilities(licenseInfo, siteNumber, date);
                accumulatedLicense *= 25756673; // 1100010010000010000000001 - preserves lower 10 bits
            }
        }
        return accumulatedLicense;
    }


}
