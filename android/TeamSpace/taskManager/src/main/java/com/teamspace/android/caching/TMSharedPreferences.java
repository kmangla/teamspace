package com.teamspace.android.caching;

import android.content.Context;
import android.content.SharedPreferences;

public class TMSharedPreferences {
	private static TMSharedPreferences mInstance;
	private static SharedPreferences sharedPref;

	private TMSharedPreferences(Context context) {
		super();
		sharedPref = context.getSharedPreferences(
				"com.example.taskmanager.sharedprefs", Context.MODE_PRIVATE);
	}

	public static TMSharedPreferences getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new TMSharedPreferences(context);
		}

		return mInstance;
	}
	
	public void putInt(String key, int value) {
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putInt(key, value);
		editor.commit();
	}
	
	public void putString(String key, String value) {
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	public void putBoolean(String key, boolean value) {
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
	
	public int getInt(String key, int defaultValue) {
		return sharedPref.getInt(key,
				defaultValue);
	}
	
	public String getString(String key, String defaultValue) {
		return sharedPref.getString(key,
				defaultValue);
	}
	
	public boolean getBoolean(String key, boolean defaultValue) {
		return sharedPref.getBoolean(key,
				defaultValue);
	}
}
