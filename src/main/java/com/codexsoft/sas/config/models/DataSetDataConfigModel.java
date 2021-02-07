package com.codexsoft.sas.config.models;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("datasetdata")
@Data
public class DataSetDataConfigModel {
    private int maxFetchSize;
    private int defaultFetchSize;
}
