#include "cyprus_client.h"

//#define NUM_THREADS 5
//#define DEBUG 1

/**Opens a BSD compatable TCP socket to the given address. 
	@param address Address of server to connect to
	@return On succes, a file descriptor for a new socket is returned. 
	On error, -1 is returned and errno is set appropriately.
*/
int tcp_open(char * address, int port)
{
	int sockfd, err;
	struct sockaddr_in serveraddr;
 	//gnutls_session_t session;
 	
	sockfd = socket(AF_INET, SOCK_STREAM, 0);
	
	bzero(&serveraddr, sizeof(serveraddr));
	serveraddr.sin_family = AF_INET;
	serveraddr.sin_addr.s_addr = inet_addr(address);
	serveraddr.sin_port = (port >  0) ? htons(port) :  htons(32000);
	
	//ssh_verify_cert_callback(gnutls_session_t session)
	
	err = connect(sockfd, (struct sockaddr *)&serveraddr, sizeof(serveraddr));
	
	if(err!=0)
	{
		perror("Socket creation failed: ");	
		return err;
	}
	else
		return sockfd;
}

/**Closes and shutsdown an open BSD socket
	@param sd Socket descriptor for a BSD socket
	@return Returns zero on success. On error, 1 is returned and errno is set appropriately.
*/
int tcp_close(int sd)
{
	return (shutdown(sd, SHUT_RDWR) ||close(sd));
	
}

int populateMetaCom(char * file, char * plate)
{
		FILE * img = fopen(file, "a+");
		char * comment;
		
		//addtional space for jpeg format characters 
		comment = (char * ) malloc( (sizeof(char) * strlen(plate) + 6));
		
		comment[0] = 0xFF;
		comment[1] = 0xFE;
		comment[2] = strlen(plate) / 256;
		comment[3] = strlen(plate) & 256;
		comment[4] = '\0';
		
		
		
		strcat(comment, plate);
		comment[strlen(comment)-1] = 0xFF;
		comment[strlen(comment)] = 0xD9;
		
		///printf("%s\n", comment);
		fseek( img, -2, SEEK_END);
				
		//printf("%d:%d\n", img, fwrite( comment, 1, strlen(comment), img));
		if(!fwrite( comment, 1, strlen(comment), img))
		{
			fclose(img);
			perror("Raw Populate error: ");
			return -1;
		}
		
		fclose(img);
		free(comment);	
		return 0;	
}

int populateMetaExif(std::string file, std::string plate)
{
	try 
	{
		Exiv2::Image::AutoPtr image = Exiv2::ImageFactory::open(file);
		std::string comment = plate + ' '+ "LOT50"; //Add code to get information about lot
	
		if(!image.get())
		{
			throw Exiv2::Error(1, "Failed to open image");
		}
		
		image->readMetadata();
		Exiv2::ExifData &exifData = image->exifData();
	
		
		exifData["Exif.Photo.UserComment"]= comment;
		//std::cout<< "Writing Exiv User Comment "<< exifData["Exif.Image.ImageDescription"] <<"\n";
		image->writeMetadata();
		
	} catch (Exiv2::AnyError& e) {
		std::cout << "Problem Writing Metadata '" << e << "'\n";
		return -1;
	}
	
	return 0;
}

/**Populates a buffer with metainfo about the device being sent from. (i.e. Lot, plate, timestamp, etc.) 
   This method works by memory manipulation. Reliability is not guarenteed. 
	@param buffer A buffer to hold metadata. Returns null if buffer is null or not large enough (>= 12)
	@param plate A string representing extracted information from a licence image
*/
void populateMetaBuffer(char * buffer, char * plate)
{
	//check 
	if(buffer == NULL) 
		return;		
	//intialize 
	int pSize = strlen(plate);
	
	//populate 
	buffer[0] ='i';
	buffer[1] ='\0';
	
	strncat(buffer, plate, strlen(plate));
	
	//Plates 
	int i; 
	if(pSize < 8)
		for(i = pSize+1 ; i< 9 ;)
		{
			buffer[i++] = ' '; 
		}
	

	//lot
	//struct utsname uts;
	//uname(&uts);
	//strcat(buffer, uts.nodename);
	strcat(buffer, "61b");
}

/**Appends information image to meta information buffer. 
	@param buffer Buffer that currently contains meta information.
	@param image Name of image to be sent to server
	@param sockfd File descriptor for an open socket to a server
	@param bufSize size of meta information buffer
	@return On success, returns the size of the byte steam sent to the server. On error, -1 is returned and errno is set.
*/
size_t populateImage(char * buffer, char * image,int sockfd, size_t bufSize)
{
	char * lbuffer;
	long lSize;
	
	//printf("Retreving Image....\n");
	FILE * img = fopen(image, "r");
	if(img == NULL)
	{
		perror("Check img input... \n");
		return -1;
	}
	
	fseek (img , 0 , SEEK_END);
 	lSize = ftell (img);
  	rewind (img);

 
  	lbuffer = ( char *) malloc (sizeof( char)*bufSize);
	if(lbuffer == NULL)
	{
		perror("Malloc Error!\n");
		return -1;
	}
	strncpy(lbuffer, buffer, bufSize);							
	
	
	buffer = NULL;
	buffer = (char *) realloc ( buffer, ((lSize)*sizeof(char)) +(sizeof(char)*bufSize));

	
	fread(buffer, 1, lSize, img);
	memmove(buffer+bufSize, buffer,lSize );
	
	//strncpy(buffer, lbuffer, bufSize);
	
	int i = 0;
	
	for(i = 0; i < (signed) bufSize-1; i++)
	{
		buffer[i]=lbuffer[i];
	}
	
	
	
		if(send(sockfd,buffer, ((lSize)*sizeof(char)) +(sizeof(char)*bufSize), 0) == -1)
		{
			perror("Sending error: ");
			return -1;
		}
	

	free(lbuffer);
	return ((lSize)*sizeof(char)) +(sizeof(char)*bufSize);
}

char  generateMD5hash(char * file)
{
	/*
	1) Create MD5 Struct
	2) Init MD5 Calculator 
	3) Init pthread
	4) Stream file to MD5 Calculator
	5) Popluate struct and return
	*/

	return NOT_YET_IMPLEMENTED;
}

/**Sends image specified byt (image) to the desination specified using (sockfd). 
*/
int sendImageByteArray(char * image,  int sockfd)
{
	//printf("Retreving Image....\n");			
	FILE * img = fopen(image, "r");
	char * lbuffer;
	
	long lSize;
	
	if(img == NULL)
	{
		perror("Check img input... \n");
		return -1;
	}
	
	fseek (img , 0 , SEEK_END);
 	lSize = ftell (img);
  	rewind (img);
  	
  	lbuffer = ( char *) malloc (sizeof( char)*lSize);
	
	fread(lbuffer, 1, lSize, img);
	
	int tmp = htonl(lSize);
	write(sockfd, &tmp, 4);

	if(send(sockfd,lbuffer, lSize+1, 0) == -1)
	{
		perror("Sending error: ");
		return -1;
	}
		
	free(lbuffer);
	return sizeof(lbuffer);
}
