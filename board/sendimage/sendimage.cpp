/*
sendimage.cpp
Author: David Turner

This program is given any number of jpeg images by the motion capture software,
 and determines the least blurry image. The program attempts to connect to the server at the given IP, and sends the selected image
*/

#include "opencv2/core/core.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/highgui/highgui.hpp"

#include <iostream>
#include <math.h>
#include <string.h>
#include <stdio.h>
#include <float.h>
#include <sys/types.h>
#include <sys/stat.h>
#include "netcode/cyprus_client.h"

using namespace cv;
using namespace std;

double calcEntropy(Mat &input);

int main(int argc, char** argv)
{
	vector<Mat> images;
	int i, index, sockfd;
	double entropy = DBL_MAX, current;
	char addr[20] = "192.168.42.142";

	//Try to connect to server
	sockfd = tcp_open(addr, 32000);

	if(argc < 2)
	{
		cout << "Invalid arguments." <<  endl;
		return -1;
	}
	else
	{
		for (i = 1; i < argc; i++)
		{
			images.push_back(imread(argv[i]));

			if (!images[i - 1].data)
			{
				cout << "Image not found." << endl;
				printf("%s\n", argv[i]);
				return -1;
			}

			current = calcEntropy(images[i - 1]);

			if (current < entropy)
			{
				entropy = current;
				index = i;
			}
		}
	}

	fprintf(stderr, "Sending %s\n", argv[index]);

	//Send image to server
	sendImageByteArray(argv[index], sockfd, false);

	tcp_close(sockfd);

	//Tell motion processing that sending is complete
	(void)system("/usr/local/share/MotionHandler/send_image_processing_done.sh");
	return 0;
}

//Determine entropy of image
double calcEntropy(Mat &input)
{
	int i, j;
	double entropy = 0, value;
	Mat image;

	cvtColor(input, image, CV_RGB2GRAY);

	equalizeHist(image, image);

	for(i = 0; i < image.rows; i++)
	{
		for (j = 0; j < image.cols; j++)
		{
			value = image.at<double>(i,j);

			if (value > 0 && value < 255)
				entropy += (value * log(value));
		}
	}
	
	return entropy;
}
