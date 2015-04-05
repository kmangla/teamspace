package com.teamspace.android.common.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.teamspace.android.registration.ui.PhoneNumberRegistrationActivity;
import com.teamspace.android.tasklist.ui.AllTasksListViewActivity;
import com.teamspace.android.utils.Utils;

/**
 * Created by vivek on 2/22/15.
 */
public class LauncherActivity extends FragmentActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Utils.isStringNotEmpty(Utils.getSignedInUserId())) {
            Intent i = new Intent(this, AllTasksListViewActivity.class);
            startActivity(i);
        } else {
            Intent i = new Intent(this, PhoneNumberRegistrationActivity.class);
            startActivity(i);
        }

        finish();
    }
}
