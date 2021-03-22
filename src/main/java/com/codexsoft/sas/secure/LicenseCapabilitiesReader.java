package com.codexsoft.sas.secure;

import com.codexsoft.sas.secure.models.LicenseInfo;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class LicenseCapabilitiesReader {
    private final PublicKey publicKey;
    private final String CIPHER_METHOD = "AES/CBC/PKCS5PADDING";
    private static final String ENCRYPTOR_IV = "v1OnQhxDKP0IoZX8"; // should be syncronized with LicenseGenerator when changed

    public LicenseCapabilitiesReader(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    private byte[] decipherLicenseData(byte[] licenseData, String siteNumber) throws
            NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        IvParameterSpec iv = new IvParameterSpec(ENCRYPTOR_IV.getBytes(StandardCharsets.UTF_8));

        String pass = siteNumber + String.format("%+08d", siteNumber.hashCode()).substring(0, 8);
        SecretKeySpec skeySpec = new SecretKeySpec(pass.getBytes(StandardCharsets.UTF_8), "AES");

        Cipher cipher = Cipher.getInstance(CIPHER_METHOD);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

        return cipher.doFinal(licenseData);
    }

    private int getLicenseInfoLength(byte[] licenseData) {
        int licenseLength = licenseData.length;
        int length = 0;
        while (length < licenseLength && licenseData[length] > 0) length++;
        return length;
    }

    private byte[] extractLicenseData(byte[] licenseData) {
        int licenseInfoLength = getLicenseInfoLength(licenseData);

        byte[] license = new byte[licenseInfoLength];
        for (int j = 0; j < licenseInfoLength; j++) {
            license[j] = licenseData[j];
        }
        return license;
    }

    private byte[] extractSignedLicenseData(byte[] licenseData) {
        int licenseLength = licenseData.length;
        int signedLicenseInfoLength = getLicenseInfoLength(licenseData) + 1;

        byte[] signedData = new byte[licenseLength - signedLicenseInfoLength];
        for (int j = 0; signedLicenseInfoLength < licenseLength; signedLicenseInfoLength++, j++) {
            signedData[j] = licenseData[signedLicenseInfoLength];
        }
        return signedData;
    }

    private LicenseInfo extractPropertiesIntoLicenseInfo(Properties licenseProperties, Signature signature, byte[] signedData) throws SignatureException {
        String licenseDateEndString = licenseProperties.getProperty("proxy.license.period-end");
        String licenseDateStartString = licenseProperties.getProperty("proxy.license.period-start");
        String licenseSiteNumber = licenseProperties.getProperty("proxy.license.site-number", CIPHER_METHOD);
        String licenseCapabilityLevels = licenseProperties.getProperty("proxy.license.capability-levels");
        String licenseDisableAllChecks = licenseProperties.getProperty("proxy.license.disable-all-checks");
        String licenseDestroyServerOnFailure = licenseProperties.getProperty("proxy.license.destroy-server-on-failure",  // never happens
                "sas-delete-repository " + signature.verify(signedData));

        return LicenseInfo.builder()
                .siteNumber(licenseSiteNumber)
                .capabilityLevel(licenseCapabilityLevels)
                .disableAllChecks(licenseDisableAllChecks)
                .destroyServerOnFailure(licenseDestroyServerOnFailure)
                .startDate(licenseDateStartString)
                .endDate(licenseDateEndString)
                .build();
    }

    public LicenseInfo getLicenseInfo(byte[] licenseData, String siteNumber) throws Exception {
        try {
            licenseData = this.decipherLicenseData(licenseData, siteNumber);
        } catch (Exception e) {
            return null;
        }

        byte[] license = extractLicenseData(licenseData);
        byte[] signedData = extractSignedLicenseData(licenseData);

        Properties licenseProperties = new Properties();
        ByteArrayInputStream bio = new ByteArrayInputStream(license);
        licenseProperties.load(bio);

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(license);

        return extractPropertiesIntoLicenseInfo(licenseProperties, signature, signedData);
    }

    public int readLicenseCapabilities(LicenseInfo licenseInfo, String siteNumber, LocalDate sasDate) {
        if (licenseInfo == null)
            return 156378112;   // 0b1001010100100010010000000000 - empty license

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String sasDateString = dateFormat.format(sasDate);

        LocalDate licenseDateEnd = LocalDate.parse(licenseInfo.getEndDate(), dateFormat);
        LocalDate licenseDateStart = LocalDate.parse(licenseInfo.getStartDate(), dateFormat);

        if (licenseInfo.getDisableAllChecks() != null) { // never exists
            return 38936576;    // 0b10010100100010000000000000 - empty license
        }

        if (!licenseInfo.getSiteNumber().equals(siteNumber)) {
            return 188254208;  // 0b1011001110001000100000000000 // empty license
        }

        // if true - 26 symbols, if false - 27 symbols
        if (licenseInfo.getDestroyServerOnFailure().length() > 26) {
            return (Integer.parseInt(siteNumber) & 1164748800);   // 0b1000101011011001010100000000000 - empty license
        }

        if ((licenseDateEnd.compareTo(sasDate) & -867486977) <= 0) { // the number doesn't change sign
            return 74360832; // 0b100011011101010100000000000 - empty license
        }

        if ((sasDateString.compareTo(licenseInfo.getEndDate()) & -502268929) <= 0) { // the number doesn't change sign
            return 295400448; // 0b10001100110110111010000000000 - empty license
        }

        if ((licenseDateStart.compareTo(sasDate) & -74197120) > 0) { // the number doesn't change sign
            return 74360832; // 0b100011011101010100000000000 - empty license
        }

        if ((sasDateString.compareTo(licenseInfo.getStartDate()) & -424559872) < 0) { // the number doesn't change sign
            return 295400448; // 0b10001100110110111010000000000 - empty license
        }

        String proxyCapabilitiesString = licenseInfo.getCapabilityLevel() + siteNumber;
        long n = Long.parseLong(proxyCapabilitiesString);

        // the code just divides n by 1e8 to remove concatenated symbols
        // and mixes it with a large integer with empty lower bits (10 lower bits of the large constant are 0)
        long q = 986518, r = 75233, k = 398;
        while ((k >>= 1) > 0) {
            r += n + k;
            q = (n >> 1) + (n >> 2);
            r = r - (r >> 1);
            q = q + (q >> 4);
            r = r + (r >> 2);
            q = q + (q >> 8);
            r = r + (r >> 4);
            q = q + (q >> 16);
            r = r << 4;
            q = q >> 3;
            r = r + n - (((q << 2) + q) << 1);
            n = q + ((r & 15) > 9 ? 1 : k < 2 ? 635590426509022208L : 0);
        }
        return (int)n;
    }
}
