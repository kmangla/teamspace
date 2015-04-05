/**
 * This is the Main Activity Page for the TaskManager.
 */

package com.android.teamspace.common.ui;

import java.util.ArrayList;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.android.teamspace.R;
import com.android.teamspace.caching.DataManager;
import com.android.teamspace.caching.DataManagerCallback;
import com.android.teamspace.employee.ui.EmployeeAddEditActivity;
import com.android.teamspace.employee.ui.EmployeePendingFragment;
import com.android.teamspace.employee.ui.EmployeeScore28DaysFragment;
import com.android.teamspace.employee.ui.EmployeeScore7DaysFragment;
import com.android.teamspace.employee.ui.EmployeeScoreFragment;
import com.android.teamspace.models.Task;

public class TaskManagerActivity extends
		android.support.v7.app.ActionBarActivity {

	Button createEmployee;
	// Fragment for List of Employees
	TaskManagerListFragment fragment1 = new TaskManagerListFragment();
	// Fragment for List of Employees sorted by Scores in past 7 days
	EmployeeScoreFragment fragment2 = new EmployeeScore7DaysFragment();
	// Fragment for List of Employees sorted by Scores in past 28 days
	EmployeeScoreFragment fragment3 = new EmployeeScore28DaysFragment();
	// Fragment for List of Employees sorted by pending Task Status
	EmployeePendingFragment fragment4 = new EmployeePendingFragment();
	// Fragment for Owner Preferences
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	ActionBar.Tab tab1;
	ActionBar.Tab tab2;
	ActionBar.Tab tab3;

	// Listener for Menu Drawer clicks.
	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView parent, View view, int position,
				long id) {
			selectItem(position);
		}

		/** Swaps fragments in the main content view */
		private void selectItem(int position) {
			Fragment fragment;
			switch(position) {
			case 0: 
				fragment = fragment1;
				removeActionBar();
				break;
			case 1:
				fragment = fragment4;
				setupActionBar();
				break;
			default:
				fragment = fragment1;
				removeActionBar();
				break;
			}
			FragmentManager fragmentManager = getSupportFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.employeeListContainer, fragment).commit();

			// Highlight the selected item, update the title, and close the
			// drawer
			mDrawerList.setItemChecked(position, true);
			mDrawerLayout.closeDrawer(mDrawerList);
		}
	}
	
	// Class to enable switching of the Tabs
	private class MyTabListener implements ActionBar.TabListener {
		Fragment fragment;

		public MyTabListener(Fragment fragment) {
			this.fragment = fragment;
		}

		@Override
		public void onTabSelected(android.support.v7.app.ActionBar.Tab tab,
				FragmentTransaction ft) {
			ft.replace(R.id.employeeListContainer, fragment);
		}

		@Override
		public void onTabUnselected(android.support.v7.app.ActionBar.Tab tab,
				FragmentTransaction ft) {
			ft.remove(fragment);
		}

		@Override
		public void onTabReselected(android.support.v7.app.ActionBar.Tab arg0,
				FragmentTransaction arg1) {
			// TODO Auto-generated method stub

		}

	}

	private void setupActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	}

	private void removeActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.employee_list_activity);
		
		// Setting up the Action Bar
		ActionBar actionBar = getSupportActionBar();
		tab1 = actionBar.newTab().setText("7 DAYS");
		tab2 = actionBar.newTab().setText("28 DAYS");
		tab3 = actionBar.newTab().setText("PENDING");

		
		tab1.setTabListener((ActionBar.TabListener) new MyTabListener(fragment2));
		tab2.setTabListener((ActionBar.TabListener) new MyTabListener(fragment3));
		tab3.setTabListener((ActionBar.TabListener) new MyTabListener(fragment4));
		
		actionBar.addTab(tab3);
		actionBar.addTab(tab1);
		actionBar.addTab(tab2);

		// Setting up the Menu Drawer
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		String[] navItems = getResources().getStringArray(R.array.nav_array);
		mDrawerList.setBackgroundColor(Color.parseColor("#C0C0C0"));

		// Set the adapter for the list view
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_list_item, navItems));
		// Set the list's click listener
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		setTitle("Employees");
		FragmentManager manager = getSupportFragmentManager();
		Fragment fragment = manager
				.findFragmentById(R.id.employeeListContainer);

		// Add Listener for the Create Employee Button
		createEmployee = (Button) findViewById(R.id.employee_create_button);

		createEmployee.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(getActivity(), EmployeeAddEditActivity.class);
				startActivityForResult(i, 0);
			}
		});
		if (fragment == null) {

			manager.beginTransaction()
					.add(R.id.employeeListContainer, fragment1).commit();
		}
		
		Log.i("KARAN", "Got here");

	}
	
	@Override
	protected void onResume() {
		//TODO: This is broken code, needs to be fixed
		fragment1.notifyDataSetChanged();
		super.onResume();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//TODO: This is broken code, needs to be fixed
		fragment1.notifyDataSetChanged();
	}

	public FragmentActivity getActivity() {
		return this;
	}
}
