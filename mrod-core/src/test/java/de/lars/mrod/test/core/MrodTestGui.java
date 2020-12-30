package de.lars.mrod.test.core;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;
import org.tinylog.Logger;

import de.lars.mrod.core.DetectorExecutor;
import de.lars.mrod.core.MrodCore;
import de.lars.mrod.core.detection.HoughLineDetector;
import de.lars.mrod.core.detection.ObjectDetector;
import de.lars.mrod.core.detection.YoloDetector;
import de.lars.mrod.core.detection.result.DetectionResult;
import de.lars.mrod.core.detection.result.ObjectDetectionResult;
import de.lars.mrod.core.detection.result.ResultCallback;
import de.lars.mrod.core.detection.result.YoloDetectionResult;
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
		JPanel panelImg = new JPanel();
		
		ButtonGroup rbtnGroup = new ButtonGroup();
		JRadioButton rbtnObj = new JRadioButton("Haar Cascade");
		JRadioButton rbtnYolo = new JRadioButton("Yolo v3");
		JRadioButton rbtnHoughLines = new JRadioButton("Hough Lines Transform");
		rbtnGroup.add(rbtnObj);
		rbtnGroup.add(rbtnYolo);
		rbtnGroup.add(rbtnHoughLines);
		JPanel panelRbtns = new JPanel();
		panelRbtns.add(rbtnObj);
		panelRbtns.add(rbtnYolo);
		panelRbtns.add(rbtnHoughLines);
		
		contentPane.add(panelImg, BorderLayout.CENTER);
		contentPane.add(panelRbtns, BorderLayout.SOUTH);
		frame.setContentPane(contentPane);
		frame.pack();
		frame.setVisible(true);
		
		// web cam resource
		VideoCaptureResource vidCam = new VideoCaptureResource(0);
		
		
		// example models from https://github.com/opencv/opencv/tree/master/data/haarcascades
		URL fileFace = MrodTestGui.class.getClassLoader().getResource("haarcascades/haarcascade_frontalface_alt.xml");
		URL fileBody = MrodTestGui.class.getClassLoader().getResource("haarcascades/haarcascade_fullbody.xml");
		
		//---
		// Object Detection
		CascadeClassifier classifierFace = new CascadeClassifier();
		if(!classifierFace.load(new File(fileFace.toURI()).getAbsolutePath())) {
			Logger.error("Could not load cascade classifier file: " + fileFace);
		}
		CascadeClassifier classifierBody = new CascadeClassifier();
		if(!classifierBody.load(new File(fileBody.toURI()).getAbsolutePath())) {
			Logger.error("Could not load cascade classifier file: " + "haarcascade_fullbody.xml");
		}
		
		ObjectDetector objDetector = new ObjectDetector(vidCam, classifierFace, classifierBody);
		objDetector.addCallback(new ResultCallback<ObjectDetectionResult>() {
			@Override
			public void onDetectionResult(ObjectDetectionResult result) {
				updateLabelIcon(panelImg, result.getFrame());
			}
		});
		DetectorExecutor objExecutor = core.registerDetector(objDetector, 100);
		
		
		//---
		// YOLO Object Detection
		// yolov3
		URI fileYoloCfg = MrodTestGui.class.getClassLoader().getResource("yolo/yolov3.cfg").toURI();
		URI fileYoloWeights = MrodTestGui.class.getClassLoader().getResource("yolo/yolov3.weights").toURI();
		// tiny yolov3
		//URI fileYoloCfg = MrodTestGui.class.getClassLoader().getResource("yolo/yolov3-tiny.cfg").toURI();
		//URI fileYoloWeights = MrodTestGui.class.getClassLoader().getResource("yolo/yolov3-tiny.weights").toURI();
		// yolo openimagesdataset
		//URI fileYoloCfg = MrodTestGui.class.getClassLoader().getResource("yolo/yolov3-openimages.cfg").toURI();
		//URI fileYoloWeights = MrodTestGui.class.getClassLoader().getResource("yolo/yolov3-openimages.weights").toURI();
		//URI fileClassNames = MrodTestGui.class.getClassLoader().getResource("yolo/openimages.names").toURI();
		
		URI fileClassNames = MrodTestGui.class.getClassLoader().getResource("yolo/coco.names").toURI();
		// read class names
		List<String> classNames = readLinesFromFile(new File(fileClassNames));
		
		YoloDetector yoloDetector = new YoloDetector(vidCam,
				new File(fileYoloCfg).getAbsolutePath(),
				new File(fileYoloWeights).getAbsolutePath());
		yoloDetector.setObjectNames(classNames);
		yoloDetector.addCallback(new ResultCallback<YoloDetectionResult>() {
			@Override
			public void onDetectionResult(YoloDetectionResult result) {
				updateLabelIcon(panelImg, result.getFrame());
			}
		});
		DetectorExecutor yoloExecutor = core.registerDetector(yoloDetector, 20);
		
		
		//--
		// Hough Lines Transform
		HoughLineDetector houghLineDetector = new HoughLineDetector(vidCam);
		houghLineDetector.addCallback(new ResultCallback<DetectionResult>() {
			@Override
			public void onDetectionResult(DetectionResult result) {
				updateLabelIcon(panelImg, result.getFrame());
			}
		});
		DetectorExecutor houghLineExecutor = core.registerDetector(houghLineDetector, 20);
		
		//---
		// Radio Buttons
		rbtnObj.addActionListener(l -> {
			if(!objExecutor.isRunning()) {
				core.stopAllDetectors();
				objExecutor.run();
			}
		});
		rbtnYolo.addActionListener(l -> {
			if(!yoloExecutor.isRunning()) {
				core.stopAllDetectors();
				yoloExecutor.run();
			}
		});
		rbtnHoughLines.addActionListener(l -> {
			if(!houghLineExecutor.isRunning()) {
				core.stopAllDetectors();
				houghLineExecutor.run();
			}
		});
		
		rbtnYolo.setSelected(true); // set yolo detector as default
		yoloExecutor.run();
	}
	
	public static void updateLabelIcon(JPanel panel, Mat frame) {
		SwingUtilities.invokeLater(() -> {
			try {
				BufferedImage bi = mat2Img(frame);
				Graphics g = panel.getGraphics();
				g.drawImage(bi, 0, 0, panel);
				g.dispose();
			} catch (IOException e) {
				Logger.error(e, "Could not convert image.");
			}
		});
	}

	public static BufferedImage mat2Img(Mat mat) throws IOException {
		MatOfByte bytes = new MatOfByte();
		Imgcodecs.imencode(".png", mat, bytes);
		try(ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes.toArray())) {
			BufferedImage image = ImageIO.read(inputStream);
			return image;
		}
	}
	
	public static List<String> readLinesFromFile(File file) {
		List<String> list = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			for(String line; (line = br.readLine()) != null;) {
				list.add(line);
			}
		} catch (FileNotFoundException e) {
			Logger.error(e, "File not found: " + file.getAbsolutePath());
		} catch (IOException e) {
			Logger.error("Error while reading file.");
		}
		return list;
	}

}
