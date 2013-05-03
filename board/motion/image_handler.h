/**
 * \file image_handler.h
 *
 * Michael Flagg
 * Senior Design May 13-17
 *
 * Handles image capture and camera configuration
 */

#ifndef IMAGE_CAPTURE_H
#define IMAGE_CAPTURE_H

#include <pthread.h>

#include <queue>
#include <string>

class ImageHandler
{
public:
    static void Init();
    static void DequeueImagesThread();
    static void GrabImage();

private:
    static void ProcessImage(const std::string &filename);
    static void DeleteImage(const std::string &filename);
    static std::string GetStdoutFromCommand(std::string cmd);

    //Class Variables
    ///Queue of images to be sent for processing
    static std::queue<std::string> image_queue;
};

#endif //IMAGE_CAPTURE_H
