/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.teamspace.android.common.ui;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.teamspace.android.R;
import com.teamspace.android.employee.ui.EmployeeListViewFragment;
import com.teamspace.android.interfaces.ActionBarResponderInterface;
import com.teamspace.android.tasklist.ui.AllTasksListViewFragment;
import com.teamspace.android.tasklist.ui.TasksForEmployeeListViewFragment;
import com.teamspace.android.utils.Constants;
import com.teamspace.android.utils.Utils;

public class MainActivityWithTabs extends FragmentActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * three primary sections of the app. We use a {@link android.support.v4.app.FragmentPagerAdapter}
     * derivative, which will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    AppSectionsPagerAdapter mAppSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will display the three primary sections of the app, one at a
     * time.
     */
    ViewPager mViewPager;

	private int mCurrentTabPosition;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        
        // Set default values of user preferences (without overriding them).
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();

        // Specify that the Home/Up button should not be enabled, since there is no hierarchical
        // parent.
        actionBar.setHomeButtonEnabled(false);
        
        // Specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager, attaching the adapter and setting up a listener for when the
        // user swipes between sections.
        mViewPager = (ViewPager) findViewById(R.id.pager);        
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
        
        // If user has selected a different tab to be the default, switch to that tab.
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		String defaultTab = sharedPref.getString(
				SettingsPreferenceFragment.KEY_PREF_DEFAULT_TAB,
				SettingsPreferenceFragment.KEY_DEFAULT_TAB);
		
		if (!defaultTab.equalsIgnoreCase(SettingsPreferenceFragment.KEY_DEFAULT_TAB)) {
			int position = getDefaultTabPosition(defaultTab);
			Tab tab = actionBar.getTabAt(position);
			actionBar.selectTab(tab);
		}
    }

    private int getDefaultTabPosition(String defaultTab) {
    	String[] tabs = getResources().getStringArray(R.array.tabs);
    	if (defaultTab.equalsIgnoreCase(tabs[0])) {
    		return 0;
    	} else if (defaultTab.equalsIgnoreCase(tabs[1])) {
    		return 1;
    	} else if (defaultTab.equalsIgnoreCase(tabs[2])) {
    		return 2;
//    	} else if (defaultTab.equalsIgnoreCase(tabs[3])) {
//    		return 3;
    	}
    	
    	return 0;
	}

	@Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
        mCurrentTabPosition = tab.getPosition();
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {
    	ArrayList<Fragment> fragments = new ArrayList<Fragment>();

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            fragments.add(new AllTasksListViewFragment());
            fragments.add(new EmployeeListViewFragment());
            TasksForEmployeeListViewFragment frag = new TasksForEmployeeListViewFragment();
            Bundle args = new Bundle();
            args.putString(Constants.EMPLOYEE_ID, Utils.getSignedInUserId());
            frag.setArguments(args);
            fragments.add(frag);
//            fragments.add(new ToDoListViewFragment());
//            fragments.add(new ChatListViewFragment());
//            fragments.add(new SettingsFragment());
        }

        @Override
        public Fragment getItem(int i) {
        	if (i < fragments.size()) {
        		return fragments.get(i);
        	}
        	
        	return new EmployeeListViewFragment();
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
        	String title = "";
            switch (position) {
            case 0:
            	title = "All Tasks";
            	break;
            case 1:
            	title = "Employees";
            	break;
            case 2:
            	title = "My Tasks";
            	break;
//            case 3:
//            	title = "Chat";
//            	break;
//            case 4:
//            	title = "More";
//            	break;
            }
            
            return title;
        }
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
//            case R.id.action_search:
//                openSearch();
//                return true;
//            case R.id.action_add:
//                addItem();
//                return true;
            case R.id.action_refresh:
                refreshUI();
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

    private void openSearch() {
    	Fragment fragment = mAppSectionsPagerAdapter.getItem(mCurrentTabPosition);
		if (fragment instanceof ActionBarResponderInterface) {
			ActionBarResponderInterface searchInterface = (ActionBarResponderInterface) fragment;
			searchInterface.search();
		}		
	}

	private void addItem() {
		Fragment fragment = mAppSectionsPagerAdapter.getItem(mCurrentTabPosition);
		if (fragment instanceof ActionBarResponderInterface) {
			ActionBarResponderInterface addInterface = (ActionBarResponderInterface) fragment;
			addInterface.addItem();
		}
	}
	
	private void refreshUI() {
		Fragment fragment = mAppSectionsPagerAdapter.getItem(mCurrentTabPosition);
		if (fragment instanceof ActionBarResponderInterface) {
			ActionBarResponderInterface refreshInterface = (ActionBarResponderInterface) fragment;
			refreshInterface.refreshUI();
		}
	}
}
