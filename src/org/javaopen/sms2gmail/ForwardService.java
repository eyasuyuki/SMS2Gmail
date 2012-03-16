package org.javaopen.sms2gmail;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ForwardService extends Service {
	private static final String TAG = ForwardService.class.getName();
	
	public static final String FORWARD_PHONE = "org.javaopen.sms2gmail.PHONE";
	public static final String FORWARD_SMS   = "org.javaopen.sms2gmail.SMS";

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		String action = intent.getAction();
		Log.d(TAG, "onStart: action="+action);
		
		if (FORWARD_PHONE.equals(action)) {
			forwardPhone(intent);
		} else if (FORWARD_SMS.equals(action)) {
			forwardSMS(intent);
		}
	}
	
	void forwardPhone(Intent intent) {
		long ctime = intent.getLongExtra(PhoneReceiver.CTIME_KEY, System.currentTimeMillis());
		String number = intent.getStringExtra(PhoneReceiver.INCOMING_NUMBER_KEY);
		// TODO contact
	}
	
	void forwardSMS(Intent intent) {}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
