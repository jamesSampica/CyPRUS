/**
 * \file motion_handler.h
 *
 * Michael Flagg
 * Senior Design May 13-17
 *
 * Handles motion information from the camera and PIR sensor.
 */

#ifndef MOTION_DETECTION_H
#define MOTION_DETECTION_H

#include <pthread.h>
#include <semaphore.h>

#include <ctime>
//#include <mutex>

class MotionHandler
{
public:
    static void Init();

    static void MotionHandlerThread();
    static void CameraThread();

private:
    static void ProcessMotion();

    //Class Variables
    ///Semaphore to signal the processing thread when motion is detected
    static sem_t m_motion_sem;
};

#endif //MOTION_DETECTION_H
