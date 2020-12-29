package de.lars.mrod.core.detection.result;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;

public class ObjectDetectionResult extends DetectionResult {
	
	private final MatOfRect[] detectedObjects;

	public ObjectDetectionResult(Mat frame, MatOfRect[] detectedObjects) {
		super(frame);
		this.detectedObjects = detectedObjects;
	}
	
	public MatOfRect[] getDetectedObjects() {
		return detectedObjects;
	}

}
