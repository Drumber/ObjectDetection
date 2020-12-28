package de.lars.mrod.core.detection;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;

public interface ResultCallback {
	
	/**
	 * Get the results from detector processing.
	 * @param frame	image
	 * @param detectedObjects MatOfRect for each registered classifier
	 */
	public void onDetectionResult(Mat frame, MatOfRect[] detectedObjects);

}
