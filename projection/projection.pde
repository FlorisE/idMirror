import java.awt.Rectangle;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.Arrays;
import java.util.List;

int numPicsDrawn = 20;

File[] imagesArray = new File[numPicsDrawn];
int previousIndex = 0;

int counter = 0;

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

File pictureDirectory;
File archiveDirectory;

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
  String targetDirectoryPath = xml.getChild("pictures").getContent();
  String archiveDirectoryPath = xml.getChild("archive").getContent();
  pictureDirectory = new File(targetDirectoryPath);
  archiveDirectory = new File(archiveDirectoryPath);
  int numArchived = archiveDirectory.listFiles (getFilter()).length;
  counter = numArchived + Integer.parseInt(xml.getChild("counterStart").getContent());
}

FilenameFilter getFilter() {
  return new FilenameFilter() {
    @Override
      public boolean accept(File dir, String name) {
      return name.endsWith("jpg");
    }
  };
}

boolean loadImages() {
  // read folder to find pictures
  File[] filteredFiles = pictureDirectory.listFiles (getFilter());
  Arrays.sort(filteredFiles);
  List<File> picturesAsArrayList = Arrays.asList(filteredFiles);
  
  if (picturesAsArrayList.size() < numPicsDrawn) {
    println("not enough pictures in the pictures folder");
    exit();
    return false;
  } else {
    for (int i = 0; i < picturesAsArrayList.size(); i++) {
      println("loading picture");
      File picture = picturesAsArrayList.get(i);
      if (!Arrays.asList(imagesArray).contains(picture)) {
        addImage(picture);
      }
    }
  }
  
  return true;
}

void drawBigPicture() {
 // draw the big images on top of each other
  for (int i = 0; i < numPicsDrawn; i++) {
    PImage picture = loadImage(imagesArray[i].getAbsolutePath());

    tint(255, opacity);
    picture.resize(2 * squareSides - 10, 2 * squareSides - 10);
    image(picture, gridLeft[2][1]+5, gridTop[2][1]+5);
  } 
}

void drawMiniatures() {
  int miniaturesDrawn = 0;
  
  for (int i = horizontalCells - 1; i >= 0; i--) {
    for (int j = verticalCells - 1; j >= 0; j--) {
      // skip four cells in the center (for drawing the big picture)
      if (i >= 2 && i <= 3 && j >= 1 && j <= 2) {
        continue;
      } 
      
      PImage picture = loadImage(imagesArray[(miniaturesDrawn + previousIndex + 1) % numPicsDrawn].getAbsolutePath());
      noTint();
      
      miniaturesDrawn++;
      picture.resize(230, 230);
      
      image(picture, gridLeft[i][j]+5, gridTop[i][j]+5, 230, 230);
    }
  }
}

void drawCounter() {
  // counter background
  fill(0);
  strokeWeight(0);
  rect(0, 0, width, 90);
  
  // counter text
  fill(255);
  text(nf(counter, 4, 0), width/2-140, 60);
}

void draw() {
  boolean sufficientPictures = loadImages();
  if (sufficientPictures) {
    drawBigPicture();
    drawMiniatures();
    drawCounter();
  }
}

void addImage(File image) {
  previousIndex = (previousIndex + 1) % numPicsDrawn;
  
  if (imagesArray[previousIndex] != null) {
    println("Moving " + imagesArray[previousIndex].getName() + " to " + archiveDirectory.getAbsolutePath() + "\\" + imagesArray[previousIndex].getName());
    imagesArray[previousIndex].renameTo(new File(archiveDirectory.getAbsolutePath() + "\\" + imagesArray[previousIndex].getName()));
  }
  
  imagesArray[previousIndex] = image;
  counter++;
}

void delay(int delay) {
  int time = millis();
  while (millis () - time <= delay);
}