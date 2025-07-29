package com.revenga.rits.smartscheduler.lib.service;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.quartz.SchedulerException;

import com.revenga.rits.smartscheduler.lib.model.ConfigBase;

import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;

@Log4j2
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractTaskManager extends AbstractManager {

	private Class<? extends AbstractProcessor> proccesorClass;
	private AbstractProcessor processorInstance;
	
	protected AbstractTaskManager(String configJson, Class<? extends ConfigBase> configClass, Class<? extends AbstractProcessor> processorClass) {
		super(configJson, configClass);
		
		this.proccesorClass = processorClass;
		
	}

	public void start() throws SchedulerException {
		
		try {
			
			if (this.proccesorClass != null) {
				
				ConfigBase currentConfig = this.newConfig != null ? this.newConfig : this.config; 
				
				this.processorInstance = this.proccesorClass.getDeclaredConstructor(ConfigBase.class).newInstance(currentConfig);
				
				this.newConfig = null;
				
				this.processorInstance.run();
			}
			
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			
			log.error(e.getMessage());
			log.debug(ExceptionUtils.getStackTrace(e));
		}
	}
	
	public void stop() throws SchedulerException {
		
		if (this.processorInstance != null) {
			
			this.processorInstance.stop();
		}
	}
	
	public void runonce() throws SchedulerException {
		
	}
}
