package com.android.teamspace.tasklist.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.android.teamspace.common.ui.SingleFragmentActivity;
import com.android.teamspace.utils.Constants;

public class TasksForEmployeeActivity extends SingleFragmentActivity {

	private String mEmployeeId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mEmployeeId = getIntent().getExtras().getString(Constants.EMPLOYEE_ID);
		super.onCreate(savedInstanceState);
	}


	@Override
	protected Fragment createFragment() {
		return new TasksForEmployeeListViewFragment(mEmployeeId);
	}
}
