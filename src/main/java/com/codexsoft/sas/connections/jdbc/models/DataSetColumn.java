package com.codexsoft.sas.connections.jdbc.models;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;


@Data
@ApiModel(description = "Dataset column description object")
public class DataSetColumn {
    @ApiModelProperty(value = "Column name")
    private String name;

    @ApiModelProperty(value = "Column type")
    private String type;

    @ApiModelProperty(value = "Column extended type")
    private String extendedType;

    @ApiModelProperty(value = "Column type length")
    private int length;

    @ApiModelProperty(value = "NOT NULL flag")
    private Boolean notNull;

    @ApiModelProperty(value = "Type of index. Empty string for none")
    private String indexType;

    @ApiModelProperty(value = "Index of column in dataset sorting columns. 0 for none")
    private int sortedBy;

    @ApiModelProperty(value = "Index of column in dataset columns. Starts from 1")
    private int columnNumber;

    @ApiModelProperty(value = "Column label")
    private String label;

    public static DataSetColumn fromResultSet(ResultSet result) throws SQLException {
        DataSetColumn column = new DataSetColumn();

        column.setName(result.getString("name").trim());
        column.setType(result.getString("type").trim());
        column.setExtendedType(result.getString("xtype").trim());
        column.setLength(result.getInt("length"));
        column.setNotNull(result.getString("notnull").trim().matches("yes"));
        column.setIndexType(result.getString("idxusage").trim());
        column.setSortedBy(result.getInt("sortedby"));
        column.setLabel(result.getString("label").trim());
        column.setColumnNumber(result.getInt("varnum"));

        return column;
    }
}
