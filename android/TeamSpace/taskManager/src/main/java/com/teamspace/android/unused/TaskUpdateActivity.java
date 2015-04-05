package com.teamspace.android.unused;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;

import com.teamspace.android.R;
import com.teamspace.android.common.ui.TaskManagerApplication;
import com.teamspace.android.models.Task;

public class TaskUpdateActivity extends
		android.support.v7.app.ActionBarActivity {

	Fragment fragment1;
	Fragment fragment2 = new TaskUpdateRepliesFragment();
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
			ft.replace(R.id.taskUpdateProfileContainer, fragment);
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
		setContentView(R.layout.task_update_activity);
		Bundle extras = getIntent().getExtras();
		long value = 0;
		if (extras != null) {
			value = extras.getLong("TASK_ID");
		}
		TaskManagerApplication applicationContext = ((TaskManagerApplication) getApplication());
		Task task = applicationContext.getTaskByID(value);
		setTitle("Task #" + task.getId());

		if (fragment1 == null) {
			if (task.isRepeated()) {
				fragment1 = new RepeatedTaskUpdateProfileFragment();
			} else {
				fragment1 = new TaskUpdateProfileFragment();
			}
		}		
		
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		tab1 = actionBar.newTab().setText("Update Task");
		tab2 = actionBar.newTab().setText("Task Replies");

		tab1.setTabListener((ActionBar.TabListener) new MyTabListener(fragment1));
		tab2.setTabListener((ActionBar.TabListener) new MyTabListener(fragment2));

		actionBar.addTab(tab2);
		actionBar.addTab(tab1);
	}
}
