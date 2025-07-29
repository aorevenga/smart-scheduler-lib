package com.revenga.rits.smartscheduler.lib.service;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;

import com.revenga.rits.smartscheduler.lib.model.ConfigBase;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractSchedulerManager extends AbstractManager {
	
	private Class<? extends Job> jobClass;
	
	@Getter
	@Setter
	private boolean running = false;
	private JobDetail job;
	
	@Setter
	@Getter
	private Thread runningThread;
	
	private Integer busyCounter;
	
	private static final AtomicLong idSequence = new AtomicLong(0);
	
	private Long idSeqValue;
	
	protected AbstractSchedulerManager(String configJson, Class<? extends ConfigBase> configClass, Class<? extends Job> jobClass) {
		super(configJson, configClass);
		
		this.jobClass = jobClass;
		this.busyCounter = 0;
	}
	
	public void start() throws SchedulerException {
		
		if (!StringUtils.isEmpty(this.config.getCronExpression())) {
			
			createJob();
			
			// All of the jobs have been added to the scheduler, but none of the jobs
			// will run until the scheduler has been started
			if (!EntitiesManager.getInstance().getScheduler().isStarted()) {

				EntitiesManager.getInstance().getScheduler().start();
			}
			
			log.info(this.jobClass.getSimpleName() + " => Started ");
		}
	}
	
	public void runonce() throws SchedulerException {
		
		if (this.job != null) {
			
			if (EntitiesManager.getInstance().getScheduler().isStarted()) {
				
				JobKey jobKey = this.job.getKey();
				
				if (jobKey != null) {
					
					EntitiesManager.getInstance().getScheduler().triggerJob(jobKey);
					
					log.info(this.jobClass.getSimpleName() + " => Fired ");
				}
			}
			else {
				
				log.warn(this.getClass().getSimpleName() + " => Job not exists");
			}
		}
	}
	
	public void stop() throws SchedulerException {

		removeJob();

		if (log.isInfoEnabled()) {
			
			log.info(this.jobClass.getSimpleName() + " => Stopped");
		}
		else {
			
			System.out.println(this.jobClass.getSimpleName() + " => Stopped");
		}
	}
	
	public Integer incBusyCounter() {
		
		return ++this.busyCounter;
	}
	
	public void resetBusyCounter() {
		
		this.busyCounter = 0;
	}

	private void createJob() {

		try {

			String cronExpression = this.config.getCronExpression();

			if (!StringUtils.isEmpty(cronExpression)) {
				
				this.idSeqValue = idSequence.incrementAndGet();
				
				this.job = newJob(this.jobClass).withIdentity("job" + this.jobClass.getSimpleName() + this.idSeqValue,
						"group" + this.jobClass.getSimpleName()).build();

				CronTrigger trigger = newTrigger().withIdentity("trigger" + jobClass.getSimpleName() + this.idSeqValue,
						"group" + jobClass.getSimpleName()).withSchedule(cronSchedule(cronExpression)).build();
				
				Date dt = EntitiesManager.getInstance().getScheduler().scheduleJob(this.job, trigger);

				EntitiesManager.getInstance().getScheduler().getContext().put(
						"group" + this.jobClass.getSimpleName() + "." + "job" + this.jobClass.getSimpleName() + this.idSeqValue,
						this);
				
				log.debug(
						this.getClass().getSimpleName() + " => " + this.job.getKey() + " has been scheduled to run at: "
								+ dt + " and repeat based on expression: " + trigger.getCronExpression());
			} else {

				log.debug(this.getClass().getSimpleName() + " => " + this.job.getKey() + " is not scheduled");
			}

		} catch (SchedulerException e) {

			log.error(e.getMessage());
			log.debug(ExceptionUtils.getStackTrace(e));
		}
	}
	
	public void killRunningJob() {
		
		if (this.job != null && this.isRunning() && this.runningThread != null) {
			
			runningThread.interrupt();
			
			log.warn(String.format("Thread %s has been interrupted due to too many retries",
					this.runningThread.getName()));
			
			this.runningThread = null;
			this.resetBusyCounter();
			this.setRunning(false);
		}
	}
	
	private void removeJob() {

		try {
			
			if (this.job != null) {

				JobKey jobKey = this.job.getKey();

				if (jobKey != null && EntitiesManager.getInstance().getScheduler().deleteJob(jobKey)
						&& EntitiesManager.getInstance().getScheduler().getContext()
								.remove("group" + this.jobClass.getSimpleName() + "." + "job"
										+ this.jobClass.getSimpleName() + this.idSeqValue) != null) {

					log.debug(this.getClass().getSimpleName() + " => " + this.job.getKey() + " has been stopped ");

					this.job = null;

				}
			} else {

				log.warn(this.getClass().getSimpleName() + " => Job not exists");
			}

		} catch (SchedulerException e) {

			log.error(e.getMessage());
			log.debug(ExceptionUtils.getStackTrace(e));
		}
	}
}
