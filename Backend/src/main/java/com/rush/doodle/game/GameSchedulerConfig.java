package com.rush.doodle.game;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class GameSchedulerConfig {
	@Bean
	TaskScheduler taskScheduler() {
	    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
	    scheduler.setPoolSize(10); // adjust based on concurrency
	    scheduler.setThreadNamePrefix("scheduled-task-");
	    scheduler.initialize();
	    return scheduler;
	}

}
