package de.lars.mrod.core.detection.result;

import org.opencv.core.Mat;

public class DetectionResult {
	
	private final Mat frame;
	
	public DetectionResult(Mat frame) {
		this.frame = frame;
	}

	public Mat getFrame() {
		return frame;
	}

}
