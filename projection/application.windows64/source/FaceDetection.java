import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import gab.opencv.*; 
import java.awt.Rectangle; 
import java.io.File; 
import java.io.FilenameFilter; 
import java.util.Collections; 
import java.util.Arrays; 
import java.util.List; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class FaceDetection extends PApplet {









OpenCV opencv;
Rectangle[] faces;
ArrayList<String> paths = new ArrayList<String>();
ArrayList<String> pathsCopy = new ArrayList<String>();
ArrayList<PImage> images = new ArrayList<PImage>();
int pictureCount = 0;
int numPicsDrawn = 4;

int screenwidth = 768;
int screenheight = 1040;

int opacity = 100;

PFont astro;

public void setup() {
  size(screenwidth, screenheight);
  astro = loadFont("Astronaut-100.vlw");
  textFont(astro, 100);
  
  //  color(0, 0, 0);
  //rect(0, 0, screenwidth, screenheight);
}

public void draw() {
  
  // load pictures from folder
  File pictureDirectory = new File("C:\\idMirrorPictures");

  FilenameFilter filter = new FilenameFilter() {
    @Override
      public boolean accept(File dir, String name) {
      return name.endsWith("jpg");
    }
  };
  
  List<File> picturesAsArrayList = Arrays.asList(pictureDirectory.listFiles (filter));
  Collections.reverse(picturesAsArrayList);
  
  // detect upto 4 faces in the pictures
  int facesDetected = 0;
  for (File picture : picturesAsArrayList) {
    String path = picture.getAbsolutePath().replace("\\", "\\\\");
        
    // only load new pictures
    if (!paths.contains(path)) {
      paths.add(path);
      opencv = new OpenCV(this, path);
      opencv.loadCascade(OpenCV.CASCADE_FRONTALFACE);
      faces = opencv.detect();

      if (faces.length == 1) {
        facesDetected++;
        int image1x = faces[0].x-25;
        int image1y = faces[0].y-25;
        int image1width = faces[0].width+50;
        int image1height = faces[0].height+50;

        PImage image = opencv.getInput();
        image = image.get(image1x, image1y, image1width, image1height);
        images.add(image);
      }
    } else {
      break;
    }
    
    if (facesDetected == 4) {
      break;
    }
  }

  for (int i = 0; i < images.size (); i++) {
    println("drawing");
    PImage picture = images.get(i);

    tint(255, opacity);
    picture.resize(768, 768);
    image(picture, 0, 80);

    if (i >= images.size() - numPicsDrawn) {
      noTint();

      picture.resize(188, 188);
      
      image(picture, i % 4 * (screenwidth / 4) + 2, screenheight - 192 + 2);
    }
  }

  images.clear();
  
  int pictureCount = picturesAsArrayList.size();

  // draw the counter
  fill(255);
  text(nf(pictureCount, 4, 0), screenwidth/2-100, 60);

  // refresh every second
  delay(1000);
}

public void delay(int delay)
{
  int time = millis();
  while (millis () - time <= delay);
}

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--full-screen", "--bgcolor=#050101", "--hide-stop", "FaceDetection" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
