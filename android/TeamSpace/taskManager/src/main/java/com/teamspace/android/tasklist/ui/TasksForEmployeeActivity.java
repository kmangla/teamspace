package com.teamspace.android.tasklist.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.teamspace.android.common.ui.SingleFragmentActivity;
import com.teamspace.android.utils.Constants;
import com.teamspace.android.utils.Utils;

public class TasksForEmployeeActivity extends SingleFragmentActivity {

	private String mEmployeeId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mEmployeeId = getIntent().getExtras().getString(Constants.EMPLOYEE_ID);
		super.onCreate(savedInstanceState);
	}


	@Override
	protected Fragment createFragment() {
        TasksForEmployeeListViewFragment frag = new TasksForEmployeeListViewFragment();
        Bundle args = new Bundle();
        args.putString(Constants.EMPLOYEE_ID, mEmployeeId);
        frag.setArguments(args);
        return frag;
	}
}
