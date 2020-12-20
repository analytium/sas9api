package com.codexsoft.sas.controllers;

import com.codexsoft.sas.config.models.ProxyConfigModel;
import com.codexsoft.sas.connections.ConnectionHelpers;
import com.codexsoft.sas.connections.ConnectionProperties;
import com.codexsoft.sas.connections.iom.IOMConnection;
import com.codexsoft.sas.connections.iom.dao.*;
import com.codexsoft.sas.connections.iom.models.*;
import com.codexsoft.sas.connections.iom.operations.ObjectOperationsDao;
import com.codexsoft.sas.connections.iom.search.SearchDao;
import com.codexsoft.sas.connections.iom.search.models.SASDetailedObject;
import com.codexsoft.sas.connections.iom.search.models.SearchParams;
import com.codexsoft.sas.connections.jdbc.JDBCConnection;
import com.codexsoft.sas.connections.jdbc.dao.DataSetColumnDao;
import com.codexsoft.sas.connections.jdbc.dao.DataSetDao;
import com.codexsoft.sas.connections.jdbc.dao.DataSetDataDao;
import com.codexsoft.sas.connections.jdbc.dao.LibraryDao;
import com.codexsoft.sas.connections.jdbc.models.DMLResponse;
import com.codexsoft.sas.connections.jdbc.models.DataSet;
import com.codexsoft.sas.connections.jdbc.models.DataSetColumn;
import com.codexsoft.sas.connections.jdbc.models.Library;
import com.codexsoft.sas.connections.workspace.WorkspaceConnection;
import com.codexsoft.sas.connections.workspace.models.SASLanguageResponse;
import com.codexsoft.sas.models.APIResponse;
import com.codexsoft.sas.models.LibraryParams;
import com.codexsoft.sas.models.ServerConfiguration;
import com.codexsoft.sas.secure.LicenseCheckerFactory;
import com.codexsoft.sas.secure.models.LicenseCapabilities;
import com.codexsoft.sas.utils.ResponseUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/sas")
@Api(value = "SAS", description = "Endpoint for SAS server")
public class SASRestController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ConnectionHelpers connectionHelpers;

    private ConnectionProperties getConnectionProperties(HttpServletRequest request) throws Exception {
        ProxyConfigModel proxyConfig = context.getBean(ProxyConfigModel.class);
        String serverName = request.getParameter("serverName");
        String userName = request.getParameter("userName");
        return proxyConfig.getConnection(serverName, userName);
    }

    // TODO: list all configured servers instead of passed one
    @ApiOperation(value = "Gets current server connection configuration and its repositories")
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ResponseEntity<APIResponse<ServerConfiguration>> sasMetadataInfo(
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(1147905, 1)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (val connection = context.getBean(IOMConnection.class, connectionProperties)) {
                val repositoriesDao = context.getBean(RepositoriesDao.class, connection);
                return ServerConfiguration.builder()
                        .metadataHost(connectionProperties.getHost())
                        .metadataPort(connectionProperties.getPort())
                        .metadataServerName("Test")     // TODO: remove test
                        .repositories(repositoriesDao.getRepositories())
                        .build();
            }
        });
    }

    @ApiOperation(value = "Gets list of available Workspace servers and their connections from metadata server")
    @RequestMapping(value = "/servers", method = RequestMethod.GET)
    public ResponseEntity<APIResponse<List<ServerComponent>>> getWorkspaceServers(
            @ApiParam(value = "Repository name", defaultValue = "Foundation")
            @RequestParam(value = "repositoryName", required = false) String repositoryName,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(201729, 1)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (val connection = context.getBean(IOMConnection.class, connectionProperties)) {
                val repositoriesDao = context.getBean(RepositoriesDao.class, connection);
                Repository repository = repositoriesDao.getRepositoryByName(repositoryName);
                val serversDao = context.getBean(ServersDao.class, connection);
                return serversDao.getWorkspaceServers(repository.getId());
            }
        });
    }

    @ApiOperation(value = "Gets Workspace server information and its connections from metadata server by name")
    @RequestMapping(value = "/servers/{serverName}", method = RequestMethod.GET)
    public ResponseEntity<APIResponse<ServerComponent>> getServer(
            @ApiParam(value = "Server name")
            @PathVariable String serverName,

            @ApiParam(value = "Repository name", defaultValue = "Foundation")
            @RequestParam(value = "repositoryName", required = false) String repositoryName,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(201729, 1)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (val connection = context.getBean(IOMConnection.class, connectionProperties)) {
                val repositoriesDao = context.getBean(RepositoriesDao.class, connection);
                Repository repository = repositoriesDao.getRepositoryByName(repositoryName);
                val serversDao = context.getBean(ServersDao.class, connection);
                return serversDao.getWorkspaceServersByName(repository.getId(), serverName);
            }
        });
    }

    //  Libraries endpoints

    private List<Library> getServerLibraries(JDBCConnection jdbcConnection) throws Exception {
        LibraryDao libraryDao = context.getBean(LibraryDao.class, jdbcConnection);
        return libraryDao.getLibraries();
    }

    @ApiOperation(
            value = "Gets list of available libraries for workspace server by server name",
            notes = "Uses first available connection from connection list for supplied server name. Note: the method doesn't populate ID field in Libraries object"
    )
    @RequestMapping(value = "/servers/{serverName}/libraries", method = RequestMethod.GET)
    public ResponseEntity<APIResponse<List<Library>>> getLibrariesByServer(
            @ApiParam(value = "Server name")
            @PathVariable String serverName,

            @ApiParam(value = "Repository name", defaultValue = "Foundation")
            @RequestParam(value = "repositoryName", required = false) String repositoryName,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(201729, 1)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByServerName(
                    connectionProperties, serverName, repositoryName
            )) {
                return getServerLibraries(jdbcConnection);
            }
        });
    }

    @ApiOperation(
            value = "Get list of libraries for workspace server using direct connection information",
            notes = "Note: the method doesn't populate ID field in Libraries object"
    )
    @RequestMapping(value = "/libraries", method = RequestMethod.GET)
    public ResponseEntity<APIResponse<List<Library>>> getLibraries(
            @ApiParam(value = "Workspace server host URL")
            @RequestParam(value = "serverUrl", required = false) String serverUrl,

            @ApiParam(value = "Workspace server port")
            @RequestParam(value = "serverPort", required = false) String serverPort,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(201729, 1)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByHost(
                    connectionProperties, serverUrl, serverPort
            )) {
                return getServerLibraries(jdbcConnection);
            }
        });
    }

    //  Library endpoints

    private Library getLibrary(JDBCConnection jdbcConnection, String libraryName) throws Exception {
        LibraryDao libraryDao = context.getBean(LibraryDao.class, jdbcConnection);
        return libraryDao.getLibraryByName(libraryName);
    }

    @ApiOperation(
            value = "Gets library information for workspace server by library name and server name",
            notes = "Uses first available connection from connection list for supplied server name"
    )
    @RequestMapping(value = "/servers/{serverName}/libraries/{libraryName}", method = RequestMethod.GET)
    public ResponseEntity<APIResponse<Library>> getLibraryByServer(
            @ApiParam(value = "Server name")
            @PathVariable String serverName,

            @ApiParam(value = "Library name")
            @PathVariable String libraryName,

            @ApiParam(value = "Repository name", defaultValue = "Foundation")
            @RequestParam(value = "repositoryName", required = false) String repositoryName,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(201729, 1)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByServerName(
                    connectionProperties, serverName, repositoryName
            )) {
                return getLibrary(jdbcConnection, libraryName);
            }
        });
    }

    @ApiOperation(value = "Gets library information for workspace server by library name using direct connection information")
    @RequestMapping(value = "/libraries/{libraryName}", method = RequestMethod.GET)
    public ResponseEntity<APIResponse<Library>> getLibrary(
            @ApiParam(value = "Library name")
            @PathVariable String libraryName,

            @ApiParam(value = "Workspace server host URL")
            @RequestParam(value = "serverUrl", required = false) String serverUrl,

            @ApiParam(value = "Workspace server port")
            @RequestParam(value = "serverPort", required = false) String serverPort,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(201729, 1)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByHost(
                    connectionProperties, serverUrl, serverPort
            )) {
                return getLibrary(jdbcConnection, libraryName);
            }
        });
    }

    // Datasets endpoints

    private List<DataSet> getDatasets(JDBCConnection jdbcConnection, String libraryName) throws Exception {
        Library library = getLibrary(jdbcConnection, libraryName);

        DataSetDao dataSetDao = context.getBean(DataSetDao.class, jdbcConnection);
        return dataSetDao.getDatasets(library.getLibname());
    }

    @ApiOperation(
            value = "Gets list of datasets for specific library by library name and server name",
            notes = "Uses first available connection from connection list for supplied server name. "
                  + "This method doesn't populate 'columns' field in response items. Use a method for specific dataset to get it populated."
    )
    @RequestMapping(value = "/servers/{serverName}/libraries/{libraryName}/datasets", method = RequestMethod.GET)
    public ResponseEntity<APIResponse<List<DataSet>>> getDatasetsByServer(
            @ApiParam(value = "Server name")
            @PathVariable String serverName,

            @ApiParam(value = "Library name")
            @PathVariable String libraryName,

            @ApiParam(value = "Repository name", defaultValue = "Foundation")
            @RequestParam(value = "repositoryName", required = false) String repositoryName,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(201729, 1)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByServerName(
                    connectionProperties, serverName, repositoryName
            )) {
                return getDatasets(jdbcConnection, libraryName);
            }
        });
    }

    @ApiOperation(value = "Gets list of datasets for specific library by library name using direct connection information")
    @RequestMapping(value = "/libraries/{libraryName}/datasets", method = RequestMethod.GET)
    public ResponseEntity<APIResponse<List<DataSet>>> getDatasets(
            @ApiParam(value = "Library name")
            @PathVariable String libraryName,

            @ApiParam(value = "Workspace server host URL")
            @RequestParam(value = "serverUrl", required = false) String serverUrl,

            @ApiParam(value = "Workspace server port")
            @RequestParam(value = "serverPort", required = false) String serverPort,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(201729, 1)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByHost(
                    connectionProperties, serverUrl, serverPort
            )) {
                return getDatasets(jdbcConnection, libraryName);
            }
        });
    }

    //  Dataset details endpoints

    private DataSet getDatasetDetails(JDBCConnection jdbcConnection, String libraryName, String datasetName) throws Exception {
        Library library = getLibrary(jdbcConnection, libraryName);

        DataSetDao dataSetDao = context.getBean(DataSetDao.class, jdbcConnection);
        DataSet dataset = dataSetDao.getDatasetByName(library.getLibname(), datasetName);

        DataSetColumnDao dataSetColumnDao = context.getBean(DataSetColumnDao.class, jdbcConnection);
        List<DataSetColumn> columns = dataSetColumnDao.getDatasetColumns(library.getLibname(), dataset.getName());

        dataset.setColumns(columns);

        return dataset;
    }

    @ApiOperation(
            value = "Gets dataset information for specific dataset by dataset name, library name and server name",
            notes = "Uses first available connection from connection list for supplied server name"
    )
    @RequestMapping(value = "/servers/{serverName}/libraries/{libraryName}/datasets/{datasetName}", method = RequestMethod.GET)
    public ResponseEntity<APIResponse<DataSet>> getDatasetDetailsByServer(
            @ApiParam(value = "Server name")
            @PathVariable String serverName,

            @ApiParam(value = "Library name")
            @PathVariable String libraryName,

            @ApiParam(value = "Dataset name")
            @PathVariable String datasetName,

            @ApiParam(value = "Repository name", defaultValue = "Foundation")
            @RequestParam(value = "repositoryName", required = false) String repositoryName,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(201729, 1)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByServerName(
                    connectionProperties, serverName, repositoryName
            )) {
                return getDatasetDetails(jdbcConnection, libraryName, datasetName);
            }
        });
    }

    @ApiOperation(value = "Gets dataset information for specific dataset by dataset name and library name using direct connection information")
    @RequestMapping(value = "/libraries/{libraryName}/datasets/{datasetName}", method = RequestMethod.GET)
    public ResponseEntity<APIResponse<DataSet>> getDatasetDetails(
            @ApiParam(value = "Library name")
            @PathVariable String libraryName,

            @ApiParam(value = "Dataset name")
            @PathVariable String datasetName,

            @ApiParam(value = "Workspace server host URL")
            @RequestParam(value = "serverUrl", required = false) String serverUrl,

            @ApiParam(value = "Workspace server port")
            @RequestParam(value = "serverPort", required = false) String serverPort,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(201729, 1)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByHost(
                    connectionProperties, serverUrl, serverPort
            )) {
                return getDatasetDetails(jdbcConnection, libraryName, datasetName);
            }
        });
    }

    // Dataset data endpoints

    private List<Map<String, Object>> getDatasetData(
            JDBCConnection jdbcConnection,
            String libraryName,
            String datasetName,
            String filterJson,
            int limit,
            int offset
    ) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> filter = null;
        if (filterJson != null && filterJson.length() > 0) {
            filter = mapper.readValue(filterJson, Map.class);
        } else {
            filter = null;
        }
        Map<String, Object> finalFilter = filter;
        DataSetDataDao dataSetDataDao = (DataSetDataDao) context.getBean("DataSetDataDao", jdbcConnection);
        return dataSetDataDao.getData(libraryName, datasetName, finalFilter, limit, offset);
    }

    @ApiOperation(
            value = "Retrieves data from dataset by dataset name, library name and server name",
            notes = "Uses first available connection from connection list for supplied server name"
    )
    @RequestMapping(value = "/servers/{serverName}/libraries/{libraryName}/datasets/{datasetName}/data", method = RequestMethod.GET)
    public ResponseEntity<APIResponse<List<Map<String, Object>>>> getDatasetDataByServer(
            @ApiParam(value = "Server name")
            @PathVariable String serverName,

            @ApiParam(value = "Library name")
            @PathVariable String libraryName,

            @ApiParam(value = "Dataset name")
            @PathVariable String datasetName,

            @ApiParam(value = "Repository name", defaultValue = "Foundation")
            @RequestParam(value = "repositoryName", required = false) String repositoryName,

            @ApiParam(value = "Number of records to retrieve. Value '0' means default value. Maximum value is 10000", defaultValue = "100")
            @RequestParam(value = "limit", defaultValue = "0") int limit,

            @ApiParam(value = "Dataset record offset", defaultValue = "0")
            @RequestParam(value = "offset", defaultValue = "0") int offset,

            @ApiParam(value = "Dataset filter in JSON format (example: {\"Sex\": \"M\", \"Age\": 14}). Must be URL-encoded")
            @RequestParam(value = "filter", required = false) String filterJson,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(201729, 1)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByServerName(
                    connectionProperties, serverName, repositoryName
            )) {
                return getDatasetData(jdbcConnection, libraryName, datasetName, filterJson, limit, offset);
            }
        });
    }

    @ApiOperation(value = "Retrieves data from dataset by dataset name and library name using direct connection information")
    @RequestMapping(value = "/libraries/{libraryName}/datasets/{datasetName}/data", method = RequestMethod.GET)
    public ResponseEntity<APIResponse<List<Map<String, Object>>>> getDatasetData(
            @ApiParam(value = "Library name")
            @PathVariable String libraryName,

            @ApiParam(value = "Dataset name")
            @PathVariable String datasetName,

            @ApiParam(value = "Workspace server host URL")
            @RequestParam(value = "serverUrl", required = false) String serverUrl,

            @ApiParam(value = "Workspace server port")
            @RequestParam(value = "serverPort", required = false) String serverPort,

            @ApiParam(value = "Number of records to retrieve. Value '0' means default value. Maximum value is 10000", defaultValue = "100")
            @RequestParam(value = "limit", defaultValue = "0") int limit,

            @ApiParam(value = "Dataset record offset", defaultValue = "0")
            @RequestParam(value = "offset", defaultValue = "0") int offset,

            @ApiParam(value = "Dataset filter JSON")
            @RequestParam(value = "filter", required = false) String filterJson,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(201729, 1)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByHost(
                    connectionProperties, serverUrl, serverPort
            )) {
                return getDatasetData(jdbcConnection, libraryName, datasetName, filterJson, limit, offset);
            }
        });
    }

    private DMLResponse putDataSetData(
            JDBCConnection jdbcConnection,
            String libraryName,
            String datasetName,
            List<Map<String, Object>> data,
            String byKey
    ) throws Exception {
        DataSetDataDao dao = new DataSetDataDao(jdbcConnection);
        int[] recordsAffected = dao.replaceData(libraryName, datasetName, data, byKey);
        
        return DMLResponse.builder()
                .itemsInserted(recordsAffected[0])
                .itemsRemoved(recordsAffected[1])
                .itemsUpdated(0)
                .build();
    }

    @ApiOperation(value = "Insert or replaces data into dataset by key")
    @RequestMapping(value = "/servers/{serverName}/libraries/{libraryName}/datasets/{datasetName}/data", method = RequestMethod.PUT)
    public ResponseEntity<APIResponse<DMLResponse>> putDataSetDataByServer(
            @ApiParam(value = "Server name")
            @PathVariable String serverName,

            @ApiParam(value = "Library name")
            @PathVariable String libraryName,

            @ApiParam(value = "Dataset name")
            @PathVariable String datasetName,

            @ApiParam(value = "Repository name", defaultValue = "Foundation")
            @RequestParam(value = "repositoryName", required = false) String repositoryName,

            @ApiParam(value = "Dataset key for record matching")
            @RequestParam(value = "byKey", required = false) String byKey,

            @RequestBody List<Map<String, Object>> data,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(17256449, 2)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByServerName(
                    connectionProperties, serverName, repositoryName
            )) {
                return putDataSetData(jdbcConnection, libraryName, datasetName, data, byKey);
            }
        });
    }

    @ApiOperation(value = "Insert or replaces data into dataset by key")
    @RequestMapping(value = "/libraries/{libraryName}/datasets/{datasetName}/data", method = RequestMethod.PUT)
    public ResponseEntity<APIResponse<DMLResponse>> putDataSetData(
            @ApiParam(value = "Library name")
            @PathVariable String libraryName,

            @ApiParam(value = "Dataset name")
            @PathVariable String datasetName,

            @ApiParam(value = "Workspace server host URL")
            @RequestParam(value = "serverUrl", required = false) String serverUrl,

            @ApiParam(value = "Workspace server port")
            @RequestParam(value = "serverPort", required = false) String serverPort,

            @ApiParam(value = "Dataset key for record matching")
            @RequestParam(value = "byKey", required = false) String byKey,

            @RequestBody List<Map<String, Object>> data,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(17256449, 2)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByHost(
                    connectionProperties, serverUrl, serverPort
            )) {
                return putDataSetData(jdbcConnection, libraryName, datasetName, data, byKey);
            }
        });
    }

    private DMLResponse postDataSetData(
            JDBCConnection jdbcConnection,
            String libraryName,
            String datasetName,
            List<Map<String, Object>> data
    ) throws Exception {
        DataSetDataDao dao = new DataSetDataDao(jdbcConnection);
        int[] recordsAffected = dao.replaceDataAll(libraryName, datasetName, data);
        return DMLResponse.builder()
                .itemsInserted(recordsAffected[0])
                .itemsRemoved(recordsAffected[1])
                .itemsUpdated(0)
                .build();
    }

    @ApiOperation(value = "Replaces all data in dataset with input data")
    @RequestMapping(value = "/libraries/{libraryName}/datasets/{datasetName}/data", method = RequestMethod.POST)
    public ResponseEntity<APIResponse<DMLResponse>> postDataSetData(
            @ApiParam(value = "Library name")
            @PathVariable String libraryName,

            @ApiParam(value = "Dataset name")
            @PathVariable String datasetName,

            @ApiParam(value = "Workspace server host URL")
            @RequestParam(value = "serverUrl", required = false) String serverUrl,

            @ApiParam(value = "Workspace server port")
            @RequestParam(value = "serverPort", required = false) String serverPort,

            @RequestBody List<Map<String, Object>> data,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(17256449, 2)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByHost(
                    connectionProperties, serverUrl, serverPort
            )) {
                return postDataSetData(jdbcConnection, libraryName, datasetName, data);
            }
        });
    }

    @ApiOperation(value = "Replaces all data in dataset with input data")
    @RequestMapping(value = "/servers/{serverName}/libraries/{libraryName}/datasets/{datasetName}/data", method = RequestMethod.POST)
    public ResponseEntity<APIResponse<DMLResponse>> postDataSetDataByServer(
            @ApiParam(value = "Server name")
            @PathVariable String serverName,

            @ApiParam(value = "Library name")
            @PathVariable String libraryName,

            @ApiParam(value = "Dataset name")
            @PathVariable String datasetName,

            @ApiParam(value = "Repository name", defaultValue = "Foundation")
            @RequestParam(value = "repositoryName", required = false) String repositoryName,

            @RequestBody List<Map<String, Object>> data,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(17256449, 2)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByServerName(
                    connectionProperties, serverName, repositoryName
            )) {
                return postDataSetData(jdbcConnection, libraryName, datasetName, data);
            }
        });
    }

    private DMLResponse deleteDataset(
            JDBCConnection jdbcConnection,
            String libraryName,
            String datasetName
    ) throws Exception {
        DataSetDao dao = new DataSetDao(jdbcConnection);
        dao.deleteDataset(libraryName, datasetName);
        return DMLResponse.builder()
        		.itemsInserted(0)
                .itemsRemoved(1)
                .itemsUpdated(0)
                .build();
    }

    @ApiOperation(value = "Deletes dataset from library")
    @RequestMapping(value = "/libraries/{libraryName}/datasets/{datasetName}/data", method = RequestMethod.DELETE)
    public ResponseEntity<APIResponse<DMLResponse>> deleteDataset(
            @ApiParam(value = "Library name")
            @PathVariable String libraryName,

            @ApiParam(value = "Dataset name")
            @PathVariable String datasetName,

            @ApiParam(value = "Workspace server host URL")
            @RequestParam(value = "serverUrl", required = false) String serverUrl,

            @ApiParam(value = "Workspace server port")
            @RequestParam(value = "serverPort", required = false) String serverPort,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(17256449, 2)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByHost(
                    connectionProperties, serverUrl, serverPort
            )) {
                return deleteDataset(jdbcConnection, libraryName, datasetName);
            }
        });
    }

    @ApiOperation(value = "Deletes dataset from library")
    @RequestMapping(value = "/servers/{serverName}/libraries/{libraryName}/datasets/{datasetName}/data", method = RequestMethod.DELETE)
    public ResponseEntity<APIResponse<DMLResponse>> deleteDatasetByServer(
            @ApiParam(value = "Server name")
            @PathVariable String serverName,

            @ApiParam(value = "Library name")
            @PathVariable String libraryName,

            @ApiParam(value = "Dataset name")
            @PathVariable String datasetName,

            @ApiParam(value = "Repository name", defaultValue = "Foundation")
            @RequestParam(value = "repositoryName", required = false) String repositoryName,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(17256449, 2)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (JDBCConnection jdbcConnection = connectionHelpers.getJDBCConnectionByServerName(
                    connectionProperties, serverName, repositoryName
            )) {
                return deleteDataset(jdbcConnection, libraryName, datasetName);
            }
        });
    }

    // Users

    private Person getUserByName(
            IOMConnection iomConnection,
            String repositoryName,
            String userName
    ) throws Exception {
        val repositoriesDao = context.getBean(RepositoriesDao.class, iomConnection);
        val repository = repositoriesDao.getRepositoryByName(repositoryName);
        val personsDao = context.getBean(PersonsDao.class, iomConnection);
        return personsDao.getPersonByName(repository.getId(), userName);
    }

    @ApiOperation(value = "Gets server users and its identities")
    @RequestMapping(value = "/meta/users", method = RequestMethod.GET)
    public ResponseEntity<APIResponse<List<Person>>> getUsers(
            @ApiParam(value = "Repository name", defaultValue = "Foundation")
            @RequestParam(value = "repositoryName", required = false) String repositoryName,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(17256449, 2)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (IOMConnection iomConnection = context.getBean(IOMConnection.class, connectionProperties)) {
                val repositoriesDao = context.getBean(RepositoriesDao.class, iomConnection);
                val repository = repositoriesDao.getRepositoryByName(repositoryName);
                val personsDao = context.getBean(PersonsDao.class, iomConnection);
                return personsDao.getPersons(repository.getId());
            }
        });
    }

    @ApiOperation(value = "Gets server user and its identities by name")
    @RequestMapping(value = "/meta/users/{userName}", method = RequestMethod.GET)
    public ResponseEntity<APIResponse<Person>> getUserByName(
            @ApiParam("User name to search")
            @PathVariable String userName,

            @ApiParam(value = "Repository name", defaultValue = "Foundation")
            @RequestParam(value = "repositoryName", required = false) String repositoryName,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(17256449, 2)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (IOMConnection iomConnection = context.getBean(IOMConnection.class, connectionProperties)) {
                return getUserByName(iomConnection, repositoryName, userName);
            }
        });
    }

    @ApiOperation(value = "Gets configured user information and its identities")
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public ResponseEntity<APIResponse<Person>> getCurrentUser(
            @ApiParam(value = "Repository name", defaultValue = "Foundation")
            @RequestParam(value = "repositoryName", required = false) String repositoryName,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(201729, 1)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (IOMConnection iomConnection = context.getBean(IOMConnection.class, connectionProperties)) {
                return getUserByName(iomConnection, repositoryName, connectionProperties.getUserName());
            }
        });
    }

    // IdentityGroups

    @ApiOperation(value = "Get groups and their associated groups and users")
    @RequestMapping(value = "/meta/groups", method = RequestMethod.GET)
    public ResponseEntity<APIResponse<List<IdentityGroup>>> getGroups(
            @ApiParam(value = "Repository name", defaultValue = "Foundation")
            @RequestParam(value = "repositoryName", required = false) String repositoryName,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(17256449, 2)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (IOMConnection iomConnection = context.getBean(IOMConnection.class, connectionProperties)) {
                val repositoriesDao = context.getBean(RepositoriesDao.class, iomConnection);
                val repository = repositoriesDao.getRepositoryByName(repositoryName);
                val identityGroupsDao = context.getBean(IdentityGroupsDao.class, iomConnection);
                return identityGroupsDao.getGroups(repository.getId());
            }
        });
    }

    @ApiOperation(value = "Get group and its associated groups and users by group name")
    @RequestMapping(value = "/meta/groups/{groupName}", method = RequestMethod.GET)
    public ResponseEntity<APIResponse<IdentityGroup>> getGroupByName(
            @ApiParam("Group name to search")
            @PathVariable String groupName,

            @ApiParam(value = "Repository name", defaultValue = "Foundation")
            @RequestParam(value = "repositoryName", required = false) String repositoryName,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(17256449, 2)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (IOMConnection iomConnection = context.getBean(IOMConnection.class, connectionProperties)) {
                val repositoriesDao = context.getBean(RepositoriesDao.class, iomConnection);
                val repository = repositoriesDao.getRepositoryByName(repositoryName);
                val identityGroupsDao = context.getBean(IdentityGroupsDao.class, iomConnection);
                return identityGroupsDao.getGroupByName(repository.getId(), groupName);
            }
        });
    }

    // Roles

    @ApiOperation(value = "Get roles and their associated groups and users")
    @RequestMapping(value = "/meta/roles", method = RequestMethod.GET)
    public ResponseEntity<APIResponse<List<IdentityGroup>>> getRoles(
            @ApiParam(value = "Repository name", defaultValue = "Foundation")
            @RequestParam(value = "repositoryName", required = false) String repositoryName,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(17256449, 2)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (IOMConnection iomConnection = context.getBean(IOMConnection.class, connectionProperties)) {
                val repositoriesDao = context.getBean(RepositoriesDao.class, iomConnection);
                val repository = repositoriesDao.getRepositoryByName(repositoryName);
                val identityGroupsDao = context.getBean(IdentityGroupsDao.class, iomConnection);
                return identityGroupsDao.getRoles(repository.getId());
            }
        });
    }

    @ApiOperation(value = "Get roles and its associated groups and users by role name")
    @RequestMapping(value = "/meta/roles/{roleName}", method = RequestMethod.GET)
    public ResponseEntity<APIResponse<IdentityGroup>> getRoleByName(
            @ApiParam("Role name to search")
            @PathVariable String roleName,

            @ApiParam(value = "Repository name", defaultValue = "Foundation")
            @RequestParam(value = "repositoryName", required = false) String repositoryName,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(17256449, 2)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (IOMConnection iomConnection = context.getBean(IOMConnection.class, connectionProperties)) {
                val repositoriesDao = context.getBean(RepositoriesDao.class, iomConnection);
                val repository = repositoriesDao.getRepositoryByName(repositoryName);
                val identityGroupsDao = context.getBean(IdentityGroupsDao.class, iomConnection);
                return identityGroupsDao.getRoleByName(repository.getId(), roleName);
            }
        });
    }

    // Stored Process Server

    @ApiOperation(value = "Gets list of available Stored Process servers and their connections from metadata server")
    @RequestMapping(value = "/stp", method = RequestMethod.GET)
    public ResponseEntity<APIResponse<List<ServerComponent>>> getSTPServers(
            @ApiParam(value = "Repository name", defaultValue = "Foundation")
            @RequestParam(value = "repositoryName", required = false) String repositoryName,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(201729, 1)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (IOMConnection iomConnection = context.getBean(IOMConnection.class, connectionProperties)) {
                val repositoriesDao = context.getBean(RepositoriesDao.class, iomConnection);
                val repository = repositoriesDao.getRepositoryByName(repositoryName);
                val serversDao = context.getBean(ServersDao.class, iomConnection);
                return serversDao.getSTPServers(repository.getId());
            }
        });
    }

    @ApiOperation(value = "Gets Stored Process server information and its connections from metadata server by name")
    @RequestMapping(value = "/stp/{serverName}", method = RequestMethod.GET)
    public ResponseEntity<APIResponse<ServerComponent>> getSTPServer(
            @ApiParam(value = "Server name")
            @PathVariable String serverName,

            @ApiParam(value = "Repository name", defaultValue = "Foundation")
            @RequestParam(value = "repositoryName", required = false) String repositoryName,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(201729, 1)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (IOMConnection iomConnection = context.getBean(IOMConnection.class, connectionProperties)) {
                val repositoriesDao = context.getBean(RepositoriesDao.class, iomConnection);
                val repository = repositoriesDao.getRepositoryByName(repositoryName);
                val serversDao = context.getBean(ServersDao.class, iomConnection);
                return serversDao.getSTPServerByName(repository.getId(), serverName);
            }
        });
    }

    @ApiOperation(value = "Creates a library at given server with given library name and parameters")
    @RequestMapping(value = "/servers/{serverName}/libraries/{libraryName}", method = RequestMethod.POST)
    public ResponseEntity<APIResponse<Library>> createLibrary(
            @ApiParam(value = "Server name")
            @PathVariable String serverName,

            @ApiParam(value = "Created library name, as will be used in LIBNAME statement")
            @PathVariable String libraryName,

            @ApiParam(value = "Repository name", defaultValue = "Foundation")
            @RequestParam(required = false) String repositoryName,

            @ApiParam(value = "LIBNAME engine")
            @RequestParam String engine,

            @ApiParam(value = "Library display name")
            @RequestParam String displayName,

            @ApiParam(value = "Library data path")
            @RequestParam String path,

            @ApiParam(value = "Folder to place metadata object")
            @RequestParam String location,

            @ApiParam(value = "Create preassigned library?")
            @RequestParam boolean isPreassigned,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(17256449, 2)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (IOMConnection iomConnection = context.getBean(IOMConnection.class, connectionProperties)) {
                val repositoriesDao = context.getBean(RepositoriesDao.class, iomConnection);
                val repository = repositoriesDao.getRepositoryByName(repositoryName);
                val libraryParams = LibraryParams.builder()
                        .libRef(libraryName)
                        .serverName(serverName)
                        .engine(engine)
                        .libraryName(displayName)
                        .path(path)
                        .location(location)
                        .isPreassigned(isPreassigned)
                        .build();
                val libraryMetadataDao = context.getBean(LibraryMetadataDao.class, iomConnection);
                return libraryMetadataDao.createLibrary(repository.getId(), libraryParams);
            }
        });
    }
    

    @ApiOperation(value = "Removes all libraries with matching library name")
    @RequestMapping(value = "/servers/{serverName}/libraries/{libraryName}", method = RequestMethod.DELETE)
    public ResponseEntity<APIResponse<Object>> deleteLibrary(
            @ApiParam(value = "Server name")
            @PathVariable String serverName,

            @ApiParam(value = "Library name to match, as used in LIBNAME")
            @PathVariable String libraryName,

            @ApiParam(value = "Repository name", defaultValue = "Foundation")
            @RequestParam(value = "repositoryName", required = false) String repositoryName,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(17256449, 2)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (IOMConnection iomConnection = context.getBean(IOMConnection.class, connectionProperties)) {
                val repositoriesDao = context.getBean(RepositoriesDao.class, iomConnection);
                val repository = repositoriesDao.getRepositoryByName(repositoryName);
                val libraryMetadataDao = context.getBean(LibraryMetadataDao.class, iomConnection);
                libraryMetadataDao.deleteLibrary(repository.getId(), libraryName);
                return true;
            }
        });
    }

    // command endpoint

    @ApiOperation(
            value = "Gets list of available libraries for workspace server by server name",
            notes = "Uses first available connection from connection list for supplied server name"
    )
    @RequestMapping(value = "/servers/{serverName}/cmd", method = RequestMethod.PUT)
    public ResponseEntity<APIResponse<SASLanguageResponse>> putCommandByServer(
            @ApiParam(value = "Server name")
            @PathVariable String serverName,

            @ApiParam(value = "Repository name", defaultValue = "Foundation")
            @RequestParam(value = "repositoryName", required = false) String repositoryName,

            @ApiParam(value = "Enables log output in endpoint response")
            @RequestParam(value = "logEnabled", defaultValue = "true") boolean logEnabled,

            @ApiParam(value = "SAS commands to submit to server")
            @RequestBody String command,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(17256449, 2)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            ConnectionProperties workspaceConnectionProps = connectionHelpers.getWorkspaceConnectionPropsByServerName(
                    connectionProperties,
                    serverName,
                    repositoryName
            );
            try (WorkspaceConnection workspaceConnection = context.getBean(WorkspaceConnection.class, workspaceConnectionProps)) {
                return workspaceConnection.submitSasCommand(command, logEnabled);
            }
        });
    }

    @ApiOperation(value = "Send a SAS command for execution to workspace server")
    @RequestMapping(value = "/cmd", method = RequestMethod.PUT)
    public ResponseEntity<APIResponse<SASLanguageResponse>> putCommand(
            @ApiParam(value = "Workspace server host URL")
            @RequestParam(value = "serverUrl", required = false) String serverUrl,

            @ApiParam(value = "Workspace server port")
            @RequestParam(value = "serverPort", required = false) String serverPort,

            @ApiParam(value = "Enables log output in endpoint response. Doesn't populate response fields if set to 'false'")
            @RequestParam(value = "logEnabled", defaultValue = "true") boolean logEnabled,

            @ApiParam(value = "SAS commands to submit to server")
            @RequestBody String command,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(17256449, 2)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            ConnectionProperties workspaceConnectionProps = connectionHelpers.getWorkspaceConnectionPropsByHost(
                    connectionProperties,
                    serverUrl,
                    serverPort
            );
            try (WorkspaceConnection workspaceConnection = context.getBean(WorkspaceConnection.class, workspaceConnectionProps)) {
                return workspaceConnection.submitSasCommand(command, logEnabled);
            }
        });
    }

    // Permissions - test endpoint

    @ApiOperation(value = "Get object permissions")
    @RequestMapping(value = "/meta/permissions", method = RequestMethod.GET)
    public ResponseEntity<APIResponse<List<Permission>>> getPermissions(
            @ApiParam(value = "SAS metadata object type")
            @RequestParam
            String objectType,

            @ApiParam(value = "SAS metadata object ID")
            @RequestParam
            String objectId,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(17256449, 2)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (val iomConnection = new  IOMConnection(connectionProperties)) {
                val permissionsDao = context.getBean(PermissionsDao.class, iomConnection);
                return permissionsDao.getObjectPermissions(objectType, objectId);
            }
        });
    }

    // Search endpoint
    @ApiOperation(value = "Find objects")
    @RequestMapping(value = "/meta/search", method = RequestMethod.GET)
    public ResponseEntity<APIResponse<List<SASDetailedObject>>> findObjects(
            @ApiParam(value = "Repository name", defaultValue = "Foundation")
            @RequestParam(required = false) String repositoryName,
            @ApiParam("Search in specific folder location")
            @RequestParam(required = false) String location,
            @ApiParam("Search in subfolders")
            @RequestParam(required = false) boolean locationRecursive,
            @ApiParam("SAS Metadata object ID")
            @RequestParam(required = false) String objectId,
            @ApiParam("SAS Metadata object type. Omit for any object types")
            @RequestParam(required = false) String objectType,
            @ApiParam("Comma-separated list of target PublicType attributes")
            @RequestParam(required = false) String publicType,
            @ApiParam("Name matches (case-insensitive)")
            @RequestParam(required = false) String nameEquals,
            @ApiParam("Name starts with (case-insensitive)")
            @RequestParam(required = false) String nameStarts,
            @ApiParam("Name contains (case-insensitive)")
            @RequestParam(required = false) String nameContains,
            @ApiParam("Name matches regex")
            @RequestParam(required = false) String nameRegex,
            @ApiParam("Description contains (case-insensitive)")
            @RequestParam(required = false) String descriptionContains,
            @ApiParam("Description matches regex")
            @RequestParam(required = false) String descriptionRegex,
            @ApiParam("MetadataCreated greater than (ISO datatime format)")
            @RequestParam(required = false) String createdGt,
            @ApiParam("MetadataCreated lower than (ISO datatime format)")
            @RequestParam(required = false) String createdLt,
            @ApiParam("MetadataModified greater than (ISO datatime format)")
            @RequestParam(required = false) String modifiedGt,
            @ApiParam("MetadataModified lower than (ISO datatime format)")
            @RequestParam(required = false) String modifiedLt,
            @ApiParam("Libref name for associated library object for a table. For table types only.")
            @RequestParam(required = false) String tableLibref,
            @ApiParam("DBMS engine name for associated library object for a table. For table types only.")
            @RequestParam(required = false) String tableDBMS,
            @ApiParam("Include object associations?")
            @RequestParam(required = false) boolean includeAssociations,
            @ApiParam("Include metadata object permissions?")
            @RequestParam(required = false) boolean includePermissions,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(5325825, 3)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (val iomConnection = context.getBean(IOMConnection.class, connectionProperties)) {
                val repositoryDao = context.getBean(RepositoriesDao.class, iomConnection);
                val repository = repositoryDao.getRepositoryByName(repositoryName);

                val searchParams = new SearchParams();
                if (location != null) {
                    val treesDao = context.getBean(TreesDao.class, iomConnection);
                    val folders = treesDao.getTreesByPath(repository.getId(), location);
                    val foldersIds = treesDao.collectTreeIds(folders, locationRecursive);
                    searchParams.setLocationFolderIds(foldersIds.toArray(new String[0]));
                }
                if (publicType != null) {
                    val publicTypes = publicType.split(",");
                    searchParams.setPublicTypes(publicTypes);
                }
                if (createdGt != null) {
                    searchParams.setCreatedGt(LocalDateTime.parse(createdGt, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
                if (createdLt != null) {
                    searchParams.setCreatedLt(LocalDateTime.parse(createdLt, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
                if (modifiedGt != null) {
                    searchParams.setModifiedGt(LocalDateTime.parse(modifiedGt, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
                if (modifiedGt != null) {
                    searchParams.setModifiedLt(LocalDateTime.parse(modifiedLt, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
                searchParams.setId(objectId);
                searchParams.setType(objectType);
                searchParams.setNameEquals(nameEquals);
                searchParams.setNameStarts(nameStarts);
                searchParams.setNameContains(nameContains);
                searchParams.setNameRegex(nameRegex);
                searchParams.setDescriptionContains(descriptionContains);
                searchParams.setDescriptionRegex(descriptionRegex);
                searchParams.setIncludeAssociations(includeAssociations);
                searchParams.setIncludePermissions(includePermissions);
                searchParams.setTableLibref(tableLibref);
                searchParams.setTableDBMS(tableDBMS);

                val searchDao = context.getBean(SearchDao.class, iomConnection);
                return searchDao.getFilteredObjects(repository.getId(), searchParams);
            }
        });
    }

    ////////////////////// Object operations ////////////////////////

    @ApiOperation(value = "Move object between folders")
    @RequestMapping(value = "/meta/objects/move", method = RequestMethod.POST)
    public ResponseEntity<APIResponse<Object>> moveObject(
            @ApiParam(value = "Source folder location")
            @RequestParam String sourceLocation,
            @ApiParam(value = "Source object name")
            @RequestParam String sourceName,
            @ApiParam(value = "Source object PublicType")
            @RequestParam String publicType,
            @ApiParam(value = "Destination folder location")
            @RequestParam String destinationLocation,
            @ApiParam(value = "Repository name", defaultValue = "Foundation")
            @RequestParam(required = false) String repositoryName,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(5325825, 3)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            
            val connectionProperties = getConnectionProperties(request);
            try (val iomConnection = context.getBean(IOMConnection.class, connectionProperties)) {
                val repositoriesDao = context.getBean(RepositoriesDao.class, iomConnection);
                val repository = repositoriesDao.getRepositoryByName(repositoryName);

                val operationsDao = context.getBean(ObjectOperationsDao.class, iomConnection);
                operationsDao.moveObject(repository.getId(), sourceLocation, sourceName, publicType, destinationLocation);
            }
            return null;
        });
    }

    @ApiOperation(value = "Delete object by folder and name")
    @RequestMapping(value = "/meta/objects/delete", method = RequestMethod.POST)
    public ResponseEntity<APIResponse<Object>> deleteObject(
            @ApiParam(value = "Folder location")
            @RequestParam String sourceLocation,
            @ApiParam(value = "Object name")
            @RequestParam String sourceName,
            @ApiParam(value = "Object PublicType")
            @RequestParam String publicType,
            @ApiParam(value = "Repository name", defaultValue = "Foundation")
            @RequestParam(required = false) String repositoryName,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(5325825, 3)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (val iomConnection = context.getBean(IOMConnection.class, connectionProperties)) {
                val repositoriesDao = context.getBean(RepositoriesDao.class, iomConnection);
                val repository = repositoriesDao.getRepositoryByName(repositoryName);

                val operationsDao = context.getBean(ObjectOperationsDao.class, iomConnection);
                operationsDao.deleteObject(repository.getId(), sourceLocation, sourceName, publicType);
            }
            return null;
        });
    }

    @ApiOperation(value = "Copy object between folders")
    @RequestMapping(value = "/meta/objects/copy", method = RequestMethod.POST)
    public ResponseEntity<APIResponse<Object>> copyObject(
            @ApiParam(value = "Source folder location")
            @RequestParam String sourceLocation,
            @ApiParam(value = "Source object name")
            @RequestParam String sourceName,
            @ApiParam(value = "Source object PublicType")
            @RequestParam String publicType,
            @ApiParam(value = "Destination folder location")
            @RequestParam String destinationLocation,
            @ApiParam(value = "Repository name", defaultValue = "Foundation")
            @RequestParam(required = false) String repositoryName,
            HttpServletRequest request
    ) {
        return ResponseUtils.withResponse(() -> {
            if (!context.getBean(LicenseCheckerFactory.class).getLicenseChecker().check(5325825, 3)) {
                throw new Exception("The endpoint is not available for use with current license");
            }
            val connectionProperties = getConnectionProperties(request);
            try (val iomConnection = context.getBean(IOMConnection.class, connectionProperties)) {
                val repositoriesDao = context.getBean(RepositoriesDao.class, iomConnection);
                val repository = repositoriesDao.getRepositoryByName(repositoryName);

                val operationsDao = context.getBean(ObjectOperationsDao.class, iomConnection);
                return operationsDao.copyObject(connectionProperties, repository.getId(), sourceLocation, sourceName, publicType, destinationLocation);
            }
        });
    }

    //////////// License endpoint /////////////////
    @ApiOperation(value = "Get information about active SAS Proxy license")
    @RequestMapping(value = "/license", method = RequestMethod.GET)
    public ResponseEntity<APIResponse<List<LicenseCapabilities>>> getLicense(

    ) {
        return ResponseUtils.withResponse(() -> {
            return context.getBean(LicenseCheckerFactory.class).getLicenseChecker().getCapabilities();
        });
    }
}

