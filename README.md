## How to setup
Requires [Java](https://www.java.com) version 8 or higher and [Maven](https://maven.apache.org/).

1. Clone repository:  
`git clone https://github.com/Drumber/ObjectDetection.git`

2. Download yolov3 weights file from [here](https://pjreddie.com/media/files/yolov3.weights)

3. Copy `yolov3.weights` file to `MROD/mrod-core/src/test/resources/yolo/`

4. Open console and run the command `mvn install` to download project dependencies

## How to run
1. Import the maven project into your IDE (Eclipse)

2. Run the `MrodTestGui.java` class located in `MROD/mrod-core/src/test/java/de/lars/mrod/test/core`

3. The first available webcam is used as the video source

4. You can select the Haar Cascade (only face and body detection) or Yolo v3 algorithm