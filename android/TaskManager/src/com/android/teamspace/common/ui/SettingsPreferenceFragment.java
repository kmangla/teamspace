package com.android.teamspace.common.ui;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.android.teamspace.R;

public class SettingsPreferenceFragment extends PreferenceFragment {
	// Default tabs
	public static final String KEY_PREF_DEFAULT_TAB = "pref_key_default_tab";
	public static final String KEY_DEFAULT_TAB = "All Tasks";
	
	// Employee screen colors
	public static final String KEY_PREF_EMP_BK_COLOR = "pref_employee_background_color";
	public static final String KEY_DEFAULT_EMP_BK_COLOR = "Light Orange";
	public static final String KEY_PREF_EMP_TEXT_COLOR = "pref_employee_text_color";
	public static final String KEY_DEFAULT_EMP_TEXT_COLOR = "Black";

	// Sort bar colors
//	public static final String KEY_PREF_SORT_BK_COLOR = "pref_sort_background_color";
//	public static final String KEY_DEFAULT_SORT_BK_COLOR = "Orange";
//	public static final String KEY_PREF_SORT_TEXT_COLOR = "pref_sort_text_color";
//	public static final String KEY_DEFAULT_SORT_TEXT_COLOR = "White";
		
	// Task screen colors
	public static final String KEY_PREF_TASK_BK_COLOR = "pref_task_background_color";
	public static final String KEY_DEFAULT_TASK_BK_COLOR = "Light Orange";
	public static final String KEY_PREF_TASK_TEXT_COLOR = "pref_task_text_color";
	public static final String KEY_DEFAULT_TASK_TEXT_COLOR = "Black";
	
	// Todo screen colors
	public static final String KEY_PREF_TODO_BK_COLOR = "pref_todo_background_color";
	public static final String KEY_DEFAULT_TODO_BK_COLOR = "White";
	public static final String KEY_PREF_TODO_TEXT_COLOR = "pref_todo_text_color";
	public static final String KEY_DEFAULT_TODO_TEXT_COLOR = "Black";
	
	// Chat screen colors
	public static final String KEY_PREF_CHAT_SENDER_BK_COLOR = "pref_chat_sender_background_color";
	public static final String KEY_PREF_CHAT_SENDER_TEXT_COLOR = "pref_chat_sender_text_color";
	public static final String KEY_PREF_CHAT_OTHER_BK_COLOR = "pref_chat_other_background_color";
	public static final String KEY_PREF_CHAT_OTHER_TEXT_COLOR = "pref_chat_other_text_color";
	public static final String KEY_DEFAULT_CHAT_BK_COLOR = "Light Orange";
	public static final String KEY_DEFAULT_CHAT_TEXT_COLOR = "Black";

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}
