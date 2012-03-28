package org.javaopen.sms2gmail;

import java.util.ArrayList;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SMS2GmailActivity extends PreferenceActivity {
	
	public static final String GOOGLE_ACCOUNT = "com.google";
	
	String[] accountNames = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        
        initAccounts();
    }
    
    void initAccounts() {
    	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
    	String key = getString(R.string.account_key);
    	String account = sp.getString(key, null);
		AccountManager am = (AccountManager)getSystemService(Context.ACCOUNT_SERVICE);
		Account[] accounts = am.getAccountsByType(GOOGLE_ACCOUNT);
		accountChooser(accounts, account);
    }
    
    void accountChooser(Account[] accounts, String prev) {
    	if (accounts == null) return;
    	List<String> items = new ArrayList<String>();
    	for (Account a: accounts) {
    		items.add(a.name);
    	}
    	accountNames = items.toArray(new String[0]);
    	int checkedItem = items.indexOf(prev);
    	AlertDialog.Builder builder =
    			new AlertDialog.Builder(this);
    	builder.setTitle(R.string.select_account_title);
    	builder.setSingleChoiceItems(accountNames, checkedItem, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (accountNames != null) {
					String account = accountNames[which];
			    	SharedPreferences sp =
			    			PreferenceManager.getDefaultSharedPreferences(SMS2GmailActivity.this);
			    	String key = getString(R.string.account_key);
			    	Editor editor = sp.edit();
			    	editor.putString(key, account);
			    	editor.commit();
				}
			}
		});
    	builder.show();
    }
}