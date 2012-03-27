package org.javaopen.sms2gmail;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public abstract class WakeLockService extends IntentService {
	abstract void wakeHandler(Intent intent);

	public static final String LOCK_NAME =
		"org.javaopen.ForwardService.Static";
	private static PowerManager.WakeLock lock = null;

	public static void acquireStaticLock(Context context) {
		getLock(context).acquire();
	}

	synchronized private static PowerManager.WakeLock getLock(Context context) {
		if (lock == null) {
			PowerManager mgr =
				(PowerManager)context.getSystemService(Context.POWER_SERVICE);
			lock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME);
			lock.setReferenceCounted(true);
		}

		return (lock);
	}

	public WakeLockService(String name) {
		super(name);
	}

	@Override
	final protected void onHandleIntent(Intent intent) {
		try {
			wakeHandler(intent);
		} finally {
			getLock(this).release();
		}
	}
}