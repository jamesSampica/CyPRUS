#!/bin/bash

#CAPTURE_RES="2592x1944" #5MP
#CAPTURE_RES="2048x1536" #3MP
CAPTURE_RES="1600x1200" #2MP
#CAPTURE_RES="1280x1024" #1.3MP
#CAPTURE_RES="1024x768" #0.8MP
#CAPTURE_RES="800x600" #0.5MP
#CAPTURE_RES="640x480" #0.3MP

#Wide:
#CAPTURE_RES="1920x1080" #2MP
#CAPTURE_RES="1280x720" #0.9MP

#Resolution the captured image is scaled to
CAPTURE_SCALED_RES=$CAPTURE_RES

#Sensor->CCDC->Preview->Resizer pipeline init
# - 2-0048 is the I2C bus address
# - CCDC processes RAW data from the sensor
# - Preview block converts the data from CCDC and
#   converts it to YUV4:2:2 format
# - Resizer scales the YUV4:2:2 format image and stores
#   it in memory
media-ctl -v -r -l \
'"mt9p031 2-0048":0->"OMAP3 ISP CCDC":0[1], '\
'"OMAP3 ISP CCDC":2->"OMAP3 ISP preview":0[1], '\
'"OMAP3 ISP preview":1->"OMAP3 ISP resizer":0[1], '\
'"OMAP3 ISP resizer":1->"OMAP3 ISP resizer output":0[1]'

#Sensor->CCDC->Preview->Resizer pipeline configuration
# - Sensor: 12 bit SGRBG 5MP image
# - CCDC: 10 bit SGRBG 5MP image
# - Preview: 5MP YUV4:2:2
# - Resize: 5MP YUV4:2:2
media-ctl -v -V \
'"mt9p031 2-0048":0 [SGRBG12 '$CAPTURE_RES'], '\
'"OMAP3 ISP CCDC":2 [SGRBG10 '$CAPTURE_RES'], '\
'"OMAP3 ISP preview":1 [UYVY '$CAPTURE_RES'], '\
'"OMAP3 ISP resizer":1 [UYVY '$CAPTURE_SCALED_RES']'

#Brightness. Min 0, max 255, step 1. Default 0
#yavta --set-control '0x00980900 0' /dev/v4l-subdev3

#Contrast. Min 0, max 255, step 1. Default 16
#yavta --set-control '0x00980901 16' /dev/v4l-subdev3

#Exposure. Min 1, max 1048575, step 1. Default 1943
#Outdoors: 5
#Indoors: 30
yavta --set-control '0x00980911 5' /dev/v4l-subdev8

#Gain. Min 8, max 1024, step 1. Default 8
yavta --set-control '0x00980913 50' /dev/v4l-subdev8

#BLC Auto. Min 0, max 1, step 1. Default 1
#yavta --set-control '0x00981902 1' /dev/v4l-subdev8

#BLC target level (for auto). Min 0, max 4095, step 1. Default 168
#yavta --set-control '0x00981903 168' /dev/v4l-subdev8

#BLC analog offset (for manual). Min -255, max 255, step 1. Default 32
#yavta --set-control '0x00981904 32' /dev/v4l-subdev8

#BLC digital offset (for manual). Min -2048, max 2047, step 1. Default 40
#yavta --set-control '0x00981905 40' /dev/v4l-subdev8
