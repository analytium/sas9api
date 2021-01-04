package com.codexsoft.sas.connections.jdbc.dao;

import com.codexsoft.sas.connections.jdbc.JDBCConnection;
import com.codexsoft.sas.connections.jdbc.models.DataSetColumn;
import com.codexsoft.sas.dao.BaseDao;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


@Service(value = "DataSetColumnDao")
@Scope("prototype")
public class DataSetColumnDao extends BaseDao {
    private JDBCConnection jdbcConnection;

    public DataSetColumnDao(JDBCConnection jdbcConnection) {
        this.jdbcConnection = jdbcConnection;
    }

    public List<DataSetColumn> getDatasetColumns(
            @NotNull String libraryName,
            @NotNull String datasetName
    ) throws Exception {
        Connection connection = jdbcConnection.getConnection();

        String queryString = ""
                + " SELECT *"
                + " FROM SASHELP.VCOLUMN"
                + " WHERE libname = ? AND memname = ?"
                + " ORDER BY varnum ASC";

        try (PreparedStatement statement = connection.prepareStatement(queryString)) {
            statement.setString(1, libraryName);
            statement.setString(2, datasetName);

            ResultSet result = statement.executeQuery();

            return new ArrayList<DataSetColumn>() {{
                while (result.next()) {
                    add(DataSetColumn.fromResultSet(result));
                }
            }};
        }
    }
}
