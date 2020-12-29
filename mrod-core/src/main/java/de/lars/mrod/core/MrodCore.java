package de.lars.mrod.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.tinylog.Logger;

import de.lars.mrod.core.detection.AbstractDetector;
import nu.pattern.OpenCV;

public class MrodCore {
	
	private boolean isInitialized;
	private List<DetectorExecutor> listExecutors;

	public MrodCore() {
		listExecutors = new ArrayList<DetectorExecutor>();
	}
	
	public void initialize() {
		if(isInitialized())
			throw new IllegalStateException("MROD is already initialized.");
		
		// load OpenCV library
		Logger.info("Loading OpenCV native library...");
		OpenCV.loadShared();
		isInitialized = true;
	}
	
	public boolean isInitialized() {
		return isInitialized;
	}
	
	public DetectorExecutor registerDetector(AbstractDetector<?> detector) {
		return registerDetector(detector, 100);
	}
	
	public DetectorExecutor registerDetector(AbstractDetector<?> detector, int delay) {
		DetectorExecutor executor = new DetectorExecutor(detector, delay);
		listExecutors.add(executor);
		return executor;
	}
	
	public void unregisterDetector(AbstractDetector<?> detector) {
		Iterator<DetectorExecutor> it = listExecutors.iterator();
		for(DetectorExecutor de = it.next(); it.hasNext(); de = it.next()) {
			if(de.getDetector().equals(detector)) {
				de.stop();
				it.remove();
				break;
			}
		}
	}
	
	public List<DetectorExecutor> getDetectorExecutors() {
		return listExecutors;
	}
	
	public void stopAllDetectors() {
		for(DetectorExecutor de : listExecutors) {
			de.stop();
		}
	}
	
}
