package com.codexsoft.sas.connections.jdbc.dao;

import com.codexsoft.sas.connections.jdbc.JDBCConnection;
import com.codexsoft.sas.connections.jdbc.models.DataSet;
import com.codexsoft.sas.dao.BaseDao;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


@Service(value = "DataSetDao")
@Scope("prototype")
public class DataSetDao extends BaseDao {

    public List<DataSet> getDatasets(@NotNull String libraryName, JDBCConnection jdbcConnection) throws Exception {
        Connection connection = jdbcConnection.getConnection();

        String queryString = ""
                + " SELECT *"
                + " FROM SASHELP.VTABLE"
                + " WHERE libname = ?";

        try (PreparedStatement statement = connection.prepareStatement(queryString)) {
            statement.setString(1, libraryName);

            ResultSet result = statement.executeQuery();

            return new ArrayList<DataSet>() {{
                while (result.next()) {
                    add(DataSet.fromResultSet(result));
                }
            }};
        }
    }

    public DataSet getDatasetByName(String libraryName, String datasetName, JDBCConnection jdbcConnection) throws Exception {
        List<DataSet> datasets = getDatasets(libraryName, jdbcConnection);

        return datasets.stream()
                .filter((dataset) -> dataset.getName().equalsIgnoreCase(datasetName))
                .findFirst()
                .orElseThrow(() -> new Exception("Dataset '" + datasetName + "' was not found"));
    }

    public int deleteDataset(String libraryName, String datasetName, JDBCConnection jdbcConnection) throws Exception {
        Connection connection = jdbcConnection.getConnection();

        String dropQuery = String.format(
                "DROP TABLE %s.%s",
                libraryName,
                datasetName
        );

        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(dropQuery);
        }
    }
}
