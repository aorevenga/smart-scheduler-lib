package com.revenga.rits.smartscheduler.lib.service;

import java.util.Properties;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class EntitiesManager {
	
	private static final String PROP_QUARTZ_THREADPOOL_THREADCOUNT = "org.quartz.threadPool.threadCount"; 
	
	private static EntitiesManager instance;
	
	@Getter
	private Scheduler scheduler;

	public static EntitiesManager getInstance() {

		if (instance == null) {
			
			instance = new EntitiesManager();
		}

		return instance;
	}

	private EntitiesManager() {
		
	}
	
	public void initQuartzScheduler(Properties props) {

		try {
			
			Properties propsSf = new Properties();
			
			propsSf.setProperty(PROP_QUARTZ_THREADPOOL_THREADCOUNT,
					(String) props.get(PROP_QUARTZ_THREADPOOL_THREADCOUNT));
			
			SchedulerFactory sf = new StdSchedulerFactory(propsSf);
			this.scheduler = sf.getScheduler();

		} catch (SchedulerException e) {

			log.error(e.getMessage());
			log.debug(ExceptionUtils.getStackTrace(e));
		}
	}
}
