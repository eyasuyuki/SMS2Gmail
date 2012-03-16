package org.javaopen.sms2gmail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneReceiver extends BroadcastReceiver {
	private static final String TAG = PhoneReceiver.class.getName();
	
	public static final String CTIME_KEY = "ctime";
	public static final String INCOMING_NUMBER_KEY = "incoming_number";

	@Override
	public void onReceive(Context context, Intent intent) {
		long ctime = System.currentTimeMillis();

		String action = intent.getAction();
		String incoming = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
		Log.d(TAG, "onReceive: ctime="+ctime+", action="+action+", incoming="+incoming);
		
		if (incoming != null) {
			Intent service = new Intent(context, ForwardService.class);
			service.putExtra(CTIME_KEY, ctime);
			service.setAction(ForwardService.FORWARD_PHONE);
			service.putExtra(INCOMING_NUMBER_KEY, incoming);
			context.startService(service);
		}
	}

}
