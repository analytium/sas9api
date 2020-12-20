package com.codexsoft.sas.secure.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EndpointDescription {
    private String method;
    private String url;
}
