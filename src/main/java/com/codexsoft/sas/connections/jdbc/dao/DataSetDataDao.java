package com.codexsoft.sas.connections.jdbc.dao;

import com.codexsoft.sas.config.models.DataSetDataConfigModel;
import com.codexsoft.sas.connections.jdbc.JDBCConnection;
import com.codexsoft.sas.connections.jdbc.utils.JDBCUtils;
import com.codexsoft.sas.connections.workspace.WorkspaceConnection;
import com.codexsoft.sas.utils.CustomRowProcessor;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.wrappers.StringTrimmedResultSet;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by eugene on 12.6.17.
 */
@Service(value = "DataSetDataDao")
@Scope("prototype")
public class DataSetDataDao {
    private final DataSetDataConfigModel dataSetDataConfigModel;
    private static final String OPTIONS_COMMAND = "options VALIDVARNAME = ANY VALIDMEMNAME = EXTEND;";

    private final JDBCConnection jdbcConnection;

    public DataSetDataDao(DataSetDataConfigModel dataSetDataConfigModel, JDBCConnection jdbcConnection) {
        this.dataSetDataConfigModel = dataSetDataConfigModel;
        this.jdbcConnection = jdbcConnection;
    }

    private Class checkTypeConsistency(List<Object> objects) throws Exception {
        Class firstObjectClass = objects.get(0).getClass();
        boolean match = objects.stream()
                .allMatch(object -> object.getClass().equals(firstObjectClass));

        if (!match) {
            throw new Exception("Not all key types match");
        }
        return firstObjectClass;
    }

    public List<Map<String, Object>> getData(
            @NotNull String libraryName,
            @NotNull String datasetName,
            Map<String, Object> filter,
            int limit,
            int offset
    ) throws Exception {
        WorkspaceConnection workspaceConnection = jdbcConnection.getWorkspaceConnection();
        workspaceConnection.submitSasCommand(OPTIONS_COMMAND, false);

        Connection connection = jdbcConnection.getConnection();

        String filterString = generateFilterString(filter);

        String query = String.format(
                "SELECT * FROM %s.%s %s",
                libraryName,
                datasetName,
                filterString
        );

        try (Statement statement = connection.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY
        )) {
            if (limit == 0) {
                limit = dataSetDataConfigModel.getDefaultFetchSize();
            }
            limit = Math.min(limit, dataSetDataConfigModel.getMaxFetchSize());

            statement.setFetchSize(limit);
            statement.setMaxRows(offset + limit);

            ResultSet resultSet = statement.executeQuery(query);
            resultSet = StringTrimmedResultSet.wrap(resultSet);

            resultSet.absolute(offset);

            return new MapListHandler(new CustomRowProcessor()).handle(resultSet);
        }
    }

    private String generateFilterString(Map<String, Object> filter) {
        StringBuilder filterString = new StringBuilder();
        if (filter != null && filter.size() > 0) {
            filterString.append("WHERE ");
            boolean flag[] = { false };
            filter.forEach((String column, Object value) -> {
                if (flag[0]) filterString.append(" AND ");
                filterString.append(JDBCUtils.quoteColumn(column));
                filterString.append(" = ");
                filterString.append(JDBCUtils.quoteValue(value));
                flag[0] = true;
            });
        }
        return filterString.toString();
    }

    public int deleteDataAll(
            @NotNull String libraryName,
            @NotNull String datasetName
    ) throws Exception {
        Connection connection = jdbcConnection.getConnection();

        String truncateQuery = String.format(
                "DELETE FROM %s.%s",
                libraryName,
                datasetName
        );

        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(truncateQuery);
        }
    }

    public int deleteData(
            @NotNull String libraryName,
            @NotNull String datasetName,
            @NotNull List<Map<String, Object>> data,
            @NotNull String byKey
    ) throws Exception {
        Connection connection = jdbcConnection.getConnection();

        List<Object> dataKeys = data.stream()
                .map(item -> item.get(byKey))
                .collect(Collectors.toList());

        Class dataKeyClass = checkTypeConsistency(dataKeys);

        String dataKeysString = dataKeys.stream()
                .map(object -> JDBCUtils.quoteValue(object, dataKeyClass))
                .collect(Collectors.joining(","));

        String deleteQuery = String.format(
                "DELETE FROM %s.%s WHERE %s IN (%s)",
                libraryName,
                datasetName,
                JDBCUtils.quoteColumn(byKey),
                dataKeysString
        );

        try (Statement statement = connection.createStatement()){
            return statement.executeUpdate(deleteQuery);
        }
    }

    public int insertData(
            @NotNull String libraryName,
            @NotNull String datasetName,
            @NotNull List<Map<String, Object>> data
    ) throws Exception {
        Connection connection = jdbcConnection.getConnection();

        String statements = generateInsertStatements(data);

        String insertQuery = String.format(
                "INSERT INTO %s.%s %s",
                libraryName,
                datasetName,
                statements
        );

        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(insertQuery);
        }
    }

    private String generateInsertStatements(@NotNull List<Map<String, Object>> data) {
        StringBuilder statements = new StringBuilder();
        boolean[] flag = { false };
        data.forEach(item -> {
            if (item.size() == 0) return;
            statements.append("\nSET ");
            flag[0] = false;
            item.forEach((key, value) -> {
                if (flag[0]) statements.append(", ");
                statements.append(JDBCUtils.quoteColumn(key));
                statements.append("=");
                statements.append(JDBCUtils.quoteValue(value));
                flag[0] = true;
            });
        });
        return statements.toString();
    }

    // TODO: make it atomic
    public int[] replaceData(
            @NotNull String libraryName,
            @NotNull String datasetName,
            @NotNull List<Map<String, Object>> data,
            String byKey
    ) throws Exception {
        int deletedCount = 0;
        if (byKey != null && byKey.length() > 0) {
            deletedCount = deleteData(libraryName, datasetName, data, byKey);
        }
        int insertedCount = insertData(libraryName, datasetName, data);
        return new int[] { insertedCount, deletedCount };
    }

    // TODO: make it atomic too
    public int[] replaceDataAll(
            @NotNull String libraryName,
            @NotNull String datasetName,
            @NotNull List<Map<String, Object>> data
    ) throws Exception {
        int deletedCount = deleteDataAll(libraryName, datasetName);
        int insertedCount = insertData(libraryName, datasetName, data);
        return new int[] { insertedCount, deletedCount };
    }
 }
