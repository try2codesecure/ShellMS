/*	Copyleft 2013, by Rainer is101024@fhstp.ac.at
 *	University of Applied Sciences St.Pölten - http://www.fhstp.ac.at
 *	This file is part of ShellMS (GPLv3 - https://www.gnu.org/licenses/gpl-3.0.html)
 *  ShellMS is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  ShellMS is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */

package com.android.shellms;

import android.net.Uri;
import android.os.Bundle;
import android.app.Service;
import android.telephony.SmsManager;
import android.util.Log;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.os.IBinder;

public class sendSMS extends Service {    
	  
	private static final String TAG = "ShellMS_Service_sendSMS";
	private int check;
	private boolean secret;
	
	private static final String TELEPHON_NUMBER_FIELD_NAME = "address";
    private static final String MESSAGE_BODY_FIELD_NAME = "body";
    private static final Uri SENT_MSGS_CONTET_PROVIDER = Uri.parse("content://sms/sent");
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand()" );
		// context.startService(new Intent(_context, sendSMS.class));
			
		String contact = null;
		String msg = null;
		check = 0;
		secret = false;
		
		Bundle extras = intent.getExtras();
		if ( extras != null ) {
			if ( extras.containsKey("contact") ) {
				contact = extras.getString("contact");
				Log.d(TAG, contact );
				check++;
			}
			if ( extras.containsKey("msg") ) {
				msg = extras.getString("msg");
				Log.d(TAG, msg );
				check++;
			}
			if ( extras.containsKey("secret") ) {
				secret = true;
			}
			if (check == 2)	{
				sendsms(contact, msg);
			} else {
				Log.e(TAG, "Error: Contact or Message missing" );
			}
		}
		stopSelf();
		return Service.START_STICKY;
	}

	private void sendsms(String phoneNumber, String message)	{
		SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
		if (!secret)	{
			Log.d(TAG, "Empfaenger: " + phoneNumber );
			addMessageToSent(phoneNumber, message);
		} else {
			Log.d(TAG, "Secret Message" );
		}
	}

	private void addMessageToSent(String phoneNumber, String message) {
        ContentValues sentSms = new ContentValues();
        sentSms.put(TELEPHON_NUMBER_FIELD_NAME, phoneNumber);
        sentSms.put(MESSAGE_BODY_FIELD_NAME, message);
        
        ContentResolver contentResolver = getContentResolver();
        contentResolver.insert(SENT_MSGS_CONTET_PROVIDER, sentSms);
    }

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}