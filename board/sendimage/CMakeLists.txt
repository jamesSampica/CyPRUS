cmake_minimum_required(VERSION 2.8)
project( sendimage )
find_package( OpenCV REQUIRED )
add_executable( sendimage sendimage.cpp /home/ubuntu/sendimage/netcode/cyprus_client.o )
target_link_libraries( sendimage ${OpenCV_LIBS} exiv2 )

