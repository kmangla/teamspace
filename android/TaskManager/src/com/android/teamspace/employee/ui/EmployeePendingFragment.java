/**
 * Fragment for showing Employees Sorted by Pending Task Status
 */

package com.android.teamspace.employee.ui;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.teamspace.R;
import com.android.teamspace.caching.DatabaseCache;
import com.android.teamspace.common.ui.TaskManagerApplication;
import com.android.teamspace.models.Employee;
import com.android.teamspace.models.EmployeeState;
import com.android.teamspace.tasklist.ui.TaskListActivity;
import com.android.teamspace.utils.Constants;


public class EmployeePendingFragment extends ListFragment {

	ArrayList<EmployeeState> employeeStates;

	private class EmployeePendingListAdapter extends ArrayAdapter<EmployeeState> {
		public EmployeePendingListAdapter(ArrayList<EmployeeState> employeeStates) {
			super(getActivity(), android.R.layout.simple_list_item_1, employeeStates);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (null == convertView) {
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.employee_pending_row, null);
			}

			EmployeeState s = getItem(position);
			TaskManagerApplication context = (TaskManagerApplication) getActivity()
					.getApplication();
			Employee e = context.getEmployee(s.getEmployeeID());
			TextView employeeName = (TextView) convertView
					.findViewById(R.id.employee_pending_name);
			employeeName.setText(e.getName());
			TextView employeeStatus = (TextView) convertView
					.findViewById(R.id.employee_pending_status);
			if (s.isReplyReceived()) {
			employeeStatus.setText("No Reminder Pending");
			employeeStatus.setTextColor(Color.parseColor("#669900"));
			} else {
			employeeStatus.setText("Reminder Pending since "
					+ DateFormat.format("dd/MM",
							s.getTaskSentReminderTime() * 1000L));
			employeeStatus.setTextColor(Color.RED);

			}

			return convertView;
		}

	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		employeeStates = DatabaseCache.getInstance(getActivity()).getAllEmployeeStates();
		// Sorting Employee States in order of time since unreplied reminders have been
		// sent.
		Collections.sort(employeeStates);
		EmployeePendingListAdapter adapter = new EmployeePendingListAdapter(
				employeeStates);
		setListAdapter(adapter);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent i = new Intent(getActivity(), TaskListActivity.class);
		@SuppressWarnings("unchecked")
		EmployeeState e = ((EmployeePendingListAdapter) getListAdapter()).getItem(position);
		i.putExtra(Constants.EMPLOYEE_ID, e.getEmployeeID());
		startActivityForResult(i, 0);
	}
}