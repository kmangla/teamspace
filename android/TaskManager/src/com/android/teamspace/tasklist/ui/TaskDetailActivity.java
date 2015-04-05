package com.android.teamspace.tasklist.ui;

import android.support.v4.app.Fragment;

import com.android.teamspace.common.ui.SingleFragmentActivity;

public class TaskDetailActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new TaskDetailFragment();
	}

}
