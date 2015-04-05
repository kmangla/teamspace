package com.teamspace.android.unused;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.teamspace.android.R;
import com.teamspace.android.utils.Constants;

public class TaskListActivityUnused extends android.support.v7.app.ActionBarActivity {

	Button createTask;
	long employeeID;
	TaskListFragment fragment1 =  new TaskListFragment();
	CompletedTaskFragment fragment2 = new CompletedTaskFragment();
	RepeatedTaskListFragment fragment3 = new RepeatedTaskListFragment();
	ActionBar.Tab tab1;
	ActionBar.Tab tab2;
	ActionBar.Tab tab3;
	
	private class MyTabListener implements ActionBar.TabListener {
		Fragment fragment;
		
		public MyTabListener(Fragment fragment) {
			this.fragment = fragment;
		}
		
		@Override
	    public void onTabSelected(android.support.v7.app.ActionBar.Tab tab, FragmentTransaction ft) {
			ft.replace(R.id.taskListContainer, fragment);
		}
	    
		@Override
		public void onTabUnselected(android.support.v7.app.ActionBar.Tab tab, FragmentTransaction ft) {
			ft.remove(fragment);
		}
		
		@Override
		public void onTabReselected(android.support.v7.app.ActionBar.Tab arg0,
				FragmentTransaction arg1) {
			// TODO Auto-generated method stub
			
		}

	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	 Log.i("test", "onCreate called");
	 super.onCreate(savedInstanceState);
	 setContentView(R.layout.task_list_activity);
	 
     ActionBar actionBar = getSupportActionBar();
     actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
     
     tab1 = actionBar.newTab().setText("Tasks");
     tab2 = actionBar.newTab().setText("Completed");
     tab3 = actionBar.newTab().setText("Repeated");

     
     tab1.setTabListener((ActionBar.TabListener) new MyTabListener(fragment1));
     tab2.setTabListener((ActionBar.TabListener) new MyTabListener(fragment2));
     tab3.setTabListener((ActionBar.TabListener) new MyTabListener(fragment3));
     
     actionBar.addTab(tab1);
     actionBar.addTab(tab3);
     actionBar.addTab(tab2);	 
	 
	 Bundle extras = this.getIntent().getExtras();
	 if (extras != null) {
	    employeeID = extras.getLong(Constants.EMPLOYEE_ID);
	 }

	 
	 createTask = (Button)findViewById(R.id.tb);
	 
	 createTask.setOnClickListener(new View.OnClickListener() {
		 
		@Override
		public void onClick(View arg0) {
		  Intent i = new Intent(getActivity(), TaskActivity.class);
		  i.putExtra(Constants.EMPLOYEE_ID, employeeID);
		  startActivityForResult(i, 0);
		}
	 });
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	  if (fragment1 != null) {
	  }
	  if (fragment2 != null) {
		  fragment2.notifyDataSetChanged();
	  }
	}
	
	public FragmentActivity getActivity() {
	  return this;
	}
}
