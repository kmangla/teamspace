package com.teamspace.android.registration.ui;

import android.support.v4.app.Fragment;

import com.teamspace.android.common.ui.SingleFragmentActivity;
import com.teamspace.android.tasklist.ui.TaskAddEditFragment;

/**
 * Created by vivek on 2/22/15.
 */
public class PhoneNumberRegistrationActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new PhoneNumberRegistrationFragment();
    }
}
