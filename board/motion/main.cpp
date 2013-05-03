/**
 * \file main.cpp
 *
 * Michael Flagg
 * Senior Design May 13-17
 *
 * Handles motion information from the camera and PIR sensor.
 */

#include "image_handler.h"
#include "motion_handler.h"
#include "print.h"

void *DequeueImages(void*);
void *WaitForMotion(void*);
void *WaitForCamEvent(void*);

/**
 * Starts motion detection and image capture
 *
 * \param[in] argc Number of arguments
 * \param[in] argv Arguments to the program (none used or required)
 */
int main(int argc, char *argv[])
{
    Print::Blue("\n*Initializing*\n");

    pthread_t dequeue_images_thread;
	pthread_t motion_handler_thread;
    pthread_t cam_thread;
	
    (void)pthread_create(&dequeue_images_thread, NULL, &DequeueImages, NULL);

	ImageHandler::Init();
    MotionHandler::Init();

    (void)pthread_create(&motion_handler_thread, NULL, &WaitForMotion, NULL);
    (void)pthread_create(&cam_thread, NULL, &WaitForCamEvent, NULL);

    pthread_join(motion_handler_thread, NULL);
    pthread_join(cam_thread, NULL);
    pthread_join(dequeue_images_thread, NULL);
}

void *DequeueImages(void*)
{
    ImageHandler::DequeueImagesThread();

    return 0;
}

void *WaitForMotion(void*)
{
    MotionHandler::MotionHandlerThread();

    return 0;
}

void *WaitForCamEvent(void*)
{
    MotionHandler::CameraThread();

    return 0;
}

