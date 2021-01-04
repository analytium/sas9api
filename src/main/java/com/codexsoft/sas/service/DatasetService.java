package com.codexsoft.sas.service;

import com.codexsoft.sas.connections.jdbc.models.DMLResponse;
import com.codexsoft.sas.connections.jdbc.models.DataSet;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface DatasetService {

    List<DataSet> getDatasetsByServerName(String serverName, String libraryName, String repositoryName, HttpServletRequest request) throws Exception;

    List<DataSet> getDatasetsByHost(String libraryName, String serverUrl, String serverPort, HttpServletRequest request) throws Exception;

    DataSet getDatasetDetailsByServerName(String serverName, String libraryName, String datasetName, String repositoryName, HttpServletRequest request) throws Exception;

    DataSet getDatasetDetailsByHost(String libraryName, String datasetName, String serverUrl, String serverPort, HttpServletRequest request) throws Exception;

    List<Map<String, Object>> getDatasetDataByServerName(String serverName, String libraryName, String datasetName, String repositoryName, int limit, int offset, String filterJson, HttpServletRequest request) throws Exception;

    List<Map<String, Object>> getDatasetDataByHost(String libraryName, String datasetName, String serverUrl, String serverPort, int limit, int offset, String filterJson, HttpServletRequest request) throws Exception;

    DMLResponse putDataSetDataByServerName(String serverName, String libraryName, String datasetName, String repositoryName, String byKey, List<Map<String, Object>> data, HttpServletRequest request) throws Exception;

    DMLResponse putDataSetDataByHost(String libraryName, String datasetName, String serverUrl, String serverPort, String byKey, List<Map<String, Object>> data, HttpServletRequest request) throws Exception;

    DMLResponse postDataSetDataByHost(String libraryName, String datasetName, String serverUrl, String serverPort, List<Map<String, Object>> data, HttpServletRequest request) throws Exception;

    DMLResponse postDataSetDataByServerName(String serverName, String libraryName, String datasetName, String repositoryName, List<Map<String, Object>> data, HttpServletRequest request) throws Exception;

    DMLResponse deleteDatasetByHost(String libraryName, String datasetName, String serverUrl, String serverPort, HttpServletRequest request) throws Exception;

    DMLResponse deleteDatasetByServerName(String serverName, String libraryName, String datasetName, String repositoryName, HttpServletRequest request) throws Exception;
}
