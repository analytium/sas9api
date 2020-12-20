package com.codexsoft.sas.connections.jdbc.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel(description = "Dataset representation object")
public class DataSet {
    @ApiModelProperty(value = "Dataset name")
    private String name;

    @ApiModelProperty(value = "Dataset type. 'DATA' for ordinal datasets")
    private String type;

    @ApiModelProperty(value = "Dataset label")
    private String label;

    @ApiModelProperty(value = "Dataset creation date", dataType = "String")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime creationDate;

    @ApiModelProperty(value = "Dataset modification date", dataType = "String")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime modificationDate;

    @ApiModelProperty(value = "Dataset objects number")
    private int objectsNumber;

    @ApiModelProperty(value = "Dataset columns")
    private List<DataSetColumn> columns;

    public static DataSet fromResultSet(ResultSet result) throws SQLException {
        DataSet dataset = new DataSet();

        dataset.setName(result.getString("memname").trim());
        dataset.setType(result.getString("typemem").trim());
        dataset.setLabel(result.getString("memlabel").trim());
        dataset.setCreationDate(result.getTimestamp("crdate").toLocalDateTime());
        dataset.setModificationDate(result.getTimestamp("modate").toLocalDateTime());
        dataset.setObjectsNumber(result.getInt("nobs"));

        return dataset;
    }
}
