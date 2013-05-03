/**
 * \file image_capture.cpp
 *
 * Michael Flagg
 * Senior Design May 13-17
 *
 * Handles handing an image off to the processing step
 */

#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <unistd.h>

#include <algorithm>
#include <ctime>

#include "image_handler.h"
#include "print.h"

///Set to the location of the "localize" executable
#define LOCALIZE_EXECUTABLE "/home/ubuntu/sendimage/sendimage"

///The directory the images are stored in by "motion"
///Trailing '/' required
#define IMAGE_DIR "/mnt/ramdisk/"

///Mutex to protect the photo queue
static pthread_mutex_t image_queue_lock = PTHREAD_MUTEX_INITIALIZER;

std::queue<std::string> ImageHandler::image_queue;

/**
 * Initializes image capture. Starts with a pipeline best suited to streaming
 * video.
 */
void ImageHandler::Init()
{
    unlink("/usr/local/share/MotionHandler/processing_done_pipe");
    (void)mkfifo("/usr/local/share/MotionHandler/processing_done_pipe",
                 S_IWUSR | S_IRUSR);

    std::string jpg_str, cmd;
    cmd = std::string("rm ") + std::string(IMAGE_DIR) + 
          std::string("*");

    (void)system(cmd.c_str());
}

/**
 * Grabs the most recent image written by "motion".
 */
void ImageHandler::GrabImage()
{
    std::string jpg_str, cmd;
    cmd = std::string("ls -1rt ") + std::string(IMAGE_DIR) + 
          std::string(" |tail -1");


    //Yes, it's probably better to use readdir etc. instead of ls
    jpg_str = std::string(IMAGE_DIR) + GetStdoutFromCommand(cmd);

    //TODO: Optimize me
    jpg_str.erase(std::remove(jpg_str.begin(), jpg_str.end(), '\n'), jpg_str.end());

    (void)pthread_mutex_lock(&image_queue_lock);
    image_queue.push(jpg_str);

    //If this is the only thing in the queue, go ahead and send the image
    //for processing
    if(image_queue.size() == 1)
    {
        ProcessImage(image_queue.front());
    }
    else
    {
        Print::Blue("Another image is in the queue. Deferring processing.\n");
    }
    (void)pthread_mutex_unlock(&image_queue_lock);
}

/**
 * Dequeues images upon notification that processing is done. If there are
 * additional images in the queue, sends the next one for processing.
 */
void ImageHandler::DequeueImagesThread()
{
    Print::Blue("Starting image dequeue thread\n");

    int pipe;
    unsigned char processing_done;

    while(true)
    {
        //Named pipe must be reopened every time or it will still
        //have the previous data
        //Always use open instead of fopen for named pipes to avoid deadlocks
        pipe = open("/usr/local/share/MotionHandler/processing_done_pipe",
                    O_RDONLY);
        if(pipe < 0)
        {
            Print::Err("Image dequeue - Can't open pipe\n"
                       "       Terminating image dequeue thread.\n");
            pthread_exit(NULL);
        }
        (void)read(pipe, &processing_done, 1);
        (void)read(pipe, NULL, 3);
        close(pipe);

        if(processing_done == 0x01)
        {
            if(image_queue.size() == 0)
            {
                Print::Err("Image dequeue - No images in queue\n");
                continue;
            }

            Print::Blue("Processing done. Popping the image queue.\n");

            DeleteImage(image_queue.front());

            //Image processing is finished.
            //Pop the queue and send off the next image
            (void)pthread_mutex_lock(&image_queue_lock);
            image_queue.pop();

            if(image_queue.size() > 0)
            {
                Print::Blue("Another image is in the queue. Processing.\n");
                ProcessImage(image_queue.front());
            }
            (void)pthread_mutex_unlock(&image_queue_lock);
        }
        else if(processing_done != 0x42)
        {
        //    Print::Err("Image dequeue - Received unexpected "
        //               "data from the pipe. Verify that only one instance "
        //               "of this program is running.\n");
        }
    }
}

/**
 * Invokes image processing
 *
 * \param[in] filename The name of the image file to process
 */
void ImageHandler::ProcessImage(const std::string &filename)
{
    /* construct the names of the other two files.
     * yes, this is the wrong way of doing things. time is
     * short.*/
    std::string filename1, filename2;
    filename1 = filename;
    filename2 = filename;

    filename1.replace(filename.size() - 5, 1, "1");
    filename2.replace(filename.size() - 5, 1, "2");

    //filename is the one ending in -3, append the -1 and -2 files
    std::string localize_cmd = LOCALIZE_EXECUTABLE;
    localize_cmd += " ";
    localize_cmd += filename1;
    localize_cmd += " ";
    localize_cmd += filename2;
    localize_cmd += " ";
    localize_cmd += filename;

    std::string cmd_output = "executing command: ";
    cmd_output += localize_cmd;
    cmd_output += "\n";
    
    Print::Blue(cmd_output);

    (void)system(localize_cmd.c_str());
}

void ImageHandler::DeleteImage(const std::string &filename)
{
    /* construct the names of the other two files.
     * yes, this is the wrong way of doing things. time is
     * short.*/
    std::string filename1, filename2;
    filename1 = filename;
    filename2 = filename;

    filename1.replace(filename.size() - 5, 1, "1");
    filename2.replace(filename.size() - 5, 1, "2");

    //filename is the one ending in -3, append the -1 and -2 files
    std::string rm_cmd = "rm";
    rm_cmd += " ";
    rm_cmd += filename1;
    rm_cmd += " ";
    rm_cmd += filename2;
    rm_cmd += " ";
    rm_cmd += filename;

    std::string cmd_output = "executing command: ";
    cmd_output += rm_cmd;
    cmd_output += "\n";
    
    Print::Blue(cmd_output);

    (void)system(rm_cmd.c_str());
}



/**
 * Execute a command and get the result.
 *
 * Source: http://stackoverflow.com/a/3578548
 *	
 * @param   cmd - The system command to run.
 * @return  The string command line output of the command.
 */
std::string ImageHandler::GetStdoutFromCommand(std::string cmd) 
{
    std::string data;
    FILE * stream;
    const int max_buffer = 512;
    char buffer[max_buffer];
    cmd.append(" 2>&1"); // Do we want STDERR?

    stream = popen(cmd.c_str(), "r");
    if (stream) 
    {
        while (!feof(stream))
	{
            if (fgets(buffer, max_buffer, stream) != NULL) 
	    {
		data.append(buffer);
	    }
	}
		
        pclose(stream);
    }
	
    return data;
}
