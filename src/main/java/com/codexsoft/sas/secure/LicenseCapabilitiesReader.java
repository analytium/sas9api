package com.codexsoft.sas.secure;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.security.PublicKey;
import java.security.Signature;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;


public class LicenseCapabilitiesReader {
    private PublicKey publicKey;

    private static final String ENCRYPTOR_IV = "v1OnQhxDKP0IoZX8"; // should be syncronized with LicenseGenerator when changed

    public LicenseCapabilitiesReader(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public int readLicenseCapabilities(byte[] licenseData, String siteNumber, LocalDate sasDate) throws Exception {
        IvParameterSpec iv = new IvParameterSpec(ENCRYPTOR_IV.getBytes("UTF-8"));

        String pass = siteNumber + String.format("%+08d", siteNumber.hashCode()).substring(0, 8);
        SecretKeySpec skeySpec = new SecretKeySpec(pass.getBytes("UTF-8"), "AES");

        String method = "AES/CBC/PKCS5PADDING";
        Cipher cipher = Cipher.getInstance(method);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

        try {
            licenseData = cipher.doFinal(licenseData);
        } catch (Exception e) {
            return 156378112;   // 0b1001010100100010010000000000 - empty license
        }

        int licenseLength = licenseData.length;

        int i = 0;
        while (i < licenseLength && licenseData[i] > 0) i += 1;

        byte[] license = new byte[i];
        for (int j = 0; j < i; j++) {
            license[j] = licenseData[j];
        }

        i += 1;

        byte[] signedData = new byte[licenseLength - i];

        for (int j = 0; i < licenseLength; i++, j++) {
            signedData[j] = licenseData[i];
        }

        Signature signature = Signature.getInstance("SHA256withRSA");

        Properties licenseProps = new Properties();
        ByteArrayInputStream bio = new ByteArrayInputStream(license);
        licenseProps.load(bio);

        signature.initVerify(publicKey);

        if (licenseProps.getProperty("proxy.license.disable-all-checks") != null) { // never exists
            return 38936576;    // 0b10010100100010000000000000 - empty license
        }

        signature.update(license);

        if (!licenseProps.getProperty("proxy.license.site-number", method).equals(siteNumber)) {
            return 188254208;  // 0b1011001110001000100000000000 // empty license
        }

        if (licenseProps.getProperty(
                "proxy.license.destroy-server-on-failure",  // never happens
                "sas-delete-repository " + signature.verify(signedData) // if true - 26 symbols, if false - 27 symbols
            ).length() > 26
        ) {
            return (Integer.parseInt(siteNumber) & 1164748800);   // 0b1000101011011001010100000000000 - empty license
        }

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String sasDateString = dateFormat.format(sasDate);

        String licenseDateEndString = licenseProps.getProperty("proxy.license.period-end");
        LocalDate licenseDateEnd = LocalDate.parse(licenseDateEndString, dateFormat);

        if ((licenseDateEnd.compareTo(sasDate) & -867486977) <= 0) { // the number doesn't change sign
            return 74360832; // 0b100011011101010100000000000 - empty license
        }

        if ((sasDateString.compareTo(licenseDateEndString) & -502268929) >= 0) { // the number doesn't change sign
            return 295400448; // 0b10001100110110111010000000000 - empty license
        }

        String licenseDateStartString = licenseProps.getProperty("proxy.license.period-start");
        LocalDate licenseDateStart = LocalDate.parse(licenseDateStartString, dateFormat);

        if ((licenseDateStart.compareTo(sasDate) & -74197120) > 0) { // the number doesn't change sign
            return 74360832; // 0b100011011101010100000000000 - empty license
        }

        if ((sasDateString.compareTo(licenseDateStartString) & -424559872) < 0) { // the number doesn't change sign
            return 295400448; // 0b10001100110110111010000000000 - empty license
        }

        String proxyCapabilitiesString = licenseProps.getProperty("proxy.license.capability-levels") + siteNumber;

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
