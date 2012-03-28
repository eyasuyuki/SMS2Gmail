package org.javaopen.sms2gmail;

import java.util.ArrayList;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Preferences extends PreferenceActivity {
	
	public static final String GOOGLE_ACCOUNT = "com.google";
	
	String[] accountNames = null;
	
	String activatedKey = null;
	String accountKey = null;
	String passwordKey = null;
	String phoneSubjectKey = null;
	String smsSubjectKey = null;
	
	CheckBoxPreference activatedBox = null;
	ListPreference accountList = null;
	EditTextPreference passwordEdit = null;
	EditTextPreference phoneSubjectEdit = null;
	EditTextPreference smsSubjectEdit = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        
        // activated
        activatedKey = getString(R.string.activated_key);
        activatedBox = (CheckBoxPreference)findPreference(activatedKey);
        // account
        accountKey = getString(R.string.account_key);
        accountList = (ListPreference)findPreference(accountKey);
        // password
        passwordKey = getString(R.string.password_key);
        passwordEdit = (EditTextPreference)findPreference(passwordKey);
        // phone
        phoneSubjectKey = getString(R.string.phone_subject_key);
        phoneSubjectEdit = (EditTextPreference)findPreference(phoneSubjectKey);
        // sms
        smsSubjectKey = getString(R.string.sms_subject_key);
        smsSubjectEdit = (EditTextPreference)findPreference(smsSubjectKey);

        accountList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				accountList.setSummary(newValue.toString());
				return true;
			}
		});
        
        passwordEdit.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (newValue != null) {
					StringBuffer buf = new StringBuffer();
					for (int i=0; i<newValue.toString().length(); i++) buf.append("*");
			    	passwordEdit.setSummary(buf.toString());
				}
				return true;
			}
		});
        
        phoneSubjectEdit.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				phoneSubjectEdit.setSummary(newValue.toString());
				return true;
			}});
        
        smsSubjectEdit.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				smsSubjectEdit.setSummary(newValue.toString());
				return true;
			}
		});
        
        initSummary();
    }
    
    void initSummary() {
    	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
    	// activated
    	if (!sp.contains(activatedKey)) {
    		Editor editor = sp.edit();
    		editor.putBoolean(activatedKey, true);
    		activatedBox.setChecked(true);
    	}
    	// account
    	String account = sp.getString(accountKey, null);
		AccountManager am = (AccountManager)getSystemService(Context.ACCOUNT_SERVICE);
		Account[] accounts = am.getAccountsByType(GOOGLE_ACCOUNT);
    	if (accounts != null && accounts.length > 0) {
        	List<String> items = new ArrayList<String>();
        	for (Account a: accounts) {
        		items.add(a.name);
        	}
        	accountNames = items.toArray(new String[0]);
    		accountList.setEntries(accountNames);
    		accountList.setEntryValues(accountNames);
    		// check default
    		if (account == null) {
    			Editor editor = sp.edit();
    			editor.putString(accountKey, accountNames[0]);
    			editor.commit();
    			account = accountNames[0];
    		}
    	}
    	accountList.setDefaultValue(account);
    	accountList.setSummary(account);
		// password
		String passwordText = sp.getString(passwordKey, null);
		if (passwordText != null) {
			StringBuffer buf = new StringBuffer();
			for (int i=0; i<passwordText.length(); i++) buf.append("*");
	    	passwordEdit.setSummary(buf.toString());
	    	passwordEdit.setDialogMessage(account);
		}
		passwordDialog(passwordText); // TEST
    	// phone
    	String phoneSubjectDefault = getString(R.string.phone_subject_default);
    	String phoneSubjectText = sp.getString(phoneSubjectKey, phoneSubjectDefault);
    	phoneSubjectEdit.setText(phoneSubjectText);
    	phoneSubjectEdit.setSummary(phoneSubjectText);
    	// sms
    	String smsSubjectDefault = getString(R.string.sms_subject_default);
    	String smsSubjectText = sp.getString(smsSubjectKey, smsSubjectDefault);
    	smsSubjectEdit.setText(smsSubjectText);
    	smsSubjectEdit.setSummary(smsSubjectText);
    }
    
    void passwordDialog(String passwordText) {
    	LayoutInflater inflater = getLayoutInflater();
    	View dialoglayout =
    			inflater.inflate(R.layout.dialog_layout, (ViewGroup)getCurrentFocus());
    	AlertDialog.Builder builder =
    			new AlertDialog.Builder(this);
    	builder.setView(dialoglayout);
    	builder.setTitle(R.string.password_title);
    	builder.show();
    }
}