#!/bin/bash
# ShellMS Script
ADB=adb
#or your path to adb

if [ $# != 2 ];then
	echo "Usage: $0 \"Contact's DISPLAY NAME or Phone Number\" \"Your message\""
	exit
else
	$ADB shell am startservice -n com.android.shellms/.sendSMS -e contact "$1" -e msg "$2" 1>/dev/null
fi


