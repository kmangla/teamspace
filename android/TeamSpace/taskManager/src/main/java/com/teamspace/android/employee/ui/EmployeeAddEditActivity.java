package com.teamspace.android.employee.ui;

import com.teamspace.android.common.ui.SingleFragmentActivity;

import android.support.v4.app.Fragment;

public class EmployeeAddEditActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new EmployeeAddEditFragment();
	}

}
