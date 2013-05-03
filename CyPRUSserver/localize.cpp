/*
localize.cpp
Author: David Turner

This program take a jpeg image and attempts to extract and OCR
the characters from a license plate. The characters are then 
stored as metadata in the given image.

This program will only run in a linux environment, and requires write
permissions to the working directory.
*/

#include "opencv2/core/core.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/highgui/highgui.hpp"

#include <iostream>
#include <string.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include "netcode/cyprus_client.h"

using namespace cv;
using namespace std;

/*Runs contour detection to find plates in image*/
static void findRects( const Mat& image, vector<vector<Point> >& squares );

/*Creates temporary images of most likely license plates*/
static void extractRects( Mat& image, IplImage *image2, const vector<vector<Point> >& squares, char *name );

/*Creates temporary images of characters from most likely plate*/
static void segmentChars(Mat &image, char *name, int random);

/*Calulates angle value for plate rectangle*/
static double getAngle( Point pt1, Point pt2, Point pt0 );

/*Compares horizontal position values of rectangle, for sorting characters*/
bool compareVect(RotatedRect x, RotatedRect y);

int thresh = 100, N = 11;

int main(int argc, char** argv)
{
	vector<vector<Point> > squares;

	Mat image;
	IplImage *image2;

        if(argc != 2)
        {
                cout << "Invalid arguments." <<  endl;
                return -1;
        }
        else
        {
		//Read image into two formats
                image = imread(argv[1]);
                image2 = cvLoadImage(argv[1], CV_LOAD_IMAGE_UNCHANGED);
        }

        if (!image.data)
        {
                cout << "Image not found." << endl;
                printf("%s\n", argv[1]);
                return -1;
        }

	//Run localization and segmentation
        findRects(image, squares);
        extractRects(image, image2, squares, argv[1]);

	return 0;
}


static void findRects( const Mat& image, vector<vector<Point> >& squares )
{
	squares.clear();

	Mat pyr, timg, gray0(image.size(), CV_8U), gray, canny_out, close;

	pyrDown(image, pyr, Size(image.cols/2, image.rows/2));
	pyrUp(pyr, timg, image.size());
	vector<vector<Point> > contours;

	//Run for each channel
	for( int c = 0; c < 3; c++ )
	{
		int ch[] = {c, 0};
		mixChannels(&timg, 1, &gray0, 1, ch, 1);

		for( int l = 0; l < N; l++ )
		{
			if( l == 0 )
			{
				Canny(gray0, gray, 0, thresh, 5);

				dilate(gray, gray, Mat(), Point(-1,-1));	
			}
			else
				gray = gray0 >= (l+1)*255/N;

			//Histogram image
			equalizeHist(gray, gray);

			//Detect contours
			findContours(gray, contours, CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);

			vector<Point> approx;

			//Run for each set of contours
			for( size_t i = 0; i < contours.size(); i++ )
			{
				//Get approximate contour regions
				approxPolyDP(Mat(contours[i]), approx, arcLength(Mat(contours[i]), true)*0.02, true);

				//Check if contour is a rectangle, is not concave, and is large enough
				if( approx.size() == 4 &&
				    fabs(contourArea(Mat(approx))) > 1000 &&
				    isContourConvex(Mat(approx)) )
				{
					double maxCosine = 0;

					for( int j = 2; j < 5; j++ )
					{
						double cosine = fabs(getAngle(approx[j%4], approx[j-2], approx[j-1]));
						maxCosine = MAX(maxCosine, cosine);
					}

					//If correct, store possible plate vector
					if( maxCosine < 0.3 )
						squares.push_back(approx);
				}
			}
		}
	}
}


static void extractRects( Mat& image, IplImage *image2, const vector<vector<Point> >& squares, char *name )
{
   
	Mat M, rotated, cropped, grey;
	char varName[200];
	int status, num = 0, random;
	float width, height;

	srand((unsigned)time(0)); 
   	random= (rand()%999999)+100000;

	sprintf(varName, "%d", random);

	//Create temp directory for images
	status = mkdir(varName, S_IRWXU);

	if (status != 0)
	{
		printf("Could not create directory.\n");
		return;
	}

	//Run for each possible plate
	for (size_t i = (squares.size() - 1); i >= 0; i--)
	{
		RotatedRect rect = minAreaRect(Mat(squares[i]));

		float angle = rect.angle;
		Size rect_size = rect.size;

		//If rectangle is sideways, swap width and height
		if (rect.angle < -45.)
		{
			angle += 90.0;
			swap(rect_size.width, rect_size.height);
		}

		width = rect_size.width;
		height = rect_size.height;
	
		//If longer than it is tall, and not the entire image
		if (width > height && width < (image.cols * 0.9))
		{
			num++;

			//Extract rectangle from image
			M = getRotationMatrix2D(rect.center, angle, 1.0);
			warpAffine(image, rotated, M, image.size(), INTER_CUBIC);
			getRectSubPix(rotated, rect_size, rect.center, cropped);

			Size size(3,3);

			//Grayscale and smooth
			cvtColor(cropped, grey, CV_RGB2GRAY);
			GaussianBlur(grey, grey, size, 0);

			//Write temporary plate image
			sprintf(varName, "%d/plate_%d.jpg", random, num);
			imwrite(varName, grey);


			//Check if plate is within ratio, may need to change depending on camera/distance
			if (((width / height) > 1.3) && ((width / height) < 2.4))
			{
				/*Depending on size, may need to resize*/
					//Mat temp(cropped.rows * 2, cropped.cols * 2, cropped.depth());
					//resize(cropped, temp, temp.size());

				//Run character segmentation
				segmentChars(cropped, name, random);
				return;
			}
		}
	}
}

static void segmentChars(Mat &image, char *name, int random)
{
	Mat M, rotated, cropped, gray, thresh, edge, canny_out, final;
	Size size(3,3);
	vector<vector<Point> > contours;
	vector<Point> approx;
	int num = 1, average = 0, partial;
	char varName[100], plate_num[12];
	RotatedRect rect;
	float angle, total_pixels, nonzero, prev_x;
	Size rect_size;

	FILE *pFile;

	//Grayscale and smooth
	cvtColor(image, gray, CV_RGB2GRAY);
	medianBlur(gray, gray, 5);

	//Apply filters to eliminate noise, highlight characters
      	Canny(gray, canny_out, 0, 100, 5);
	dilate(canny_out, thresh, Mat(), Point(-1,-1));
	erode(thresh, thresh, Mat(), Point(-1,-1));

	//Find contours of characters
	findContours(thresh, contours, CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE, cvPoint(0, 0));

	vector<RotatedRect> chars;

	//Run for each contour
	for( size_t i = 0; i < contours.size(); i++ )
	{
		//Get approximate countour regions
		approxPolyDP(Mat(contours[i]), approx, arcLength(Mat(contours[i]), true)*0.02, true);

		//If area is large enough
		if( fabs(contourArea(Mat(approx))) > 500)
                {
			//Get rectangle containing character from image
			rect = minAreaRect(Mat(approx));

			angle = rect.angle;
    			rect_size = rect.size;

			//If rectangle is sideways, swap width and height
			if (rect.angle < -45.)
    			{
				angle += 90.0;
				swap(rect_size.width, rect_size.height);
    			}

			//If taller than it is long, save possible character
			if (rect_size.width < rect_size.height)
			{
				chars.push_back(rect);

				//Calculate average height of characters to eliminate smaller or larger ones
				average += rect.boundingRect().y;
			}
		}
	}

	//Sort characters based on horizontal position
	sort(chars.begin(), chars.end(), compareVect);
	average = average / chars.size();
	prev_x = 0;

	//For each possible character
	for( size_t i = 0; i < chars.size(); i++ )
	{
		/*Check if character is far enough away from last character.
		  This needs to be changed based on resolution/distance of camera*/
		if (i == 0 || (chars[i].boundingRect().x - prev_x) > 10)
		{
			angle = chars[i].angle;
	    		rect_size = chars[i].size;

			//If rectangle is sideways, swap width and height
			if (chars[i].angle < -45.)
			{
				angle += 90.0;
				swap(rect_size.width, rect_size.height);
			}

			//Add a little space around character for OCR
			rect_size.width += 5;
			rect_size.height += 5;			

			/*Next two commented sections add space around a character until all edges
			  are white. This prevents characters from being cut off at the edges.
			  This needs to be fixed, will break if given something with dark background*/

			/*
			partial = 1;

			while (partial)
			{
				partial = 0;
			*/
				//Extract character image from plate
				getRectSubPix(image, rect_size, chars[i].center, cropped);

				//Grayscale, then black and white
				cvtColor(cropped, final, CV_RGB2GRAY);
				final = final > 128;

			/*
				for (size_t j = 0; j < final.rows; j++)
				{
					if (final.at<unsigned char>(j, final.cols - 1) == 0)
					{
						rect_size.width += 1;
						partial = 1;
						break;
					}
				}
			}*/

			//Count number of white pixels
			CvMat temp = final;
			nonzero = cvCountNonZero(&temp);
			total_pixels = rect_size.width * rect_size.height;

			/*If image is a certain percent black, and if character size is comparable to average.
			  These values may need to be adjusted*/
			if ((nonzero / total_pixels < 0.75) && (abs(chars[i].boundingRect().y - average) < 40))
			{
				prev_x = chars[i].boundingRect().x;

				//Smooth character for OCR
				GaussianBlur(final, final, size, 0);

				//Write temporary character image
				sprintf(varName, "%d/char_%d.jpg", random, num);
				imwrite(varName, final);
	
				/*Run tesseract OCR using Linux command line call, single character mode.
				  Outputs result to out.txt*/
				sprintf(varName, "tesseract %d/char_%d.jpg out -psm 10 whitelist > /dev/null", random, num);
				system(varName);

				//Add to temp.txt
				sprintf(varName, "cat out.txt >> temp.txt");
				system(varName);

				num++;
			}
		}
	}

	//Copy to plate.txt, removing newlines. Then delete temp.txt and out.txt
	sprintf(varName, "tr -d '\n' < temp.txt > plate.txt;rm temp.txt out.txt");
	system(varName);

	//Read plate numbers from file
	pFile = fopen ("plate.txt","r");

	if (pFile!=NULL)
		fgets(plate_num, 12, pFile);
	else
		printf("Could not read file.\n");

	fclose(pFile);

	//Delete plate.txt
	system("/bin/rm -f plate.txt");

	//Print the plate number
	printf("%s\n", plate_num);

	//Remove temporary directory. Comment out to see extracted character and plate images
	sprintf(varName, "/bin/rm -rf %d", random);
	system(varName);

	//Add plate number to jpeg metadata
	populateMetaExif(name, plate_num);
}

bool compareVect(RotatedRect x, RotatedRect y)
{
	return x.boundingRect().x < y.boundingRect().x;
}

static double getAngle( Point pt1, Point pt2, Point pt0 )
{
    double dx1 = pt1.x - pt0.x;
    double dy1 = pt1.y - pt0.y;
    double dx2 = pt2.x - pt0.x;
    double dy2 = pt2.y - pt0.y;
    return (dx1*dx2 + dy1*dy2)/sqrt((dx1*dx1 + dy1*dy1)*(dx2*dx2 + dy2*dy2) + 1e-10);
}
