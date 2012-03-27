package org.javaopen.sms2gmail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;



public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		WakeLockService.acquireStaticLock(context);
		
		Intent service = new Intent(context, ForwardService.class);
		service.setAction(intent.getAction());
		service.setFlags(intent.getFlags());
		service.setData(intent.getData());
		Bundle extras = intent.getExtras();
		service.putExtras(extras);

		context.startService(service);
	}

}