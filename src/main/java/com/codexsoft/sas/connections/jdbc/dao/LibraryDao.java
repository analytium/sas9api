package com.codexsoft.sas.connections.jdbc.dao;

import com.codexsoft.sas.connections.jdbc.JDBCConnection;
import com.codexsoft.sas.connections.jdbc.models.Library;
import com.codexsoft.sas.dao.BaseDao;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Service(value = "LibraryDao")
@Scope("prototype")
public class LibraryDao extends BaseDao {

    public List<Library> getLibraries(JDBCConnection jdbcConnection) throws Exception {
        Connection connection = jdbcConnection.getConnection();

        String queryString = ""
                + " SELECT DISTINCT libname, engine, path, level, readonly, sequential, temp"
                + " FROM SASHELP.VLIBNAM";

        try (Statement statement = connection.createStatement()) {
            ResultSet result = statement.executeQuery(queryString);

            return new ArrayList<Library>() {{
                while (result.next()) {
                    add(Library.fromResultSet(result));
                }
            }};
        }
    }

    public Library getLibraryByName(String libraryName, JDBCConnection jdbcConnection) throws Exception {
        List<Library> libraries = getLibraries(jdbcConnection);
        return libraries.stream()
                .filter((library) -> library.getLibname().equalsIgnoreCase(libraryName))
                .findFirst()
                .orElseThrow(() -> new Exception("Library '" + libraryName + "' was not found"));
    }
}