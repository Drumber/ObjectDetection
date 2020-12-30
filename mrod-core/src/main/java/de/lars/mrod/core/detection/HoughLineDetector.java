package de.lars.mrod.core.detection;

import java.awt.Color;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.tinylog.Logger;

import de.lars.mrod.core.detection.result.DetectionResult;
import de.lars.mrod.core.resource.Resource;

public class HoughLineDetector extends AbstractDetector<DetectionResult> {
	
	private final Resource resource;
	private float threshold1 = 100f;
	private float threshold2 = 200f;
	private int thresholdAccu = 150; // accumulator threshold for HoughLines Transform
	private int minLineLength = 90;
	private int maxLineGap = 5;

	public HoughLineDetector(Resource resource) {
		this.resource = resource;
	}


	@Override
	public boolean process() {
		// get single frame from resource
		Mat frame = resource.getResourceMat();
		if (frame == null || frame.empty()) {
			Logger.error("Received invalid/empty frame.");
			return false;
		}
				
		// output variables
		Mat dst = new Mat();  // output edges
		Mat cdst = new Mat();
		Mat cdstP;
		
		// perform edge detection
		Imgproc.Canny(frame, dst, threshold1, threshold2);
		
		// copy edges to the image that will display the results in BGR
		Imgproc.cvtColor(dst, cdst, Imgproc.COLOR_GRAY2BGR);
		cdstP = cdst.clone();
		
		// Hought Line Transform
		Mat lines = new Mat(); // result of the detection
		Imgproc.HoughLines(dst, lines, 1, Math.PI / 180, thresholdAccu);
		
		// draw the lines
		for(int x = 0; x < lines.rows(); x++) {
			double rho = lines.get(x, 0)[0];
			double theta = lines.get(x, 0)[1];
			
			double a = Math.cos(theta);
			double b = Math.sin(theta);
			double x0 = a * rho;
			double y0 = b * rho;
			Point pt1 = new Point(Math.round(x0 + 1000*(-b)), Math.round(y0 + 1000*(a)));
			Point pt2 = new Point(Math.round(x0 - 1000*(-b)), Math.round(y0 - 1000*(a)));
			// draw line in image
			Color c = new Color(255, 0, 0);
			Imgproc.line(cdst, pt1, pt2, new Scalar(c.getBlue(), c.getGreen(), c.getRed()), 3, Imgproc.LINE_AA, 0);
		}
		
		// Probabilistic Line Transform
		Mat linesP = new Mat(); // result of the detection
		Imgproc.HoughLinesP(dst, linesP, 1, Math.PI / 180, 50, minLineLength, maxLineGap);
		
		// draw the lines
		for(int x = 0; x < linesP.rows(); x++) {
			double[] l = linesP.get(x, 0);
			Point pt1 = new Point(l[0], l[1]);
			Point pt2 = new Point(l[2], l[3]);
			// draw line in image
			Color c = new Color(0, 0, 255);
			Imgproc.line(cdstP, pt1, pt2, new Scalar(c.getBlue(), c.getGreen(), c.getRed()), 3, Imgproc.LINE_AA, 0);
		}
		
		//triggerCallbacks(new DetectionResult(cdst));
		triggerCallbacks(new DetectionResult(cdstP));
		return true;
	}

}
