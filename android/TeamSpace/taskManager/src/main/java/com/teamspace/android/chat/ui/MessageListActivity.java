package com.teamspace.android.chat.ui;

import android.support.v4.app.Fragment;

import com.teamspace.android.common.ui.SingleFragmentActivity;

public class MessageListActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new MessageListFragment();
	}
}
