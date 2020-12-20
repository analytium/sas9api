package com.codexsoft.sas.connections.jdbc.utils;

public class JDBCUtils {
    public static final String QUOTE = "'";

    public static String quoteValue(Object value, Class valueClass) {
        if (value == null) return null;

        if (valueClass.equals(Integer.class) || valueClass.equals(Double.class)) {
            return value.toString();
        }

        return QUOTE + value.toString().replaceAll(QUOTE, QUOTE + QUOTE) + QUOTE;
    }

    public static String quoteValue(Object value) {
        if (value == null) return null;
        return quoteValue(value, value.getClass());
    }

    public static String quoteColumn(String columnName) {
        return quoteValue(columnName, String.class) + "n";
    }
}
