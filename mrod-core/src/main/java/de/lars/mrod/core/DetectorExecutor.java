package de.lars.mrod.core;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.tinylog.Logger;

import de.lars.mrod.core.detection.ObjectDetector;

public class DetectorExecutor {
	
	private ScheduledExecutorService executor;
	private final ObjectDetector detector;
	private boolean running;
	private int loopDelay;
	
	public DetectorExecutor(ObjectDetector detector) {
		this(detector, 100);
	}
	
	public DetectorExecutor(ObjectDetector detector, int delay) {
		this.detector = detector;
		this.loopDelay = delay;
	}
	
	public ObjectDetector getDetector() {
		return detector;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public void run() {
		if(isRunning())
			return;
		
		executor = Executors.newScheduledThreadPool(1);
		Logger.info("Starting executor for '" + detector.toString() + "'.");
		executor.scheduleAtFixedRate(() -> {
			boolean success = detector.process();
			if(!success) {
				stop();
			}
		}, 0, loopDelay, TimeUnit.MILLISECONDS);
	}
	
	public void stop() {
		running = false;
		if(executor != null && !executor.isShutdown()) {
			Logger.info("Stopping executor of '" + detector.toString() + "'.");
			executor.shutdown();
		}
	}

}
