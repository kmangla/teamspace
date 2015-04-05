package com.android.teamspace.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.teamspace.BuildConfig;
import com.android.teamspace.R;

public class Utils {

	public static int getColor(Context context, String colorName) {
		if ("Light Orange".equalsIgnoreCase(colorName)) {
			return context.getResources().getColor(android.R.color.holo_orange_light);
		} else if ("Dark Orange".equalsIgnoreCase(colorName)) {
			return context.getResources().getColor(android.R.color.holo_orange_dark);
		} else if ("Orange".equalsIgnoreCase(colorName)) {
			return context.getResources().getColor(android.R.color.holo_orange_light);
		} else if ("Dark Red".equalsIgnoreCase(colorName)) {
			return context.getResources().getColor(android.R.color.holo_red_dark);
		} else if ("Purple".equalsIgnoreCase(colorName)) {
			return context.getResources().getColor(android.R.color.holo_purple);
		} else if ("Black".equalsIgnoreCase(colorName)) {
			return context.getResources().getColor(android.R.color.black);
		} else if ("White".equalsIgnoreCase(colorName)) {
			return context.getResources().getColor(android.R.color.white);
		} else if ("Light Blue".equalsIgnoreCase(colorName)) {
			return context.getResources().getColor(android.R.color.holo_blue_light);
		} else if ("Light Green".equalsIgnoreCase(colorName)) {
			return context.getResources().getColor(android.R.color.holo_green_light);
		} else if ("Dark Gray".equalsIgnoreCase(colorName)) {
			return context.getResources().getColor(android.R.color.darker_gray);
		} else if ("Light Gray".equalsIgnoreCase(colorName)) {
			return context.getResources().getColor(android.R.color.darker_gray);
		} else if ("Default".equalsIgnoreCase(colorName)) {
			return context.getResources().getColor(R.color.header_background);
		}		
		
		return context.getResources().getColor(android.R.color.white);
	}

	public static void callPhoneNumber(Context context, String empNumber) {
		Intent callIntent = new Intent(Intent.ACTION_CALL);
		callIntent.setData(Uri.parse("tel:" + empNumber));
		context.startActivity(callIntent);
	}

	public static String extractInitialsFromName(String name) {
		if (!Utils.isStringNotEmpty(name)) {
			return Constants.EMPTY_STRING;
		}
		
		char firstInitial = name.charAt(0);
		char lastInitial = name.charAt((name.indexOf(" ")) + 1);
		String initials = String.valueOf(firstInitial) + String.valueOf(lastInitial);
		return initials;
	}
	
	public static String getSignedInUserId() {
		return "user_1";
	}
	
	public static String getSignedInUserPhoneNumber() {
		return "+918510006309";
	}

	public static boolean isStringNotEmpty(String str) {
		if (str == null || str.length() == 0) {
			return false;
		}
		return true;
	}
	
	/**
	 * Normalize a number returned by Contact Picker to remove any prefixes.
	 * @param number
	 * @return
	 */
	public static String removeCountryPrefixFromPhoneNumber(Context context, String number) {
		if (number.startsWith("0")) {
			return number.substring(1);
		}
		
		// Note: If +91 and +9 are two valid country codes, this loop will
		// remove the one that occurs first in the country_code_array
		String [] countryCodeArray = context.getResources().getStringArray(R.array.country_code_array);
		for (int i = 0; i < countryCodeArray.length; i++) {
			String code = countryCodeArray[i];
			if (number.startsWith(code)) {
				return number.substring(code.length());
			}
		}

		return number;
	}

	public static String getCountryCodeForCountry(Context context, int countryIndex) {
		String [] countryCodeArray = context.getResources().getStringArray(R.array.country_code_array);
		if (countryIndex >= countryCodeArray.length) {
			countryIndex = 0;
		}
		return countryCodeArray[countryIndex];
	}

	public static void refreshListWithoutLosingScrollPosition(ListView listview, ArrayAdapter adapter) {
		if (adapter != null && listview != null) {
			int index = listview.getFirstVisiblePosition();
			View v = listview.getChildAt(0);
			int top = (v == null) ? 0 : (v.getTop() - listview.getPaddingTop());
			
			adapter.notifyDataSetChanged();
			
			listview.setSelectionFromTop(index, top);
		}
	}
	
	public static void log(String message) {
		log(null, message);
	}
	
	public static void log(String tag, String message) {
		if (!BuildConfig.DEBUG) {
			return;
		}
		
		if (tag == null) {
			tag = "TEAMSPACE";
		}
		
		Log.d(tag, message);
	}
	
	public static String getCurrentDate() {
		long time = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, HH:mm");
        Date resultdate = new Date(time);
        return sdf.format(resultdate);
	}
	
	public static String getDateAndTime(long time) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, HH:mm");
        Date resultdate = new Date(time);
        return sdf.format(resultdate);
	}

	public static void setTextViewTextAndVisibility(TextView tv, String text) {
		if (tv == null) {
			return;
		}
		
		if (Utils.isStringNotEmpty(text)) {
			tv.setVisibility(View.VISIBLE);
			tv.setText(text);
		} else {
			tv.setVisibility(View.GONE);
		}		
	}

	public static String getSignedInUserName() {
		return "Vivek Tripathi";
	}

	public static String getSignedInUserNumber() {
		return "+16506447351";
	}
}
