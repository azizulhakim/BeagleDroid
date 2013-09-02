BeagleDroid
===========

Boot BBB/BBW via USB from an Android Device

To run the project you need the following:
-an USB Host capable Android device that runs Android newer than 3.1.x
-the kernel of the Android device must be compiled without CONFIG_USB_OTG support in it. USB_NET_RNDIS_HOST needs to be activated in the kernel

After your device is running the kernel that meets the requirements install the .apk of the project from https://github.com/ungureanuvladvictor/BeagleDroid/blob/master/bin/BeagleDroid.apk . After you install the .apk on the device do the following:
-select image you want to download form the drop down menu
-click download
-when the download has finished mark “Make SD Card” *currently all the images from the drop down menu are meant to be ran from uSD card; eMMC images will be supplied soon*
-power up BBB/BBW in USB Boot mode *while having inserted a blank or a non working uSD in the BBB press the user button(the one near the uSD slot) and power on the BBB. this procedure can be made without a uSD inserted in the slot*
-connect to the Android device
-click Flash

A Toast notification will appear when you can disconnect your phone from the board. When the flashing is done only 2 LEDs will be lit.
