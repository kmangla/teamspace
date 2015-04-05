package com.teamspace.android.common.ui;

import android.support.v4.app.Fragment;

public class DebugActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new DebugFragment();
	}

}
