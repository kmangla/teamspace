package com.teamspace.android.common.ui;

import android.support.v4.app.Fragment;

import com.teamspace.android.employee.ui.EmployeeAddEditFragment;

public class SettingsActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new SettingsFragment();
	}

}
