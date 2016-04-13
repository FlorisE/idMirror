import java.awt.Rectangle;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.Arrays;
import java.util.List;

ArrayList<String> paths = new ArrayList<String>();
ArrayList<String> pathsCopy = new ArrayList<String>();
ArrayList<PImage> images = new ArrayList<PImage>();
ArrayList<PImage> drawnImages = new ArrayList<PImage>();
int numPicsDrawn = 20;

int counterStart = 2416;

// hdmi mode 58

int opacity = 75;

int leftMargin = 120;
int topMargin = 90;

int horizontalCells = 6;
int verticalCells = 4;

int squareSides = 240;

int[][] gridLeft = new int[horizontalCells][verticalCells];
int[][] gridTop = new int[horizontalCells][verticalCells];

PFont astro;

String targetDirectory = "";

void setup() {
  fullScreen();
  println("starting");
  for (int i = 0; i < horizontalCells; i++) {
    for (int j = 0; j < verticalCells; j++) {
      gridLeft[i][j] = leftMargin + i * squareSides;
      gridTop[i][j]  = topMargin + j * squareSides;
    }
  }
  
  background(0);
  astro = loadFont("Astronaut-100.vlw");
  textFont(astro, 100);
  
  XML xml = loadXML("config.xml");
  targetDirectory = xml.getChild("pictures").getContent();
  counterStart = Integer.parseInt(xml.getChild("counterStart").getContent());
}

FilenameFilter getFilter() {
  return new FilenameFilter() {
    @Override
      public boolean accept(File dir, String name) {
      return name.endsWith("jpg");
    }
  };
}

List<File> loadImages() {
  // read folder to find pictures
  File pictureDirectory = new File(targetDirectory);
  File[] filteredFiles = pictureDirectory.listFiles (getFilter());
  Arrays.sort(filteredFiles);
  List<File> picturesAsArrayList = Arrays.asList(filteredFiles);
  Collections.reverse(picturesAsArrayList);
  
  if (picturesAsArrayList.size() < numPicsDrawn) {
    println("not enough pictures in the pictures folder");
    exit();
  } else {
    for (int i = 0; i < numPicsDrawn; i++) {
      println("loading picture");
      File picture = picturesAsArrayList.get(i);
      String path = picture.getAbsolutePath().replace("\\", "\\\\");
      
      // only load new pictures
      if (!paths.contains(path)) {
        paths.add(path);
        PImage image = loadImage(path);
        images.add(image);
      } else {
        break;
      }
    }
  }
  
  return picturesAsArrayList;
}

void updateMiniaturesArray() {
  if (drawnImages.size() == 0) {
    for (int i = images.size()-1; i >= images.size()-numPicsDrawn; i--) {
      drawnImages.add(images.get(i));
    }
  } else {
    for (int i = 0; i < images.size(); i++) {
      PImage newPicture = images.get(i);
      
      // move existing items
      for (int j = 0; j < drawnImages.size()-1; j++) {
        drawnImages.set(j, drawnImages.get(j+1));
      }
      // replace image
      drawnImages.set(drawnImages.size()-1, newPicture);
    }
  }
}

void drawBigPicture() {
 // draw the big images on top of each other
  for (int i = images.size()-1; i >= 0; i--) {
    PImage picture = images.get(i);

    tint(255, opacity);
    picture.resize(2 * squareSides - 10, 2 * squareSides - 10);
    image(picture, gridLeft[2][1]+5, gridTop[2][1]+5);
  } 
}

void drawMiniatures() {
  int miniaturesDrawn = 0;
  
  for (int i = horizontalCells - 1; i >= 0; i--) {
    for (int j = verticalCells - 1; j >= 0; j--) {
      if (i >= 2 && i <= 3 && j >= 1 && j <= 2) {
        continue;
      } 
      
      PImage picture = drawnImages.get(miniaturesDrawn);
      noTint();
      
      miniaturesDrawn++;
      picture.resize(230, 230);
      
      image(picture, gridLeft[i][j]+5, gridTop[i][j]+5, 230, 230);
    }
  }
}

void drawCounter(List<File> picturesAsArrayList) {
  int pictureCount = picturesAsArrayList.size();
  
  // counter background
  fill(0);
  strokeWeight(0);
  rect(0, 0, width, 90);
  
  // counter text
  fill(255);
  text(nf(counterStart + pictureCount, 4, 0), width/2-140, 60);
}

void draw() {
  List<File> picturesAsArrayList = loadImages();
  if (picturesAsArrayList.size() >= numPicsDrawn) {
    println("updating miniatures");
    updateMiniaturesArray();
    drawBigPicture();
    drawMiniatures();
    drawCounter(picturesAsArrayList);  
    
    images.clear();
  }
}

void delay(int delay) {
  int time = millis();
  while (millis () - time <= delay);
}