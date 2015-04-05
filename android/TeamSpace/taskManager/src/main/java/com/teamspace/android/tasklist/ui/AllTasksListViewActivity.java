package com.teamspace.android.tasklist.ui;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.teamspace.android.R;
import com.teamspace.android.common.ui.DebugActivity;
import com.teamspace.android.common.ui.SettingsActivity;
import com.teamspace.android.common.ui.SingleFragmentActivity;
import com.teamspace.android.registration.ui.PhoneNumberRegistrationFragment;

/**
 * Created by vivek on 2/22/15.
 */
public class AllTasksListViewActivity extends SingleFragmentActivity {
    private AllTasksListViewFragment fragment;

    @Override
    protected Fragment createFragment() {
        fragment = new AllTasksListViewFragment();
        return fragment;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_refresh:
                fragment.refreshUI();
                return true;
            case R.id.action_settings:
                launchSettingsActivity();
                return true;
            case R.id.action_debug:
                launchDebugActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void launchSettingsActivity() {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    private void launchDebugActivity() {
        Intent i = new Intent(this, DebugActivity.class);
        startActivity(i);
    }
}
