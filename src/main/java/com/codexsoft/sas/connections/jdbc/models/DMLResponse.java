package com.codexsoft.sas.connections.jdbc.models;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel(description = "Response for insert/update/delete operations")
public class DMLResponse {
    @ApiModelProperty(value = "Number of records inserted during operation")
    private Integer itemsInserted;

    @ApiModelProperty(value = "Number of records removed during operation")
    private Integer itemsRemoved;

    @ApiModelProperty(value = "Number of records updated during operation")
    private Integer itemsUpdated;
}
