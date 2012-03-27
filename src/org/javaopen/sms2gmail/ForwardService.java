package org.javaopen.sms2gmail;

import java.util.Date;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.util.Log;

public class ForwardService extends WakeLockService {
	private static final String TAG = ForwardService.class.getName();
	private static final String NEWLINE = System.getProperty("line.separator");
	
	public static final String FORWARD_PHONE = "org.javaopen.sms2gmail.PHONE";
	public static final String FORWARD_SMS   = "org.javaopen.sms2gmail.SMS";
	
	String account = null;

	public ForwardService() {
		super(TAG);
	}
	
	@Override
	void wakeHandler(Intent intent) {
		String action = intent.getAction();
		Log.d(TAG, "wakeHandler: action="+action);
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		String key = getString(R.string.account_key);
		account = sp.getString(key, null);
		
		if (FORWARD_PHONE.equals(action)) {
			forwardPhone(intent);
		} else if (FORWARD_SMS.equals(action)) {
			forwardSMS(intent);
		}
	}
	
	void forwardPhone(Intent intent) {
		long ctime = intent.getLongExtra(PhoneReceiver.CTIME_KEY, System.currentTimeMillis());
		String number = intent.getStringExtra(PhoneReceiver.INCOMING_NUMBER_KEY);
		Log.d(TAG, "forwardPhone: ctime="+ctime+"number="+number);

		StringBuffer body = new StringBuffer();
		// ctime
		body.append(new Date(ctime).toLocaleString());
		body.append(NEWLINE);
		// contact
		Cursor c = null;
		try {
			c = getContentResolver().query(
					Data.CONTENT_URI, 
					new String[]{ Phone.LABEL }, 
					Phone.NUMBER + " = ? ", 
					new String[]{number},
					Phone.LABEL);
			if (c != null) {
				body.append("name=");
				while (c.moveToNext()) {
					int i = 0;
					body.append(c.getString(i++));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (c != null) c.close();
		}
		if (body.length() > 0) body.append(NEWLINE);

		// number
		body.append("tel:");
		body.append(number);
		body.append(NEWLINE);

		if (account == null) {
			Log.d(TAG, "onStart: account is null. ctime="+ctime+", number="+number+", body="+body);
			return;
		}
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		String key = getString(R.string.phone_subject_key);
		String def = getString(R.string.phone_subject_default);
		String subject = sp.getString(key, def);
		
		// gmail
		gmail(account, subject, body.toString());
	}
	
	void forwardSMS(Intent intent) {
		long ctime = intent.getLongExtra(SMSReceiver.TIMESTAMP_KEY, System.currentTimeMillis());
		String from = intent.getStringExtra(SMSReceiver.FROM_KEY);
		String body = intent.getStringExtra(SMSReceiver.BODY_KEY);
		Log.d(TAG, "forwardSMS: ctime="+ctime+", from="+from+", body="+body);

		if (account == null) {
			Log.d(TAG, "onStart: account is null. ctime="+ctime+", from="+from+", body="+body);
			return;
		}
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		String key = getString(R.string.sms_subject_key);
		String def = getString(R.string.sms_subject_default);
		String subject = sp.getString(key, def);
		// TODO gmail
		gmail(account, subject, body);
	}
	
	public void gmail(String to, String subject, String body) {
	    Intent intent = new Intent(Intent.ACTION_SENDTO);
	    intent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
	    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    intent.setData(Uri.parse("mailto:" + to));
	    intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
	    intent.putExtra(android.content.Intent.EXTRA_TEXT, body);
	    try {
	      startActivity(intent);
	    } catch(ActivityNotFoundException ex) {
	      ex.printStackTrace();
	    }
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
