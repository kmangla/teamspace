package com.teamspace.android.employee.ui;

import android.support.v4.app.Fragment;

import com.teamspace.android.common.ui.SingleFragmentActivity;
import com.teamspace.android.tasklist.ui.AllTasksListViewFragment;

/**
 * Created by vivek on 2/22/15.
 */
public class EmployeeListViewActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new EmployeeListViewFragment();
    }
}
