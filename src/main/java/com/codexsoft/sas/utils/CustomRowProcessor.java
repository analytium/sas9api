package com.codexsoft.sas.utils;

import org.apache.commons.dbutils.BasicRowProcessor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by eugene on 12.6.17.
 */
public class CustomRowProcessor extends BasicRowProcessor {

    @Override
    public Map<String, Object> toMap(ResultSet rs) throws SQLException {
        Map<String, Object> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        ResultSetMetaData rsmd = rs.getMetaData();
        int cols = rsmd.getColumnCount();

        for(int i = 1; i <= cols; ++i) {
            String columnName = rsmd.getColumnName(i);
            result.put(columnName, rs.getObject(i));
        }

        return result;
    }
}
