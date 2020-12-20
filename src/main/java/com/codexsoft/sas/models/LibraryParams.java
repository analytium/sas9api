package com.codexsoft.sas.models;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class LibraryParams {
    private String serverName;
    private String libraryName;
    private String libRef;
    private String engine;
    private String path;
    private String location;
    private boolean isPreassigned;
}
