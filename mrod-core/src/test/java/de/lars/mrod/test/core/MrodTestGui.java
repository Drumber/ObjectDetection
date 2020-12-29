package de.lars.mrod.test.core;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
import de.lars.mrod.core.detection.ObjectDetector;
import de.lars.mrod.core.detection.YoloDetector;
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
		JLabel lblImg = new JLabel();
		
		ButtonGroup rbtnGroup = new ButtonGroup();
		JRadioButton rbtnObj = new JRadioButton("Haar Cascade");
		JRadioButton rbtnYolo = new JRadioButton("Yolo v3");
		rbtnGroup.add(rbtnObj);
		rbtnGroup.add(rbtnYolo);
		JPanel panelRbtns = new JPanel();
		panelRbtns.add(rbtnObj);
		panelRbtns.add(rbtnYolo);
		
		contentPane.add(lblImg, BorderLayout.CENTER);
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
				updateLabelIcon(lblImg, result.getFrame());
			}
		});
		DetectorExecutor objExecutor = core.registerDetector(objDetector, 100);
		
		
		//---
		// YOLO Object Detection
		URI fileYoloCfg = MrodTestGui.class.getClassLoader().getResource("yolo/yolov3.cfg").toURI();
		URI fileYoloWeights = MrodTestGui.class.getClassLoader().getResource("yolo/yolov3.weights").toURI();
		URI fileCocoNames = MrodTestGui.class.getClassLoader().getResource("yolo/coco.names").toURI();
		// read coco names
		List<String> cocoNames = readLinesFromFile(new File(fileCocoNames));
		
		YoloDetector yoloDetector = new YoloDetector(vidCam,
				new File(fileYoloCfg).getAbsolutePath(),
				new File(fileYoloWeights).getAbsolutePath());
		yoloDetector.setObjectNames(cocoNames);
		yoloDetector.addCallback(new ResultCallback<YoloDetectionResult>() {
			@Override
			public void onDetectionResult(YoloDetectionResult result) {
				updateLabelIcon(lblImg, result.getFrame());
			}
		});
		DetectorExecutor yoloExecutor = core.registerDetector(yoloDetector, 20);
		
		
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
		
		rbtnObj.setSelected(true); // set haar cascade detector as default
		objExecutor.run();
	}
	
	public static void updateLabelIcon(JLabel lbl, Mat frame) {
		SwingUtilities.invokeLater(() -> {
			try {
				lbl.setIcon(new ImageIcon(mat2Img(frame)));
			} catch (IOException e) {
				Logger.error(e, "Could not convert image.");
			}
		});
	}

	public static BufferedImage mat2Img(Mat mat) throws IOException {
		MatOfByte bytes = new MatOfByte();
		Imgcodecs.imencode(".png", mat, bytes);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes.toArray());
		return ImageIO.read(inputStream);
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
