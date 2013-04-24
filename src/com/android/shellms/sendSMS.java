/*	Copyleft 2013, by Rainer is101024@fhstp.ac.at
 *	University of Applied Sciences St.Pölten - http://www.fhstp.ac.at
 *	Project Home = https://github.com/try2codesecure/ShellMS
 *	This file is part of ShellMS (GPLv3 - https://www.gnu.org/licenses/gpl-3.0.html)
 *  ShellMS is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  ShellMS is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */

package com.android.shellms;

import android.net.Uri;
import android.os.Bundle;
import android.app.Service;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.util.Log;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;

public class sendSMS extends Service {    
	  
	private static final String TAG = "ShellMS_Service_sendSMS";
	private static final String TELEPHON_NUMBER_FIELD_NAME = "address";
    private static final String MESSAGE_BODY_FIELD_NAME = "body";
    private static final Uri SENT_MSGS_CONTET_PROVIDER = Uri.parse("content://sms/sent");
	
	// This is the start function for the service.
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		boolean SECRET = false;	// for secret mode => dont't save sent sms to sent folder. 
		boolean DEBUG = false;	// debug mode, display additional output, sends no sms.
		String contact = null;
		String val_num = null;	// validated Number
		String msg = null;		// message
		boolean valid = false;	// for user input validation
		int check = 0;			// getExtras check counter
		
		// extract and validate the extra strings from the service start
		Bundle extras = intent.getExtras();
		if ( extras != null ) {
			if ( extras.containsKey("debug") ) {
				DEBUG = true;
				Log.d(TAG, "DEBUG Mode enabled" );
			}
			if ( extras.containsKey("secret") ) {
				SECRET = true;
			}
			if ( extras.containsKey("contact") ) {
				contact = extras.getString("contact");
				if (contact!=null)	{
					check++;
				}
			}
			if ( extras.containsKey("msg") ) {
				msg = extras.getString("msg");
				if (msg!=null)	{
					check++;
				}
			}
			if (check == 2)	{
				if (DEBUG)	{
					Log.d(TAG, contact);
				}
				// search for valid telephone number
				valid = isNumberValid(contact);
				if (!valid)	{
					if (DEBUG)	{
						Log.d(TAG, "not valid");
					}
					val_num = makeNumberValid(contact);
					if (val_num != null)	{
						if (DEBUG)	{
							Log.d(TAG, "val_num != null");
						}
						valid = true;
						contact = val_num;
					} else	{
						valid = false;
						if (DEBUG)	{
							Log.d(TAG, "Error: Can't validate mobile number: " + contact);
						}
					}
				}
				if (!valid)	{
					// otherwise search for valid contact names in database
					val_num = getNumberfromContact(contact, DEBUG);
					if (val_num != null)	{
						contact = val_num;
						valid = true;
						if (DEBUG)	{
							Log.d(TAG, "found contact: " + contact );
						}
					} else	{
						Log.e(TAG, "Error: No valid mobile number for contact " + contact);
					}
				}
				if (valid)	{
					if (!DEBUG)	{
						sendsms(contact, msg, !SECRET);
						Log.i(TAG, "Sent SMS to contact: " + contact );
					} else	{
						Log.e(TAG, "NO MESSAGE WILL BE SENT IN DEBUG MODE" );
						Log.e(TAG, "Contact: " + contact );
						Log.e(TAG, "Message: " + msg);
					}
				} else	{
					Log.e(TAG, "Unknown Error occoured with contact: " + contact);
				}
			} else {
				Log.e(TAG, "Error: Contact or Message missing" );
			}
		}
		stopSelf();
		return Service.START_STICKY;
	}

	// User input validation
	private Boolean isNumberValid(String contact)	{
		if (contact == null)	{
			return false;
		}
		boolean valid1 = PhoneNumberUtils.isGlobalPhoneNumber(contact);
		boolean valid2 = PhoneNumberUtils.isWellFormedSmsAddress(contact);
		if ((valid1 == true) && (valid2 == true))	{
			return true;
		}
		return false;
	}
	private String makeNumberValid(String contact)	{
		if (contact == null)	{
			return null;
		}
		String number = null;
		number = PhoneNumberUtils.formatNumber(contact);
		Boolean valid = isNumberValid(number);
		if (valid)	{
			return number;
		}
		return null;
	}
	
	// This function searches for an mobile phone entry for the contact
	private String getNumberfromContact(String contact, Boolean debugging)	{
		ContentResolver cr = getContentResolver();
		String result = null;
		boolean valid = false;	
		String val_num = null;
		int contact_id = 0;
        // Cursor1 search for valid Database Entries who matches the contact name
		Uri uri = ContactsContract.Contacts.CONTENT_URI;
		String[] projection = new String[]{	ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER };
		String selection = ContactsContract.Contacts.DISPLAY_NAME + "=?";
		String[] selectionArgs = new String[]{String.valueOf(contact)};
		String sortOrder = null;
		Cursor cursor1 = cr.query(uri, projection, selection, selectionArgs, sortOrder);
	
	    if(cursor1.moveToFirst()){
	    	if(cursor1.getInt(cursor1.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) == 1){
	    		contact_id = cursor1.getInt(cursor1.getColumnIndex(ContactsContract.Contacts._ID));
	    		if (debugging)	{
	        		Log.d(TAG, "C1 found Database ID: " + contact_id + " with Entry: " + cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
	            }
	            // Cursor 2 search for valid MOBILE Telephone numbers (selection = Phone.TYPE 2)
	        	Uri uri2 = ContactsContract.Data.CONTENT_URI;	
	        	String[] projection2 = new String[]{ Phone.NUMBER, Phone.TYPE };
	        	String selection2 = Phone.CONTACT_ID + "=? AND " + Data.MIMETYPE + "=? AND " + Phone.TYPE + "=2";
	    		String[] selectionArgs2 = new String[]{ String.valueOf(contact_id), Phone.CONTENT_ITEM_TYPE };
	    		String sortOrder2 = Data.IS_PRIMARY + " desc"; 	
	        	Cursor cursor2 = cr.query(uri2, projection2, selection2, selectionArgs2, sortOrder2);
	            
	        	if(cursor2.moveToFirst()){
	                result = cursor2.getString(cursor2.getColumnIndex(Phone.NUMBER));
	        		if (debugging)	{
	                	Log.d(TAG, "C2 found number: " + result);
	                }
	            }
	            cursor2.close();
	        }
	        cursor1.close();
	    }
	    if (result != null)	{
	    	valid = isNumberValid(result);
	    }
		if (!valid)	{
			val_num = makeNumberValid(result);
			if (val_num != null)	{
				valid = true;
				result = val_num;
			}
		}
	    if (valid)	{
	    	return result;
	    } else	{
	    	return null;
	    }
	}
	
	
	// This function sends the sms with the SMSManager
	private void sendsms(String phoneNumber, String message, Boolean AddtoSent)	{
		SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
		if (AddtoSent)	{
			addMessageToSent(phoneNumber, message);
		}
	}

	// This function add's the sent sms to the SMS sent folder
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