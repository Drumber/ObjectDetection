package de.lars.mrod.core.detection;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.tinylog.Logger;

import de.lars.mrod.core.detection.result.ObjectDetectionResult;
import de.lars.mrod.core.resource.Resource;

public class ObjectDetector extends AbstractDetector<ObjectDetectionResult> {
	
	private final Resource resource;
	private final List<CascadeClassifier> listClassifier;
	
	public ObjectDetector(Resource resource, CascadeClassifier... classifier) {
		this.resource = resource;
		listClassifier = new ArrayList<CascadeClassifier>();
		addClassifier(classifier);
	}
	
	public void addClassifier(CascadeClassifier... classifier) {
		for(CascadeClassifier cc : classifier) {
			listClassifier.add(cc);
		}
	}
	
	/**
	 * Process image from specified resource.
	 * @return	false if resource is closed/empty,
	 * 			true otherwise
	 */
	public boolean process() {
		// get single frame from resource
		Mat frame = resource.getResourceMat();
		if(frame == null || frame.empty()) {
			Logger.error("Received invalid/empty frame.");
			return false;
		}
		
        Mat frameGray = new Mat();
        Imgproc.cvtColor(frame, frameGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(frameGray, frameGray);
        MatOfRect[] detectedObjects = new MatOfRect[listClassifier.size()];
		
        // detect objects
        for(int i = 0; i < listClassifier.size(); i++) {
        	CascadeClassifier classifier = listClassifier.get(i);
        	detectedObjects[i] = new MatOfRect();
        	classifier.detectMultiScale(frameGray, detectedObjects[i]);
        	
        	//--- Render ---//
            List<Rect> listOfObjects = detectedObjects[i].toList();
            // draw rectangle
            for(Rect obj : listOfObjects) {
            	Color c = Color.getHSBColor(1.0f / listClassifier.size() * i, 1f, 1f);
            	Imgproc.rectangle(frame, obj, new Scalar(c.getRed(), c.getGreen(), c.getBlue()), 2);
            }
        }
        
        // trigger callbacks
        triggerCallbacks(new ObjectDetectionResult(frame, detectedObjects));
        return true;
	}

}
