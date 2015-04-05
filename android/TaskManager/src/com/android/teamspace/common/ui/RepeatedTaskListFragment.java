/**
 * Fragment to show repeated tasks list for a user.
 */
package com.android.teamspace.common.ui;

import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.android.teamspace.R;
import com.android.teamspace.caching.DatabaseCache;
import com.android.teamspace.models.Employee;
import com.android.teamspace.models.EmployeeState;
import com.android.teamspace.models.Task;
import com.android.teamspace.utils.Constants;

public class RepeatedTaskListFragment extends ListFragment {
	private ArrayList<Task> tasks;
	private String value = "";

	private class TaskListAdapter extends ArrayAdapter<Task> {
		public TaskListAdapter(ArrayList<Task> tasks) {
			super(getActivity(), android.R.layout.simple_list_item_1, tasks);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (null == convertView) {
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.task_row, null);
			}
			Task t = getItem(position);
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
					taskStatus.setText("Is Paused");
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
		tasks = DatabaseCache.getInstance(getActivity())
				.getRepeatedTasks(value);
		Employee e = ((TaskManagerApplication) getActivity().getApplication())
				.getEmployee(value);

		Collections.sort(tasks, Collections.reverseOrder());
		TaskListAdapter adapter = new TaskListAdapter(tasks);
		setListAdapter(adapter);
		getActivity().setTitle("Tasks for " + e.getName());
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent i = new Intent(getActivity(), TaskUpdateActivity.class);
		@SuppressWarnings("unchecked")
		Task task = ((TaskListAdapter) getListAdapter()).getItem(position);
		i.putExtra("TASK_ID", task.getId());
		startActivityForResult(i, 0);
	}

	public void notifyDataSetChanged() {
		Activity activity = getActivity();
		if (activity == null) {
			return;
		}
		tasks = DatabaseCache.getInstance(getActivity())
				.getRepeatedTasks(value);
		Collections.sort(tasks, Collections.reverseOrder());
		TaskListAdapter adapter = new TaskListAdapter(tasks);
		setListAdapter(adapter);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		notifyDataSetChanged();
	}
}
