cmake_minimum_required(VERSION 2.8)
project( localize )
find_package( OpenCV REQUIRED )
add_executable( localize localize.cpp netcode/cyprus_client.o )
target_link_libraries( localize ${OpenCV_LIBS} exiv2 )

