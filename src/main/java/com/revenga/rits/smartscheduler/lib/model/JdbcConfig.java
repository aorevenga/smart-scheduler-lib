package com.revenga.rits.smartscheduler.lib.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JdbcConfig implements Serializable {

	private static final long serialVersionUID = -8424219473338423199L;
	private String url;
	private String username;
	private String password;
}
