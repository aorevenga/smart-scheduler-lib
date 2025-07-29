package com.revenga.rits.smartscheduler.lib.model;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class ConfigBase implements Serializable {

	private static final long serialVersionUID = 2773046831462365476L;

	private String cronExpression;
	private Integer processorThreads;
	private List<String> libs;
}
