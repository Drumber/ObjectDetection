package de.lars.mrod.core.detection.result;

public interface ResultCallback<T> {
	
	/**
	 * Get the results from detector processing.
	 * @param frame	image
	 * @param detectedObjects MatOfRect for each registered classifier
	 */
	public void onDetectionResult(T result);

}
