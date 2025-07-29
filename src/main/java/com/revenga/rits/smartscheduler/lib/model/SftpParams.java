package com.revenga.rits.smartscheduler.lib.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public  class SftpParams implements Serializable {

	private static final long serialVersionUID = -5380809741043068713L;

	private String user;
	private String password;
	private String host;
	private Integer port;
	private String localTempPath;
	private String localPendingPath;
	private String remotePath;
	private Integer maxAttempts;
	private Long waitTimeInMilliseconds;
}
