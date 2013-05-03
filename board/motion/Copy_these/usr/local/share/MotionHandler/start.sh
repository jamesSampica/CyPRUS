#!/bin/bash

#Start handling motion detection events
/usr/local/share/MotionHandler/process_motion &

#Configure the camera
/usr/local/share/MotionHandler/configure_camera.sh

#Start the program that detects motion from the webcam
motion &
