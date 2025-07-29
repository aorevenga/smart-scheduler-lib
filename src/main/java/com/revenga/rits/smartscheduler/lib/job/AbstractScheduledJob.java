package com.revenga.rits.smartscheduler.lib.job;

import java.lang.reflect.Method;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revenga.rits.smartscheduler.lib.model.ConfigBase;
import com.revenga.rits.smartscheduler.lib.service.AbstractSchedulerManager;

import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class AbstractScheduledJob implements InterruptableJob {
	
	private static final Integer MAX_JOB_WAIT_TIMES = 10;
	
	protected Class<?> processorClass;
	protected String appName;
	
	protected AbstractScheduledJob() {

		super();
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		AbstractSchedulerManager schedulerManager = null;
		
		try {
			
			SchedulerContext schedulerContext = context.getScheduler().getContext();
					
			schedulerManager = (AbstractSchedulerManager) schedulerContext
							.get(context.getJobDetail().getKey().toString());

			if (schedulerManager != null) {
				
				if (!schedulerManager.isRunning()) {
					
					schedulerManager.setRunning(true);
					schedulerManager.setRunningThread(Thread.currentThread());
					schedulerManager.resetBusyCounter();
					log.trace(this.getClass().getSimpleName() + " => Scheduler running...");
									
					if (schedulerManager.getNewConfig() != null) {

						schedulerManager.setConfig(schedulerManager.getNewConfig());
						schedulerManager.setNewConfig(null);
						
						printConfig(schedulerManager.getConfig());
					}
					
					Object processorInstance = processorClass.getDeclaredConstructor(ConfigBase.class)
							.newInstance(schedulerManager.getConfig());
					
					Method method = processorInstance.getClass().getDeclaredMethod("run");
					
					method.invoke(processorInstance);
					
					schedulerManager.setRunning(false);
					
					log.trace(this.getClass().getSimpleName() + " => Scheduler ends");
				}
				else {
					
					Integer busyCounter = schedulerManager.incBusyCounter();
					
					log.warn(String.format("%s => The previous scheduler is still running (%d/%d) on thread %s",
							this.getClass().getSimpleName(), busyCounter, MAX_JOB_WAIT_TIMES, schedulerManager.getRunningThread().getName()));
					
					if (busyCounter >= MAX_JOB_WAIT_TIMES) {
						
						log.warn(String.format("%s => Interrupting job in thread %s", this.getClass().getSimpleName(),
								schedulerManager.getRunningThread().getName()));
					
						schedulerManager.killRunningJob();
					}
				}
			}

		} catch (Exception e) {
			
			if (schedulerManager != null) {
				
				schedulerManager.setRunning(false);
				schedulerManager.setRunningThread(null);
				schedulerManager.resetBusyCounter();
			}
			
			log.error(e.getMessage());
			log.debug(ExceptionUtils.getStackTrace(e));
		}
	}
	
	@Override
	public void interrupt() {
		
		log.debug("Interrupt signal received");
	}
	
	protected void printConfig(ConfigBase config) {
		
		try {
			
			ObjectMapper objectMapper = new ObjectMapper();
					
			String jsonConfig = objectMapper.writeValueAsString(config);
			
			log.info("New config running: " + jsonConfig);
			
		} catch (JsonProcessingException e) {
			
			log.error(e.getMessage());
			log.debug(ExceptionUtils.getStackTrace(e));
			e.printStackTrace();
		}
	}
}
