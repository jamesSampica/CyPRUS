CC = g++
CFLAGS =  -Wall
LFLAGS = -lm -lpthread -lexiv2

server : helper_server.c
	$(CC) helper_server.c -o server $(CFLAGS) 
	
thread : threaded_cy.cpp
	$(CC) threaded_cy.cpp -o cy_client $(CFLAGS) 
	
cyprus_client.o : cyprus_client.cpp cyprus_client.h
	$(CC) cyprus_client.cpp $(CFLAGS) -c -g

threaded_cy.o : threaded_cy.cpp cyprus_client.h
	$(CC) threaded_cy.cpp $(CFLAGS) -c -g 

netexample : threaded_cy.o cyprus_client.o
	$(CC) threaded_cy.o cyprus_client.o $(CFLAGS) $(LFLAGS) -o netexample
	
all: server netexample

clean: 
	-/bin/rm -f *.o server netexample
