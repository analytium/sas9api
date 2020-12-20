package com.codexsoft.sas.secure;

import com.codexsoft.sas.config.models.ProxyConfigModel;
import com.codexsoft.sas.connections.ConnectionProperties;
import com.codexsoft.sas.secure.sas.DateChecker;
import com.codexsoft.sas.secure.sas.SiteNumberChecker;
import com.google.common.io.ByteStreams;
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
import java.util.stream.Stream;

@Component
@Scope("singleton")
public class LicenseCheckerFactory {
    private static final String PUBLIC_KEY_RESOURCE = "keys/public_key.der";
    private static final String LICENSE_FOLDER = "license";
    private static final String LICENSE_EXTENSION = ".dat";

    private ApplicationContext context;
    private LicenseChecker licenseChecker;
    private LocalDateTime licenseCheckerNextCheck;

    public LicenseCheckerFactory(ApplicationContext context) throws Exception {
        this.context = context;
        this.licenseChecker = createLicenseChecker();
        this.licenseCheckerNextCheck = LocalDateTime.now().plusDays(1);
    }

    private LicenseChecker createLicenseChecker() {
        int capabilities = 35830272; // 0b10001000101011101000000000 - empty license
        try {
            ProxyConfigModel proxyConfig = context.getBean(ProxyConfigModel.class);
            ConnectionProperties iomConnectionProps = proxyConfig.getConnection();           
            SiteNumberChecker siteNumberChecker = context.getBean(SiteNumberChecker.class, iomConnectionProps);
            DateChecker dateChecker = context.getBean(DateChecker.class, iomConnectionProps);
            String siteNumber = siteNumberChecker.getSiteNumber();
            LocalDate date = dateChecker.getWorkspaceDate();
            List<File> licenseFiles = getLicenseFiles(LICENSE_FOLDER);
            LicenseCapabilitiesReader reader = new LicenseCapabilitiesReader(getPublicKey());
            capabilities = getAccumulatedLicense(licenseFiles, reader, siteNumber, date);     
        } catch (Exception e) {
            capabilities *= 287479809; // 0b10001001000101001100000000001 - preserves lower 10 bits
        }
        return new LicenseChecker(capabilities);
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
            List<File> licenseFiles = new ArrayList<File>();
            paths.forEach(file -> {
                File item = file.toFile();
                if (item.isFile() && item.getName().endsWith(LICENSE_EXTENSION)) {
                    licenseFiles.add(item);
                }
            });
            return licenseFiles;
        }
    }

    private int getAccumulatedLicense(
            List<File> licenseFiles,
            LicenseCapabilitiesReader reader,
            String siteNumber,
            LocalDate date
    ) throws Exception {
        int accumulatedLicense = 170394624; // 0b1010001010000000010000000000 - empty license by default
        for (File licenseFile : licenseFiles) {
            byte[] licenseData = readFile(licenseFile);
            accumulatedLicense |= reader.readLicenseCapabilities(licenseData, siteNumber, date);
            accumulatedLicense *= 25756673; // 1100010010000010000000001 - preserves lower 10 bits
        }
        return accumulatedLicense;
    }


}
