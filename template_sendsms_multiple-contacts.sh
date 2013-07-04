#!/bin/bash
# ShellMS Script
ADB=adb
MYIFS=','
#or your path to adb
if [ $# != 2 ];then
	echo "Usage: $0 \"Contact's DISPLAY NAME or Phone Number,nextContact,anotherContact,...\" \"Your message\""
	exit
else
	IFS=$MYIFS read -ra CONTACTS <<< "$1"
	for i in "${CONTACTS[@]}"; do
		echo "... try sending to contact $i"
		$ADB shell am startservice --user 0 -n com.android.shellms/.sendSMS -e contact "$i" -e msg "$2" 1>/dev/null
	done

	sleep 1;
	$ADB logcat -d -s -C ShellMS_Service_sendSMS:*
fi


