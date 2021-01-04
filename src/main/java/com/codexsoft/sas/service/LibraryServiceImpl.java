package com.codexsoft.sas.service;

import com.codexsoft.sas.connections.ConnectionHelpers;
import com.codexsoft.sas.connections.ConnectionProperties;
import com.codexsoft.sas.connections.jdbc.JDBCConnection;
import com.codexsoft.sas.connections.jdbc.dao.LibraryDao;
import com.codexsoft.sas.connections.jdbc.models.Library;
import lombok.val;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
public class LibraryServiceImpl implements LibraryService {

    private final ConnectionHelpers connectionHelpers;
    private final LibraryDao libraryDao;

    public LibraryServiceImpl(ConnectionHelpers connectionHelpers, LibraryDao libraryDao) {
        this.connectionHelpers = connectionHelpers;
        this.libraryDao = libraryDao;
    }

    @Override
    public List<Library> getServerLibraries(JDBCConnection jdbcConnection) throws Exception {
        return libraryDao.getLibraries(jdbcConnection);
    }

    @Override
    public Library getLibrary(String libraryName, JDBCConnection jdbcConnection) throws Exception {
        return libraryDao.getLibraryByName(libraryName, jdbcConnection);
    }

    @Override
    public List<Library> getLibrariesByServerName(String serverName, String repositoryName, HttpServletRequest request) throws Exception {
        ConnectionProperties connectionProperties = connectionHelpers.getConnectionProperties(request);
        try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByServerName(
                connectionProperties, serverName, repositoryName)) {
            return getServerLibraries(jdbcConnection);
        }
    }

    @Override
    public List<Library> getLibrariesByHost(String serverUrl, String serverPort, HttpServletRequest request) throws Exception {
        ConnectionProperties connectionProperties = connectionHelpers.getConnectionProperties(request);
        try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByHost(
                connectionProperties, serverUrl, serverPort)) {
            return getServerLibraries(jdbcConnection);
        }
    }

    @Override
    public Library getLibraryByServerName(String serverName, String libraryName, String repositoryName,
                                          HttpServletRequest request) throws Exception {
        ConnectionProperties connectionProperties = connectionHelpers.getConnectionProperties(request);
        try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByServerName(
                connectionProperties, serverName, repositoryName
        )) {
            return getLibrary(libraryName, jdbcConnection);
        }
    }

    @Override
    public Library getLibraryByHost(String libraryName, String serverUrl, String serverPort, HttpServletRequest request) throws Exception {
        val connectionProperties = connectionHelpers.getConnectionProperties(request);
        try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByHost(
                connectionProperties, serverUrl, serverPort
        )) {
            return getLibrary(libraryName, jdbcConnection);
        }
    }

}
