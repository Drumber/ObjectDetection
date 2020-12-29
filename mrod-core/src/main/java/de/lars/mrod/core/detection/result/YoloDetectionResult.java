package de.lars.mrod.core.detection.result;

import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Rect2d;

public class YoloDetectionResult extends DetectionResult {

	private final List<Integer> clsIds;
    private final List<Float> confs;
    private final List<Rect2d> rects;
	
	public YoloDetectionResult(Mat frame, List<Rect2d> objects, List<Integer> classIds, List<Float> confidences) {
		super(frame);
		this.rects = objects;
		this.clsIds = classIds;
		this.confs = confidences;
	}

	public List<Integer> getClassIds() {
		return clsIds;
	}

	public List<Float> getConfidences() {
		return confs;
	}

	public List<Rect2d> getObjects() {
		return rects;
	}

}
