package com.codexsoft.sas.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class DateTimeUtils {
    // http://support.sas.com/documentation/cdl/en/jdbcref/63713/HTML/default/viewer.htm#n13rompfut4cqin1b9h50fwzzh7f.htm
    private static final LocalDateTime origin = LocalDateTime.of(1960, 1, 1, 0, 0, 0);
    private static final DateTimeFormatter sasDateFormatter = DateTimeFormatter.ofPattern("ddMMMyyyy:HH:mm:ss[.S]", new Locale("en"));

    public static LocalDateTime fromSasDate(long sasDate) {
        return origin.plus(Duration.ofSeconds(sasDate));
    }

    public static long toSasDate(LocalDateTime javaDate) {
        return ChronoUnit.SECONDS.between(origin, javaDate);
    }

    public static LocalDateTime fromSasDateString(String sasDate) {
        return LocalDateTime.parse(sasDate, sasDateFormatter);
    }

    public static String toSasStringDate(LocalDateTime javaDate) {
        return javaDate.format(sasDateFormatter);
    }
}
