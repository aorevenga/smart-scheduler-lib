package com.revenga.rits.smartscheduler.lib.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.revenga.rits.smartscheduler.lib.model.ConfigBase;

import lombok.Getter;

@Getter
public abstract class AbstractProcessor {
	
	public static class ProcessorException extends Exception {

		private static final long serialVersionUID = 2829610090193860755L;
		
		public ProcessorException(String message) {
			
			super(message);
		}
	}

	protected ConfigBase configBase;
	protected Integer processorThreads;
	protected ExecutorService executor;
	
	protected AbstractProcessor(ConfigBase configBase) {
		super();
		
		this.configBase = configBase;
		this.processorThreads = (this.configBase.getProcessorThreads() != null ? this.configBase.getProcessorThreads() : 1);
		this.executor = Executors.newFixedThreadPool(this.processorThreads);
	}
	
	public abstract void run();
	
	public void stop() {}
}
