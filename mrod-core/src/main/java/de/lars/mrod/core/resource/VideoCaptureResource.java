package de.lars.mrod.core.resource;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class VideoCaptureResource implements Resource {
	
	private final VideoCapture videoCapture;
	
	public VideoCaptureResource(String fileName) {
		this();
		videoCapture.open(fileName);
	}
	
	public VideoCaptureResource(int camIndex) {
		this();
		videoCapture.open(camIndex);
	}
	
	private VideoCaptureResource() {
		this.videoCapture = new VideoCapture();
	}
	
	public boolean isOpened() {
		return videoCapture.isOpened();
	}

	@Override
	public Mat getResourceMat() {
		Mat mat = new Mat();
		// read single image and store it in matrix object
		videoCapture.read(mat);
		return mat;
	}

	public void close() {
		if(videoCapture.isOpened())
			videoCapture.release();
	}

}
