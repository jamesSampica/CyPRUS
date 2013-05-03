/**
 * \file motion_handler.cpp
 *
 * Michael Flagg
 * Senior Design May 13-17
 *
 * Handles motion information from the camera and PIR sensor.
 */

#include <fcntl.h>
#include <math.h>
#include <stdio.h>
#include <sys/stat.h>
#include <unistd.h>

#include <iostream>

#include "image_handler.h"
#include "motion_handler.h"
#include "print.h"

sem_t MotionHandler::m_motion_sem;

/**
 * Initializes motion detection threads.
 */
void MotionHandler::Init()
{
    (void)sem_init(&m_motion_sem, 0, 0);
    
    unlink("/usr/local/share/MotionHandler/image_ready_pipe");
    (void)mkfifo("/usr/local/share/MotionHandler/image_ready_pipe",
                 S_IWUSR | S_IRUSR);
}

/**
 * Waits for motion to be signalled by another thread.
 * Accepts signals from both the camera and PIR threads.
 */
void MotionHandler::MotionHandlerThread()
{
    Print::Blue("Starting motion handler waiting thread\n");
    while(true)
    {
        sem_wait(&m_motion_sem);
        ProcessMotion();
    }
}

/**
 * Signals the main thread when motion is detected by the camera.
 * Keeps a timestamp of the event.
 */
void MotionHandler::CameraThread()
{
    Print::Blue("Starting camera event thread\n");

    int pipe;
    unsigned char motion_found;

    while(true)
    {
        //Named pipe must be reopened every time or it will still
        //have the previous data
        //Always use open instead of fopen for named pipes to avoid deadlocks
        pipe = open("/usr/local/share/MotionHandler/image_ready_pipe", 
		    O_RDONLY);
        if(pipe < 0)
        {
            Print::Err("Camera event processing - Can't open pipe\n"
                     "       Terminating camera motion processing thread.\n");
            pthread_exit(NULL);
        }
        (void)read(pipe, &motion_found, 1);
        //(void)read(pipe, NULL, 3);
        close(pipe);

        if(motion_found == 0x01)
        {
            //Motion detected
            sem_post(&m_motion_sem);
        }
        else if(motion_found != 0x42)
        {
        //    Print::Err("Camera event processing - Received unexpected "
        //               "data from the pipe. Verify that only one instance "
	//	         "of this program is running.\n");
        }
    }
}


/**
 * Processes a motion event. Invoked indirectly by  the camera thread.
 */
void MotionHandler::ProcessMotion()
{
    Print::Blue("Processing motion event\n");
	ImageHandler::GrabImage();
}
