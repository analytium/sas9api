package com.codexsoft.sas.service;

import com.codexsoft.sas.connections.jdbc.JDBCConnection;
import com.codexsoft.sas.connections.jdbc.models.Library;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface LibraryService {

    Library getLibrary(String libraryName, JDBCConnection jdbcConnection) throws Exception;

    List<Library> getServerLibraries(JDBCConnection jdbcConnection) throws Exception;

    List<Library> getLibrariesByServerName(String serverName, String repositoryName, HttpServletRequest request) throws Exception;

    List<Library> getLibrariesByHost(String serverUrl, String serverPort, HttpServletRequest request) throws Exception;

    Library getLibraryByServerName(String serverName, String libraryName, String repositoryName, HttpServletRequest request) throws Exception;

    Library getLibraryByHost(String libraryName, String serverUrl, String serverPort, HttpServletRequest request) throws Exception;
}
