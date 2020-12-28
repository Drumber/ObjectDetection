package de.lars.mrod.test.core;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;
import org.tinylog.Logger;

import de.lars.mrod.core.DetectorExecutor;
import de.lars.mrod.core.MrodCore;
import de.lars.mrod.core.detection.ObjectDetector;
import de.lars.mrod.core.detection.ResultCallback;
import de.lars.mrod.core.resource.VideoCaptureResource;

public class MrodTestGui {

	public static void main(String[] args) throws URISyntaxException {
		MrodCore core = new MrodCore();
		core.initialize();
		
		// GUI
		JFrame frame = new JFrame("MROD Test Gui");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(800, 600));
		
		JPanel contentPane = new JPanel(new BorderLayout());
		JLabel lblImg = new JLabel();
		contentPane.add(lblImg, BorderLayout.CENTER);
		frame.setContentPane(contentPane);
		frame.pack();
		frame.setVisible(true);
		
		// example models from https://github.com/opencv/opencv/tree/master/data/haarcascades
		URL fileFace = MrodTestGui.class.getClassLoader().getResource("haarcascades/haarcascade_frontalface_alt.xml");
		URL fileBody = MrodTestGui.class.getClassLoader().getResource("haarcascades/haarcascade_fullbody.xml");
		
		// Object Detection
		CascadeClassifier classifierFace = new CascadeClassifier();
		if(!classifierFace.load(new File(fileFace.toURI()).getAbsolutePath())) {
			Logger.error("Could not load cascade classifier file: " + fileFace);
		}
		CascadeClassifier classifierBody = new CascadeClassifier();
		if(!classifierBody.load(new File(fileBody.toURI()).getAbsolutePath())) {
			Logger.error("Could not load cascade classifier file: " + "haarcascade_fullbody.xml");
		}
		
		VideoCaptureResource vidCam = new VideoCaptureResource(0);
		ObjectDetector detector = new ObjectDetector(vidCam, classifierFace, classifierBody);
		detector.addCallback(new ResultCallback() {
			@Override
			public void onDetectionResult(Mat frame, MatOfRect[] detectedObjects) {
				SwingUtilities.invokeLater(() -> {
					try {
						lblImg.setIcon(new ImageIcon(mat2Img(frame)));
					} catch (IOException e) {
						Logger.error(e, "Could not convert image.");
					}
				});
			}
		});
		
		DetectorExecutor executor = core.registerDetector(detector, 100);
		executor.run();
	}

	public static BufferedImage mat2Img(Mat mat) throws IOException {
		MatOfByte bytes = new MatOfByte();
		Imgcodecs.imencode(".png", mat, bytes);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes.toArray());
		return ImageIO.read(inputStream);
	}

}
