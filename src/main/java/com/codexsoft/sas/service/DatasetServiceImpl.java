package com.codexsoft.sas.service;

import com.codexsoft.sas.connections.ConnectionHelpers;
import com.codexsoft.sas.connections.ConnectionProperties;
import com.codexsoft.sas.connections.jdbc.JDBCConnection;
import com.codexsoft.sas.connections.jdbc.dao.DataSetColumnDao;
import com.codexsoft.sas.connections.jdbc.dao.DataSetDao;
import com.codexsoft.sas.connections.jdbc.dao.DataSetDataDao;
import com.codexsoft.sas.connections.jdbc.models.DMLResponse;
import com.codexsoft.sas.connections.jdbc.models.DataSet;
import com.codexsoft.sas.connections.jdbc.models.DataSetColumn;
import com.codexsoft.sas.connections.jdbc.models.Library;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Service
public class DatasetServiceImpl implements DatasetService {
    private final ConnectionHelpers connectionHelpers;
    private final LibraryService libraryService;
    private final DataSetDao dataSetDao;
    private final DataSetColumnDao dataSetColumnDao;
    private final DataSetDataDao dataSetDataDao;

    public DatasetServiceImpl(ConnectionHelpers connectionHelpers, LibraryService libraryService, DataSetDao dataSetDao, DataSetColumnDao dataSetColumnDao, DataSetDataDao dataSetDataDao) {
        this.connectionHelpers = connectionHelpers;
        this.libraryService = libraryService;
        this.dataSetDao = dataSetDao;
        this.dataSetColumnDao = dataSetColumnDao;
        this.dataSetDataDao = dataSetDataDao;
    }

    @Override
    public List<DataSet> getDatasetsByServerName(String serverName, String libraryName, String repositoryName, HttpServletRequest request) throws Exception {
        ConnectionProperties connectionProperties = connectionHelpers.getConnectionProperties(request);
        try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByServerName(
                connectionProperties, serverName, repositoryName)) {
            return getDatasets(jdbcConnection, libraryName);
        }
    }

    @Override
    public List<DataSet> getDatasetsByHost(String libraryName, String serverUrl, String serverPort, HttpServletRequest request) throws Exception {
        ConnectionProperties connectionProperties = connectionHelpers.getConnectionProperties(request);
        try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByHost(
                connectionProperties, serverUrl, serverPort
        )) {
            return getDatasets(jdbcConnection, libraryName);
        }
    }

    @Override
    public DataSet getDatasetDetailsByServerName(String serverName, String libraryName, String datasetName, String repositoryName, HttpServletRequest request) throws Exception {
        ConnectionProperties connectionProperties = connectionHelpers.getConnectionProperties(request);
        try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByServerName(
                connectionProperties, serverName, repositoryName
        )) {
            return getDatasetDetails(jdbcConnection, libraryName, datasetName);
        }
    }

    @Override
    public DataSet getDatasetDetailsByHost(String libraryName, String datasetName, String serverUrl, String serverPort, HttpServletRequest request) throws Exception {
        ConnectionProperties connectionProperties = connectionHelpers.getConnectionProperties(request);
        try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByHost(
                connectionProperties, serverUrl, serverPort
        )) {
            return getDatasetDetails(jdbcConnection, libraryName, datasetName);
        }
    }

    @Override
    public List<Map<String, Object>> getDatasetDataByServerName(String serverName, String libraryName, String datasetName, String repositoryName, int limit, int offset, String filterJson, HttpServletRequest request) throws Exception {
        ConnectionProperties connectionProperties = connectionHelpers.getConnectionProperties(request);
        try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByServerName(
                connectionProperties, serverName, repositoryName
        )) {
            return getDatasetData(jdbcConnection, libraryName, datasetName, filterJson, limit, offset);
        }
    }

    @Override
    public List<Map<String, Object>> getDatasetDataByHost(String libraryName, String datasetName, String serverUrl, String serverPort, int limit, int offset, String filterJson, HttpServletRequest request) throws Exception {
        ConnectionProperties connectionProperties = connectionHelpers.getConnectionProperties(request);
        try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByHost(
                connectionProperties, serverUrl, serverPort
        )) {
            return getDatasetData(jdbcConnection, libraryName, datasetName, filterJson, limit, offset);
        }
    }

    @Override
    public DMLResponse putDataSetDataByServerName(String serverName, String libraryName, String datasetName, String repositoryName, String byKey, List<Map<String, Object>> data, HttpServletRequest request) throws Exception {
        ConnectionProperties connectionProperties = connectionHelpers.getConnectionProperties(request);
        try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByServerName(
                connectionProperties, serverName, repositoryName
        )) {
            return putDataSetData(jdbcConnection, libraryName, datasetName, data, byKey);
        }
    }

    @Override
    public DMLResponse putDataSetDataByHost(String libraryName, String datasetName, String serverUrl, String serverPort, String byKey, List<Map<String, Object>> data, HttpServletRequest request) throws Exception {
        ConnectionProperties connectionProperties = connectionHelpers.getConnectionProperties(request);
        try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByHost(
                connectionProperties, serverUrl, serverPort
        )) {
            return putDataSetData(jdbcConnection, libraryName, datasetName, data, byKey);
        }
    }

    @Override
    public DMLResponse postDataSetDataByHost(String libraryName, String datasetName, String serverUrl, String serverPort, List<Map<String, Object>> data, HttpServletRequest request) throws Exception {
        ConnectionProperties connectionProperties = connectionHelpers.getConnectionProperties(request);
        try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByHost(
                connectionProperties, serverUrl, serverPort
        )) {
            return postDataSetData(jdbcConnection, libraryName, datasetName, data);
        }
    }

    @Override
    public DMLResponse postDataSetDataByServerName(String serverName, String libraryName, String datasetName, String repositoryName, List<Map<String, Object>> data, HttpServletRequest request) throws Exception {
        ConnectionProperties connectionProperties = connectionHelpers.getConnectionProperties(request);
        try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByServerName(
                connectionProperties, serverName, repositoryName
        )) {
            return postDataSetData(jdbcConnection, libraryName, datasetName, data);
        }
    }

    @Override
    public DMLResponse deleteDatasetByHost(String libraryName, String datasetName, String serverUrl, String serverPort, HttpServletRequest request) throws Exception {
        ConnectionProperties connectionProperties = connectionHelpers.getConnectionProperties(request);
        try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByHost(
                connectionProperties, serverUrl, serverPort
        )) {
            return deleteDataset(jdbcConnection, libraryName, datasetName);
        }
    }

    @Override
    public DMLResponse deleteDatasetByServerName(String serverName, String libraryName, String datasetName, String repositoryName, HttpServletRequest request) throws Exception {
        ConnectionProperties connectionProperties = connectionHelpers.getConnectionProperties(request);
        try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByServerName(
                connectionProperties, serverName, repositoryName
        )) {
            return deleteDataset(jdbcConnection, libraryName, datasetName);
        }
    }

    private List<DataSet> getDatasets(JDBCConnection jdbcConnection, String libraryName) throws Exception {
        Library library = libraryService.getLibrary(libraryName, jdbcConnection);
        return dataSetDao.getDatasets(library.getLibname(), jdbcConnection);
    }

    private DataSet getDatasetDetails(JDBCConnection jdbcConnection, String libraryName, String datasetName) throws Exception {
        Library library = libraryService.getLibrary(libraryName, jdbcConnection);
        DataSet dataset = dataSetDao.getDatasetByName(library.getLibname(), datasetName, jdbcConnection);
        List<DataSetColumn> columns = dataSetColumnDao.getDatasetColumns(library.getLibname(), dataset.getName(), jdbcConnection);
        dataset.setColumns(columns);
        return dataset;
    }

    private List<Map<String, Object>> getDatasetData(JDBCConnection jdbcConnection, String libraryName, String datasetName, String filterJson, int limit, int offset) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> filter;
        if (filterJson != null && filterJson.length() > 0) {
            filter = mapper.readValue(filterJson, Map.class);
        } else {
            filter = null;
        }
        Map<String, Object> finalFilter = filter;
        return dataSetDataDao.getData(libraryName, datasetName, finalFilter, limit, offset, jdbcConnection);
    }

    private DMLResponse putDataSetData(JDBCConnection jdbcConnection, String libraryName, String datasetName, List<Map<String, Object>> data, String byKey) throws Exception {
        int[] recordsAffected = dataSetDataDao.replaceData(libraryName, datasetName, data, byKey, jdbcConnection);
        return DMLResponse.builder()
                .itemsInserted(recordsAffected[0])
                .itemsRemoved(recordsAffected[1])
                .itemsUpdated(0)
                .build();
    }

    private DMLResponse postDataSetData(JDBCConnection jdbcConnection, String libraryName, String datasetName, List<Map<String, Object>> data) throws Exception {
        int[] recordsAffected = dataSetDataDao.replaceDataAll(libraryName, datasetName, data, jdbcConnection);
        return DMLResponse.builder()
                .itemsInserted(recordsAffected[0])
                .itemsRemoved(recordsAffected[1])
                .itemsUpdated(0)
                .build();
    }

    private DMLResponse deleteDataset(JDBCConnection jdbcConnection, String libraryName, String datasetName) throws Exception {
        dataSetDao.deleteDataset(libraryName, datasetName, jdbcConnection);
        return DMLResponse.builder()
                .itemsInserted(0)
                .itemsRemoved(1)
                .itemsUpdated(0)
                .build();
    }
}
