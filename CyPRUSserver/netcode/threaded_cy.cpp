#include <pthread.h>
#include <string.h>
#include "cyprus_client.h"

int main(int argc, char ** argv)
{	
	int sockfd;
	//char comment[20] = "PLATE LOT61B";
	//char  * sendline = malloc(12 * sizeof(char));
			
	if(argc != 3) 
	{
		printf("use: ./cy_client <address> <image to send>\n");
		exit (-1);
	}	
	
	sockfd = tcp_open(argv[1], atoi(argv[2]));
	
	//populateMetaRaw(argv[2], comment);
	populateMetaExif(argv[2], "PLATE");
	sendImageByteArray(argv[2], sockfd);
	//populateImage(sendline, "friend.png",sockfd, 12);
	
	//pthread_exit(NULL);
	
	/*Last one out, get the lights*/
	return tcp_close(sockfd);
}
