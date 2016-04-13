# idMirror
An artistic installation featuring an Android application which acts like an unusual mirror to the viewer.

See our video on YouTube for an example of the full installation setting:
https://www.youtube.com/watch?v=mo1oXyKlGkM

# Android app
The Android application uses face detection on the front facing camera. Once a face is found a 20-seconds effect starts, in which the face is slowly modified into bits and pieces.

By default, after 2 seconds a picture will be taken and stored on the device. An FTP address can be configured to which the picture is uploaded. We use this for the idMirror projection. 

We tested the application primarily on the Google Nexus 9 tablet.

Some easy tinkering can be done in the tsukuba.emp.mirrorgl.util.Constants class:
- Setting the amount of rows/columns
- Changing the time for fade in/fade out
- Changing the total interaction time
- Changing the time at which a picture is taken
- Switching between rectangle and ellipse shape

# Projection

We have developed a Processing application which displays all the pictures taken. It can be found in the Processing folder. Change the projection tag in the config.xml to point to the folder containing the pictures send by the Android application. To be able to start the projection there should be enough pictures available to fill the screen. By default this means 20 pictures have to be available.

The FTP server has to be configured to allow anonymous uploading. On Windows we recommend to use babyftp (http://www.pablosoftwaresolutions.com/html/baby_ftp_server.html). On Raspbian running on Raspberry PI 3 Model B we have tested the installation with vsftpd.

There is currently a bug which makes the application unable to connect to the FTP server if the server displays a welcome message. This should thus be disabled.

Note that this functionality was added with an artistic installation in mind. Because FTP access and uploading is anonymous we do not recommend using an internet connection but rather a password protected local network.
