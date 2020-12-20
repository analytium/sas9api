package com.codexsoft.sas.connections.jdbc.models;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;

@Data
@ApiModel(description = "Library object")
public class Library {
//OP: id field removed as id=null for libraries list	
//    @ApiModelProperty(value = "SAS metadata object ID")
//    private String id;

    @ApiModelProperty(value = "Library LIBNAME")
    private String libname;

    @ApiModelProperty(value = "Library engine")
    private String engine;

    @ApiModelProperty(value = "Library data path")
    private String path;

    @ApiModelProperty(value = "Library level")
    private int level;

    @ApiModelProperty(value = "Readonly flag")
    private Boolean readonly;

    @ApiModelProperty(value = "Sequential flag")
    private Boolean sequential;

    @ApiModelProperty(value = "Temporary flag")
    private Boolean temp;

    public static Library fromResultSet(ResultSet result) throws SQLException {
        Library library = new Library();
        
        library.setLibname(result.getString("libname").trim());
        library.setEngine(result.getString("engine").trim());
        library.setPath(result.getString("path").trim());
        library.setReadonly(result.getString("readonly").trim().matches("yes"));
        library.setSequential(result.getString("sequential").trim().matches("yes"));
        library.setTemp(result.getString("temp").trim().matches("yes"));
        library.setLevel(result.getInt("level"));

        return library;
    }
}
