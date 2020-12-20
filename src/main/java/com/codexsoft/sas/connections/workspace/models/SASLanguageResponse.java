package com.codexsoft.sas.connections.workspace.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SASLanguageResponse {
	private String lines;
	private String log;
}
