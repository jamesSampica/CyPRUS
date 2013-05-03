
/* Sample TCP server */

#include <sys/socket.h>
#include <netinet/in.h>
#include <stdio.h>
#include <strings.h>
#include <unistd.h>

int main(int argc, char**argv)
{
   int listenfd,connfd,n=1;
   struct sockaddr_in servaddr,cliaddr;
   socklen_t clilen;
   pid_t     childpid;
   char mesg[1000];
   
   FILE * img;

   listenfd=socket(AF_INET,SOCK_STREAM,0);

   bzero(&servaddr,sizeof(servaddr));
   servaddr.sin_family = AF_INET;
   servaddr.sin_addr.s_addr=htonl(INADDR_ANY);
   servaddr.sin_port=htons(32000);
   bind(listenfd,(struct sockaddr *)&servaddr,sizeof(servaddr));

   listen(listenfd,1024);
	
   img = fopen("outimg.jpg", "w");


      clilen=sizeof(cliaddr);
      connfd = accept(listenfd,(struct sockaddr *)&cliaddr,&clilen);

      if ((childpid = fork()) == 0)
      {
         close (listenfd);
	
         while(n != 0)
         {
         	
            n = recvfrom(connfd,mesg,100,0,(struct sockaddr *)&cliaddr,&clilen);
            fwrite(mesg, 1, n, img);
            mesg[0] = 0 ;
            
         }
         
      }
      close(connfd);
   fclose(img);
   
   return 0;
}
