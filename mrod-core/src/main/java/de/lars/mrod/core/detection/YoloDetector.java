package de.lars.mrod.core.detection;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.*;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.tinylog.Logger;

import de.lars.mrod.core.detection.result.YoloDetectionResult;
import de.lars.mrod.core.resource.Resource;
import de.lars.mrod.core.utils.Stopwatch;

public class YoloDetector extends AbstractDetector<YoloDetectionResult> {
	
	private final Resource resource;
	private final Net net;
	private final Stopwatch stopWatch;
	private final List<String> outNames;
	private final List<Mat> outBlobs;
	private List<String> objNames;
	
	/**
	 * Create a new DarkNet Yolo object detector.
	 * @param resource		frame resource
	 * @param configFile	yolo configuration file ({@code *.cfg})
	 * @param modelWeights	yolo model weights ({@code *.weights})
	 */
	public YoloDetector(Resource resource, String configFile, String modelWeights) {
		this.resource = resource;
		stopWatch = new Stopwatch();
		net = Dnn.readNetFromDarknet(configFile, modelWeights);
		net.setPreferableBackend(Dnn.DNN_BACKEND_OPENCV);
		net.setPreferableTarget(Dnn.DNN_TARGET_OPENCL);
		
		outBlobs = new ArrayList<Mat>();
		outNames = net.getUnconnectedOutLayersNames();
		objNames = new ArrayList<String>();
		
		outNames.forEach(System.out::println);
	}
	
	public void setObjectNames(List<String> objNames) {
		this.objNames = objNames;
	}

	@Override
	public boolean process() {
		// get single frame from resource
		Mat frame = resource.getResourceMat();
		if (frame == null || frame.empty()) {
			Logger.error("Received invalid/empty frame.");
			return false;
		}
		stopWatch.reset();
		
		// create 4D blob from frame
		Mat blob = Dnn.blobFromImage(frame, 1.0 / 255, new Size(416, 416), new Scalar(0), true, false);
		
		// set input data
		net.setInput(blob);
		
		// compute data
		net.forward(outBlobs, outNames);
		
		System.out.printf("%nImage forwarding took %d ms.%n", stopWatch.getCurrentDuration());
		
		// process result data
		processResult(frame);
		return true;
	}
	
	private float confidenceThreshold = 0.1f;
	private float nmsThreshold = 0.3f; // Non-Maximal Suppression (only keep the best bounding box)
	private boolean skipNms = false;
	
	private void processResult(Mat frame) {
		List<Integer> clsIds = new ArrayList<>();
        List<Float> confs = new ArrayList<>();
        List<Rect2d> rects = new ArrayList<>();
        
		for(int i = 0; i < outBlobs.size(); i++) { // loop through bounding boxes
			Mat result = outBlobs.get(i);
			
			for(int j = 0; j < result.rows(); j++) { // loop through rows
				// first 4 columns: center_x, center_y, width, height
				Mat row = result.row(j);
				// columns after first 4: scores
				Mat scores = row.colRange(5, result.cols());
				// value and location of maximum score
				Core.MinMaxLocResult mm = Core.minMaxLoc(scores);
				float confidence = (float) mm.maxVal;
				Point clsIdLocPoint = mm.maxLoc;
				
				if(confidence > confidenceThreshold) {
					//scaling for drawing the bounding boxes
					int centerX = (int)(row.get(0,0)[0] * frame.cols());
                    int centerY = (int)(row.get(0,1)[0] * frame.rows());
                    int width   = (int)(row.get(0,2)[0] * frame.cols());
                    int height  = (int)(row.get(0,3)[0] * frame.rows());
                    int left    = centerX - width  / 2;
                    int top     = centerY - height / 2;

					clsIds.add((int) clsIdLocPoint.x);
					confs.add((float) confidence);
					rects.add(new Rect2d(left, top, width, height));
				}
			}
		}
		
		// skip when nothing was detected
		if(rects.size() < 1) {
			triggerCallbacks(new YoloDetectionResult(frame, rects, clsIds, confs));
			return;
		}
		
		if(skipNms) {
			for(int i = 0; i < rects.size(); i++) {
		    	processImage(frame, rects.get(i), i, clsIds.get(i), confs.get(i));
			}
			
			System.out.printf("Total processing took %d ms.%n", stopWatch.stop());
			triggerCallbacks(new YoloDetectionResult(frame, rects, clsIds, confs));
			return;
		}
		
		// NMS
		//MatOfFloat confidences = new MatOfFloat(Converters.vector_float_to_Mat(confs));
		float[] arrConfs = new float[confs.size()];
		for(int i = 0; i < arrConfs.length; i++)
			arrConfs[i] = confs.get(i);
		MatOfFloat confidences = new MatOfFloat(arrConfs);
		Rect2d[] arrBoxes = rects.toArray(new Rect2d[0]);
		MatOfRect2d boxes = new MatOfRect2d(arrBoxes);
		MatOfInt indices = new MatOfInt();
		Dnn.NMSBoxes(boxes, confidences, confidenceThreshold, nmsThreshold, indices);
		
		//--- Render ---//
		int checkVec = indices.checkVector(1, CvType.CV_32S);
		if(checkVec < 0) {
			Logger.warn("Invalid indices vector: " + checkVec);
			return;
		}
		int[] arrIndices = indices.toArray();
		for(int i = 0; i < arrIndices.length; i++) {
			int index = arrIndices[i];
			Rect2d box = arrBoxes[index];
	    	processImage(frame, box, i, clsIds.get(index), confs.get(index));
		}
		
		System.out.printf("Total processing took %d ms.%n", stopWatch.stop());
		// trigger callbacks
        triggerCallbacks(new YoloDetectionResult(frame, rects, clsIds, confs));
	}
	
	
	private void processImage(Mat frame, Rect2d box, int i, int classId, float confidence) {
		float hue = 1.0f / (objNames.size() / 10.0f) * classId;
		if(hue > 1.0f) hue -= 10.0f;
		Color c = Color.getHSBColor(hue, 1f, 1f);
		Scalar color = new Scalar(c.getRed(), c.getGreen(), c.getBlue());
		Rect rect = rect2dToRect(box);
    	Imgproc.rectangle(frame, rect, color, 2);
    	
    	String objName = classId < objNames.size() ? objNames.get(classId) : "<undefined>";
    	String text = String.format("%s [%d]: %f", objName, classId, confidence);
    	Imgproc.putText(frame, text, new Point(rect.x, rect.y - 5), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, color);
	}
	
	private Rect rect2dToRect(Rect2d rect2d) {
		return new Rect(new double[] {rect2d.x, rect2d.y, rect2d.width, rect2d.height});
	}

}
