package de.lars.mrod.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.tinylog.Logger;

import de.lars.mrod.core.detection.ObjectDetector;
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
	
	public DetectorExecutor registerDetector(ObjectDetector detector) {
		return registerDetector(detector, 100);
	}
	
	public DetectorExecutor registerDetector(ObjectDetector detector, int delay) {
		DetectorExecutor executor = new DetectorExecutor(detector, delay);
		listExecutors.add(executor);
		return executor;
	}
	
	public void unregisterDetector(ObjectDetector detector) {
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
	
}
