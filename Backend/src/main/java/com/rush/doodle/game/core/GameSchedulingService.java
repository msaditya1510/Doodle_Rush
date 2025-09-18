package com.rush.doodle.game.core;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class GameSchedulingService {
    private final TaskScheduler taskScheduler;
    private final Map<String, ScheduledFuture<?>> roomTasks = new ConcurrentHashMap<>();

    // Constructor injection is clear and immutable
    public GameSchedulingService(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    public void scheduleTask(String roomId, Runnable task, long delayInSeconds) {
        cancelTask(roomId); // Always cancel previous task for the room first
        Instant endTime = Instant.now().plusSeconds(delayInSeconds);
        ScheduledFuture<?> future = taskScheduler.schedule(task, endTime);
        roomTasks.put(roomId, future);
    }

    public void cancelTask(String roomId) {
        ScheduledFuture<?> future = roomTasks.remove(roomId); // Remove from map first
        if (future != null) {
            future.cancel(false); // Prevent the task from running if it hasn't started
        }
    }

    public boolean isTaskScheduled(String roomId) {
        ScheduledFuture<?> future = roomTasks.get(roomId);
        return future != null && !future.isDone();
    }
    
    
}