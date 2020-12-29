package de.lars.mrod.core.detection;

import java.util.ArrayList;
import java.util.List;

import de.lars.mrod.core.detection.result.DetectionResult;
import de.lars.mrod.core.detection.result.ResultCallback;

public abstract class AbstractDetector<T extends DetectionResult> {
	
	protected final List<ResultCallback<T>> callbacks = new ArrayList<ResultCallback<T>>();
	
	public abstract boolean process();
	
	public void addCallback(ResultCallback<T> callback) {
		callbacks.add(callback);
	}
	
	public void removeCallback(ResultCallback<T> callback) {
		callbacks.remove(callback);
	}
	
	protected void triggerCallbacks(T result) {
		for (ResultCallback<T> rc : callbacks) {
			if (rc != null) {
				rc.onDetectionResult(result);
			}
		}
	}

}
