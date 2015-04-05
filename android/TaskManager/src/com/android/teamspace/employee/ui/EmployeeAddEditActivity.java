package com.android.teamspace.employee.ui;

import com.android.teamspace.common.ui.SingleFragmentActivity;

import android.support.v4.app.Fragment;

public class EmployeeAddEditActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new EmployeeAddEditFragment();
	}

}
