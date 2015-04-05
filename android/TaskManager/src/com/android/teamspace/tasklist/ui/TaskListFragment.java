package com.android.teamspace.tasklist.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.android.teamspace.R;
import com.android.teamspace.caching.DataManager;
import com.android.teamspace.caching.DataManagerCallback;
import com.android.teamspace.caching.DatabaseCache;
import com.android.teamspace.common.ui.DatePickerFragment;
import com.android.teamspace.common.ui.TaskManagerApplication;
import com.android.teamspace.common.ui.TaskUpdateActivity;
import com.android.teamspace.models.Employee;
import com.android.teamspace.models.EmployeeState;
import com.android.teamspace.models.Task;
import com.android.teamspace.utils.Constants;

public class TaskListFragment extends ListFragment {
	
	private class ListItem {
		private int type;
		private Task task;
		private String header;

		public int getType() {
			return type;
		}
		public void setType(int type) {
			this.type = type;
		}
		public Task getTask() {
			return task;
		}
		public void setTask(Task task) {
			this.task = task;
		}
		public String getHeader() {
			return header;
		}
		public void setHeader(String header) {
			this.header = header;
		}
	}
	
	private ArrayList<Task> tasks;
	private ArrayList<ListItem> allSections;
	private String value = "";

	private class TaskListAdapter extends ArrayAdapter<ListItem> {
		public TaskListAdapter(ArrayList<ListItem> items) {
			super(getActivity(), android.R.layout.simple_list_item_1, items);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ListItem l = getItem(position);
			if (l.getType() == 1) {
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.task_row, null);
			} else {
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.section_header_row, null);
			}
			if (l.getType() == 2) {
				TextView header = (TextView) convertView
						.findViewById(R.id.taskListSectionHeader);
				header.setText(l.getHeader());
				return convertView;
			} 
			Log.i("test", position + " " + l.getType() + l.getTask()+ l.getHeader());
			Task t = l.getTask();
			TextView taskDescription = (TextView) convertView
					.findViewById(R.id.task_list_description);
			taskDescription.setText(t.getDescription());
			setTaskStatus(t, convertView);
			CheckBox isCompleted = (CheckBox) convertView
					.findViewById(R.id.task_list_is_completed);
			isCompleted.setChecked(t.isCompleted());
			TextView taskUnreadReplies = (TextView) convertView
					.findViewById(R.id.task_unread_replies);
			int unreadCount = ((TaskManagerApplication) getActivity()
					.getApplication()).getUnreadRepliesForTask(t);
			if (unreadCount > 0) {
				taskUnreadReplies.setVisibility(View.VISIBLE);
				taskUnreadReplies.setText(String.valueOf(unreadCount));
			} else {
				taskUnreadReplies.setVisibility(View.GONE);
			}
			return convertView;
		}

		private void setTaskStatus(Task t, View convertView) {
			TextView taskStatus = (TextView) convertView
					.findViewById(R.id.task_list_status);
			TaskManagerApplication applicationContext = ((TaskManagerApplication) getActivity()
					.getApplication());
			EmployeeState state = applicationContext.getEmployeeState(t
					.getEmployeeID());
			if (state.getLastTaskSentID() == t.getId()
					&& !state.isReplyReceived()) {
				taskStatus.setText("Reminder Sent on "
						+ DateFormat.format("hh:mmaa dd/MM",
								state.getTaskSentTime() * 1000L));
				taskStatus.setTextColor(Color.RED);
			} else {
				if (applicationContext.isTaskPaused(t)) {
					taskStatus.setText("Is Paused till "
							+ DateFormat.format("dd/MM", DatabaseCache
									.getInstance(applicationContext)
									.taskPausedTill(t) * 1000L));
					taskStatus.setTextColor(Color.parseColor("#FFBB33"));
				} else if (t.needsToBeSent()) {
					taskStatus.setText("Update Pending");
					taskStatus.setTextColor(Color.parseColor("#FFBB33"));
				} else {
					taskStatus.setText("Updated "
							+ DateFormat.format("hh:mmaa dd/MM",
									t.getLastSent() * 1000L));
					taskStatus.setTextColor(Color.parseColor("#669900"));
				}
			}
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras = getActivity().getIntent().getExtras();
		if (extras != null) {
			value = extras.getString(Constants.EMPLOYEE_ID);
		}
		setUpList();
		Employee e = ((TaskManagerApplication) getActivity().getApplication())
				.getEmployee(value);
		getActivity().setTitle("Tasks for " + e.getName());
	}
	
	private void setUpList() {
		// Ask DataManager for the data.
		DataManager.getInstance(getActivity()).fetchTasksForEmployee(value,
				new DataManagerCallback() {

					@Override
					public void onDataReceivedFromCache(String dataStoreKey) {
						if (dataStoreKey == null) {
							return;
						}
						ArrayList<Task> data = (ArrayList<Task>) DataManager
								.getInstance(getActivity())
								.retrieveData(dataStoreKey);
						setUpListHelper(data);
					}

					@Override
					public void onDataReceivedFromServer(String dataStoreKey) {
						if (dataStoreKey == null) {
							return;
						}
						ArrayList<Task> data = (ArrayList<Task>) DataManager
								.getInstance(getActivity())
								.retrieveData(dataStoreKey);
						setUpListHelper(data);
					}

				});
	}
	
	private void setUpListHelper(ArrayList<Task> data) {
		tasks = data;

		Collections.sort(tasks, Collections.reverseOrder());
		
		ArrayList<ListItem> dailyTasks = new ArrayList<ListItem>();
		ArrayList<ListItem> weeklyTasks = new ArrayList<ListItem>();
		ArrayList<ListItem> monthlyTasks = new ArrayList<ListItem>();
		
		Iterator<Task> it = tasks.iterator();
		while(it.hasNext()) {
			Task t = it.next();
			if (t.isRepeated()) {
				continue;
			}
			ListItem l = new ListItem();
			l.setTask(t);
			l.setType(1);
			if (t.getFrequency() == 86400L) {
				dailyTasks.add(l);
			} else if (t.getFrequency() == 86400L*7) {
				weeklyTasks.add(l);
			} else {
				monthlyTasks.add(l);
			}
		}
		
		allSections = new ArrayList<ListItem>();
		if (dailyTasks.size() != 0) {
			ListItem l = new ListItem();
			l.setType(2);
			l.setHeader("Daily");
			allSections.add(l);
			allSections.addAll(dailyTasks);
		}
		if (weeklyTasks.size() != 0) {
			ListItem l = new ListItem();
			l.setType(2);
			l.setHeader("Weekly");
			allSections.add(l);
			allSections.addAll(weeklyTasks);
		}
		if (monthlyTasks.size() != 0) {
			ListItem l = new ListItem();
			l.setType(2);
			l.setHeader("Monthly");
			allSections.add(l);
			allSections.addAll(monthlyTasks);
		}

		TaskListAdapter adapter = new TaskListAdapter(allSections);
		setListAdapter(adapter);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerForContextMenu(getListView());
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		ListItem item = (ListItem) getListAdapter().getItem(info.position);
		if (item.getType() == 2) {
			return;
		}
		Task task = item.getTask();
		MenuInflater inflater = getActivity().getMenuInflater();
		TaskManagerApplication context = (TaskManagerApplication) getActivity()
				.getApplication();
		if (context.isTaskPaused(task)) {
			inflater.inflate(R.layout.unpause_task_menu, menu);
		} else {
			inflater.inflate(R.layout.pause_task_menu, menu);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		ListItem l = (ListItem) getListAdapter().getItem(info.position);
		Task task = l.getTask();
		switch (item.getItemId()) {
		case R.id.pause_task:
			FragmentManager fm = getActivity().getSupportFragmentManager();
			new DatePickerFragment().setTask(task).setTaskFragment(this)
					.show(fm, "pause");
			return true;
		case R.id.unpause_task:
			TaskManagerApplication context = (TaskManagerApplication) getActivity()
					.getApplication();
			context.removeTaskPause(task);
			notifyDataSetChanged();
			return true;

		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent i = new Intent(getActivity(), TaskUpdateActivity.class);
		@SuppressWarnings("unchecked")
		ListItem item = ((TaskListAdapter) getListAdapter()).getItem(position);
		if (item.getType() == 1) {
			Task task = item.getTask();
			i.putExtra("TASK_ID", task.getId());	
			startActivityForResult(i, 0);
		} else {
			return;
		}
	}

	public void notifyDataSetChanged() {
		Activity activity = getActivity();
		if (activity == null) {
			return;
		}
		setUpList();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		notifyDataSetChanged();
	}
}
