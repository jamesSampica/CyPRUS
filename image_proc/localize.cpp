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

static void findRects( const Mat& image, vector<vector<Point> >& squares );
static void extractRects( Mat& image, IplImage *image2, const vector<vector<Point> >& squares, char *name );
static void segmentChars(Mat &image, char *name);
double calcEntropy(Mat &input);
static double angle( Point pt1, Point pt2, Point pt0 );
bool compareVect(RotatedRect x, RotatedRect y);

int thresh = 100, N = 11;

int main(int argc, char** argv)
{
	//namedWindow( wndname, 1 );
	vector<vector<Point> > squares;

	vector<Mat> images;
	IplImage *image2;
	int i, index;
	double entropy = DBL_MAX, current;

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
				index = (i - 1);
			}
		}
	}

	image2 = cvLoadImage(argv[index], CV_LOAD_IMAGE_UNCHANGED);

	findRects(images[index], squares);
	extractRects(images[index], image2, squares, argv[index + 1]);

	return 0;
}


static void findRects( const Mat& image, vector<vector<Point> >& squares )
{
    squares.clear();

    Mat pyr, timg, gray0(image.size(), CV_8U), gray;

    pyrDown(image, pyr, Size(image.cols/2, image.rows/2));
    pyrUp(pyr, timg, image.size());
    vector<vector<Point> > contours;

   // imshow(wndname, gray0);
    //waitKey(0);

    for( int c = 0; c < 3; c++ )
    {
        int ch[] = {c, 0};
        mixChannels(&timg, 1, &gray0, 1, ch, 1);

        for( int l = 0; l < N; l++ )
        {
            if( l == 0 )
            {
                Canny(gray0, gray, 0, thresh, 5);
		//imshow(wndname, gray);
		//waitKey(0);

                dilate(gray, gray, Mat(), Point(-1,-1));
		//imshow(wndname, gray);
		//waitKey(0);		
            }
            else
                gray = gray0 >= (l+1)*255/N;

		equalizeHist(gray, gray);

            findContours(gray, contours, CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);

            vector<Point> approx;

            for( size_t i = 0; i < contours.size(); i++ )
            {
                approxPolyDP(Mat(contours[i]), approx, arcLength(Mat(contours[i]), true)*0.02, true);

                if( approx.size() == 4 &&
                    fabs(contourArea(Mat(approx))) > 1000 &&
                    isContourConvex(Mat(approx)) )
                {
                    double maxCosine = 0;

                    for( int j = 2; j < 5; j++ )
                    {
                        double cosine = fabs(angle(approx[j%4], approx[j-2], approx[j-1]));
                        maxCosine = MAX(maxCosine, cosine);
                    }

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
	char varName[100];
	int status, num = 0;
	float width, height;

	//name[strlen(name) - 4] = '\0';
	//memmove(name, name + 7, strlen(name));

	//sprintf(varName, "out/%s", name);

	status = mkdir("/home/david/localize/out/demo", S_IRWXU);

	if (status != 0)
	{
		printf("Could not create directory.\n");
		return;
	}

	for (size_t i = 0; i < squares.size(); i++)
	{
		RotatedRect rect = minAreaRect(Mat(squares[i]));

		float angle = rect.angle;
		Size rect_size = rect.size;

		if (rect.angle < -45.)
		{
			angle += 90.0;
			swap(rect_size.width, rect_size.height);
		}

		width = rect_size.width;
		height = rect_size.height;
	
		if (width > height || height > 500)
		{
			num++;

			M = getRotationMatrix2D(rect.center, angle, 1.0);
			warpAffine(image, rotated, M, image.size(), INTER_CUBIC);
			getRectSubPix(rotated, rect_size, rect.center, cropped);

			Size size(3,3);

			cvtColor(cropped, grey, CV_RGB2GRAY);
			//equalizeHist(grey, grey);
			//threshold(grey, grey, 100, 255, CV_THRESH_BINARY);
			GaussianBlur(grey, grey, size, 0);

			sprintf(varName, "/home/david/localize/out/demo/plate_%d.jpg", num);
			imwrite(varName, grey);

			if (((width / height) > 1.8) && ((width / height) < 2.1))
			{
				segmentChars(cropped, name);
				return;
			}
		}

	}

	//segmentChars(cropped, name);

	//for( size_t i = 0; i < squares.size(); i++ )
	//{
	//const Point* p = &squares[i][0];
	//int n = (int)squares[i].size();
	//polylines(image, &p, &n, 1, true, Scalar(0,255,0), 3, CV_AA);
	//}
}

static void segmentChars(Mat &image, char *name)
{
	Mat M, rotated, cropped, gray, thresh, edge, canny_out, final;
	vector<vector<Point> > contours;
	vector<Point> approx;
	int num = 1, average = 0;
	char varName[100], plate_num[12];
	char addr[20] = "10.24.102.145";
	RotatedRect rect;
	float angle, total_pixels, nonzero, prev_x;
	Size rect_size;
	int sockfd;

	//sockfd = tcp_open(addr, 0);
	FILE *pFile;

	cvtColor(image, gray, CV_RGB2GRAY);
	medianBlur(gray, gray, 5);
	
      Canny(gray, canny_out, 0, 100, 5);
	dilate(canny_out, thresh, Mat(), Point(-1,-1));
	erode(thresh, thresh, Mat(), Point(-1,-1));

	findContours(thresh, contours, CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE, cvPoint(0, 0));

	//printf("%d\n", contours.size());

	vector<RotatedRect> chars;

	for( size_t i = 0; i < contours.size(); i++ )
	{
		approxPolyDP(Mat(contours[i]), approx, arcLength(Mat(contours[i]), true)*0.02, true);

		if( //approx.size() == 4 &&
                    fabs(contourArea(Mat(approx))) > 500)// &&
                    //isContourConvex(Mat(approx)) )
                {

		//printf("%lf\n", fabs(contourArea(Mat(approx))));
                   /* double maxCosine = 0;

                    for( int j = 2; j < 5; j++ )
                    {
                        double cosine = fabs(angle(approx[j%4], approx[j-2], approx[j-1]));
                        maxCosine = MAX(maxCosine, cosine);
                    }

                    if( maxCosine < 0.3 )
		    {
			Rect rect = boundingRect(Mat(approx));
			rectangle(image, rect, Scalar(0, 255, 0));

			imshow("img", image);
			waitKey(0);
		    //}*/

			rect = minAreaRect(Mat(approx));

			angle = rect.angle;
    			rect_size = rect.size;

			if (rect.angle < -45.)
    			{
				angle += 90.0;
				swap(rect_size.width, rect_size.height);
    			}

			if (rect_size.width < rect_size.height)
			{
				chars.push_back(rect);
				average += rect.boundingRect().y;
			}
		}
	}

	sort(chars.begin(), chars.end(), compareVect);
	average = average / chars.size();
	prev_x = 0;
	for( size_t i = 0; i < chars.size(); i++ )
	{
		if (i == 0 || (chars[i].boundingRect().x - prev_x) > 15)
		{
			angle = chars[i].angle;
	    		rect_size = chars[i].size;
			prev_x = chars[i].boundingRect().x;

			if (chars[i].angle < -45.)
			{
				angle += 90.0;
				swap(rect_size.width, rect_size.height);
			}

			//if ((chars[i + 1].boundingRect().x - chars[i].boundingRect().x) <= 15)
				//chars.erase(chars.begin() + i + 1);

			//M = getRotationMatrix2D(rect.center, angle, 1.0);
			//warpAffine(image, rotated, M, image.size(), INTER_CUBIC);

			rect_size.width += 5;
			rect_size.height += 5;

			getRectSubPix(image, rect_size, chars[i].center, cropped);

			Size size(3,3);

			cvtColor(cropped, final, CV_RGB2GRAY);
			//equalizeHist(grey, grey);
			//threshold(final, final, 100, 255, CV_THRESH_BINARY);
			final = final > 128;

			CvMat temp = final;
			nonzero = cvCountNonZero(&temp);
			total_pixels = rect_size.width * rect_size.height;

			if ((nonzero / total_pixels < 0.75) && (abs(chars[i].boundingRect().y - average) < 40))
			{
				GaussianBlur(final, final, size, 0);

				sprintf(varName, "/home/david/localize/out/demo/char_%d.jpg", num);
				imwrite(varName, final);
	
				sprintf(varName, "tesseract /home/david/localize/out/demo/char_%d.jpg out -psm 10 whitelist > /dev/null", num);
				system(varName);

				sprintf(varName, "cat out.txt >> temp.txt");
				system(varName);

				num++;
			}
		}
	}

	sprintf(varName, "tr -d '\n' < temp.txt > plate.txt;rm temp.txt");
	system(varName);

	pFile = fopen ("plate.txt","r");

	if (pFile!=NULL)
		fgets(plate_num, 12, pFile);
	else
		printf("Could not read file.\n");

	fclose(pFile);

	printf("%s\n", plate_num);

	//system("/bin/rm -rf /home/david/localize/out/demo");
	//populateMetaExif(name, plate_num);
	//sendImageByteArray(name, sockfd);

	//tcp_close(sockfd);
}

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

bool compareVect(RotatedRect x, RotatedRect y)
{
	return x.boundingRect().x < y.boundingRect().x;
}

static double angle( Point pt1, Point pt2, Point pt0 )
{
    double dx1 = pt1.x - pt0.x;
    double dy1 = pt1.y - pt0.y;
    double dx2 = pt2.x - pt0.x;
    double dy2 = pt2.y - pt0.y;
    return (dx1*dx2 + dy1*dy2)/sqrt((dx1*dx1 + dy1*dy1)*(dx2*dx2 + dy2*dy2) + 1e-10);
}
