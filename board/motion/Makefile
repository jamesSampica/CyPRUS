CC = g++
CFLAGS = -Wall -O2
#CFLAGS = -Wall -g
LDFLAGS = -lpthread

all: main.o motion_handler.o image_handler.o print.o
	${CC} ${CFLAGS} main.o motion_handler.o image_handler.o print.o -o process_motion ${LDFLAGS}

image_handler.o : image_handler.cpp image_handler.h
	${CC} ${CFLAGS} -c image_handler.cpp

image_capture.o : image_capture.cpp image_capture.h
	${CC} ${CFLAGS} -c image_capture.cpp

print.o : print.cpp print.h
	${CC} ${CFLAGS} -c print.cpp

main.o  : main.cpp
	${CC} ${CFLAGS} -c main.cpp

install:
	cp process_motion /usr/local/share/MotionHandler/process_motion

clean:
	rm -f process_motion *.o
