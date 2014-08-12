package org.javaopen.sms2gmail;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class ForwardService extends IntentService {
	private static final String TAG = ForwardService.class.getName();
	private static final String NEWLINE = System.getProperty("line.separator");
	
	public static final String FORWARD_PHONE = "org.javaopen.sms2gmail.PHONE";
	public static final String FORWARD_SMS   = "org.javaopen.sms2gmail.SMS";
	
	String account = null;

	public ForwardService() {
		super(TAG);
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getAction();
		Log.d(TAG, "wakeHandler: action="+action);
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

		String key = getString(R.string.activated_key);
		boolean activated = sp.getBoolean(key, false);
		if (!activated) return;
		
		key = getString(R.string.account_key);
		account = sp.getString(key, null);
		
		if (FORWARD_PHONE.equals(action)) {
			forwardPhone(intent);
		} else if (FORWARD_SMS.equals(action)) {
			forwardSMS(intent);
		}
	}
	
	boolean isReply() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		String key = getString(R.string.reply_sms_key);
		return sp.getBoolean(key, false);
	}
	
	void replySms(String number) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		String key = getString(R.string.reply_sms_key);
		boolean reply = sp.getBoolean(key, false);
		if (reply) {
			key = getString(R.string.reply_body_key);
			String def = getString(R.string.reply_body_default);
			String text = sp.getString(key, def);
			
			TelephonyManager tm = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
			String myNumber = tm.getLine1Number();
			
			SmsManager sm = SmsManager.getDefault();
			sm.sendTextMessage(number, myNumber, text, null, null);
		}
	}
	
	void forwardPhone(Intent intent) {
		long ctime = intent.getLongExtra(PhoneReceiver.CTIME_KEY, System.currentTimeMillis());
		String number = intent.getStringExtra(PhoneReceiver.INCOMING_NUMBER_KEY);
		Log.d(TAG, "forwardPhone: ctime="+ctime+"number="+number);
		
		replySms(number);

		StringBuffer message = new StringBuffer();
		// ctime
		message.append(new Date(ctime).toLocaleString());
		message.append(NEWLINE);
		// number
		message.append("tel:");
		message.append(number);
		message.append(NEWLINE);
		// contact
		getDisplayName(number, message);

		if (account == null) {
			Log.d(TAG, "onStart: account is null. ctime="+ctime+", number="+number+", message="+message);
			return;
		}
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		String key = getString(R.string.phone_subject_key);
		String def = getString(R.string.phone_subject_default);
		String subject = sp.getString(key, def);
		
		// gmail
		gmail(account, subject, message.toString());
	}
	
	void getDisplayName(String number, StringBuffer message) {
		Cursor c = null;
		try {
			c = getContentResolver().query(
					Data.CONTENT_URI, 
					new String[]{ ContactsContract.Contacts.DISPLAY_NAME }, 
					Phone.NUMBER + " = ? ", 
					new String[]{number},
					null);
			if (c != null) {
				while (c.moveToNext()) {
					int i = 0;
					String name = c.getString(i++);
					if (name != null && name.length() > 0) {
						message.append("name=");
						message.append(name);
						message.append(NEWLINE);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (c != null) c.close();
		}
	}
	
	void forwardSMS(Intent intent) {
		long ctime = intent.getLongExtra(SMSReceiver.TIMESTAMP_KEY, System.currentTimeMillis());
		String from = intent.getStringExtra(SMSReceiver.FROM_KEY);
		String body = intent.getStringExtra(SMSReceiver.BODY_KEY);
		Log.d(TAG, "forwardSMS: ctime="+ctime+", from="+from+", body="+body);
		
		replySms(from);

		if (account == null) {
			Log.d(TAG, "onStart: account is null. ctime="+ctime+", from="+from+", body="+body);
			return;
		}
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		String key = getString(R.string.sms_subject_key);
		String def = getString(R.string.sms_subject_default);
		String subject = sp.getString(key, def);
		// TODO gmail
		StringBuffer message = new StringBuffer();
		// ctime
		message.append(new Date(ctime).toLocaleString());
		message.append(NEWLINE);
		// from
		message.append("from=");
		message.append(from);
		message.append(NEWLINE);
		// contact
		getDisplayName(from, message);
		// body
		message.append("body=");
		message.append(body);
		message.append(NEWLINE);
		
		gmail(account, subject, message.toString());
	}
	
	public void gmail(String to, String subject, String body) {
	    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
	    String key = getString(R.string.account_key);
	    String login = sp.getString(key, null);
	    key = getString(R.string.password_key);
	    String password = sp.getString(key, null);
	    
	    Properties props = new Properties();
	    props.put("mail.smtp.host", "smtp.gmail.com"); // SMTPサーバー
	    props.put("mail.host", "smtp.gmail.com");      // ホスト
	    props.put("mail.smtp.port", "587");       // SMTPポート 
	    props.put("mail.smtp.auth", "true");    // smtp auth
	    props.put("mail.smtp.starttls.enable", "true"); // STTLS
	    
	    Session session = Session.getDefaultInstance(props);
	    session.setDebug(true);

	    MimeMessage msg = new MimeMessage(session);
	    try {
	        msg.setSubject(subject, "utf-8");
	        msg.setFrom(new InternetAddress(login));
	        msg.setSender(new InternetAddress(login));
	        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
	        msg.setText(body,  "utf-8");

	        Transport t = session.getTransport("smtp");
	        t.connect(login, password);
	        t.sendMessage(msg, msg.getAllRecipients());
	    } catch (MessagingException e) {
	        e.printStackTrace();
	    }
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
