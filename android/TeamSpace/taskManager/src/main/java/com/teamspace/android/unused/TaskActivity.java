package com.teamspace.android.unused;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;

import com.teamspace.android.R;
import com.teamspace.android.unused.RepeatedTaskFragment;
import com.teamspace.android.common.ui.TaskManagerApplication;

public class TaskActivity extends android.support.v7.app.ActionBarActivity {
	TaskFragment fragment1 = new TaskFragment();
	RepeatedTaskFragment fragment2 = new RepeatedTaskFragment();
	ActionBar.Tab tab1;
	ActionBar.Tab tab2;

	private class MyTabListener implements ActionBar.TabListener {
		Fragment fragment;

		public MyTabListener(Fragment fragment) {
			this.fragment = fragment;
		}

		@Override
		public void onTabSelected(android.support.v7.app.ActionBar.Tab tab,
				FragmentTransaction ft) {
			ft.replace(R.id.taskCreateComponent, fragment);
		}

		@Override
		public void onTabUnselected(android.support.v7.app.ActionBar.Tab tab,
				FragmentTransaction ft) {
			ft.remove(fragment);
		}

		@Override
		public void onTabReselected(android.support.v7.app.ActionBar.Tab arg0,
				FragmentTransaction arg1) {
			// TODO Auto-generated method stub

		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.task_activity);
		TaskManagerApplication applicationContext = ((TaskManagerApplication) getApplication());
		setTitle("Create new task");
		
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		tab1 = actionBar.newTab().setText("Single Task");
		tab2 = actionBar.newTab().setText("Repeated Task");

		tab1.setTabListener((ActionBar.TabListener) new MyTabListener(fragment1));
		tab2.setTabListener((ActionBar.TabListener) new MyTabListener(fragment2));

		actionBar.addTab(tab1);
		actionBar.addTab(tab2);
	}
}