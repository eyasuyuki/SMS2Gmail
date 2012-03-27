package org.javaopen.sms2gmail;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver {
	private static final String TAG = SMSReceiver.class.getName();
	
	private static final long INTERVAL = 5000;
	
	public static final String FROM_KEY      = "from";
	public static final String TIMESTAMP_KEY = "timestamp";
	public static final String BODY_KEY      = "body";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle extras = intent.getExtras();
    	AlarmManager am =
    			(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		if (extras != null) {
		    Object[] pdus = (Object[]) extras.get("pdus");
	        for (int i=0; i<pdus.length; i++) {
	        	Object pdu = pdus[i];
	        	
	        	SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
	        	String from = sms.getOriginatingAddress();
	        	long timestamp = sms.getTimestampMillis();
	        	String body = sms.getMessageBody();
	        	
	        	Log.d(TAG, "onReceive: from=" + from);
	        	Log.d(TAG, "onReceive: time=" + Long.toString(timestamp));
	        	Log.d(TAG, "onReceive: body=" + body.replaceAll("\n", "\t"));
	        	
	        	Intent service = new Intent(context, AlarmReceiver.class);
	        	service.setAction(ForwardService.FORWARD_SMS);
	        	service.putExtra(FROM_KEY, from);
	        	service.putExtra(TIMESTAMP_KEY, timestamp);
	        	service.putExtra(BODY_KEY, body);
	        	
	        	PendingIntent pending =
	        			PendingIntent.getBroadcast(context, 0, service, 0);
	        	long interval = i * INTERVAL;
	        	am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+interval, pending);
	        }
		}
    }

}
