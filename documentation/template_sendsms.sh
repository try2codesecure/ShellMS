#!/bin/sh
# ShellMS Script
ADB=adb
#or your path to adb

if [ $# != 2 ];then
	echo "Usage: $0 \"Contact's DISPLAY NAME or Phone Number\" \"Your message\""
	exit
else
	$ADB shell am startservice --user 0 -n com.android.shellms/.sendSMS -e contact "$1" -e msg "$2" 1>/dev/null
	sleep 1;
	$ADB logcat -d -s -C ShellMS_Service_sendSMS:*
fi
