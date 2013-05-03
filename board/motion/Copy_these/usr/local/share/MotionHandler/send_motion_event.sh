#!/bin/bash

#Use a named pipe to send the motion detected event to the process_motion program

pipe="/usr/local/share/MotionHandler/image_ready_pipe"

if [[ ! -p $pipe ]]; then
    echo "Error: Pipe doesn't exist. (file: $pipe)"
    exit 1
fi

echo -e "\x01" > $pipe
echo -e "\x42" > $pipe
