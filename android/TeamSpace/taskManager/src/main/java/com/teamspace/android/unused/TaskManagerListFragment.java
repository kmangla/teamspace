package com.teamspace.android.unused;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.teamspace.android.R;
import com.teamspace.android.common.ui.TaskManagerApplication;
import com.teamspace.android.employee.ui.EmployeeAddEditActivity;
import com.teamspace.android.models.Employee;
import com.teamspace.android.utils.Constants;

public class TaskManagerListFragment extends ListFragment {

	private ArrayList<Employee> employees;

	private class EmployeeListAdapter extends ArrayAdapter<Employee> {
		public EmployeeListAdapter(ArrayList<Employee> employees) {
			super(getActivity(), android.R.layout.simple_list_item_1, employees);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (null == convertView) {
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.employee_row, null);
			}
			Employee e = getItem(position);
			TextView employeeName = (TextView) convertView
					.findViewById(R.id.employee_name);
			employeeName.setText(e.getName());
			TextView employeeStatus = (TextView) convertView
					.findViewById(R.id.employee_status);
			employeeStatus.setText(getEmployeeStatus(e));
			TextView employeeUnreadReplies = (TextView) convertView
					.findViewById(R.id.employee_unread_replies);
			int unreadCount = ((TaskManagerApplication) getActivity()
					.getApplication()).getUnreadRepliesForEmployee(e);
			if (unreadCount > 0) {
				employeeUnreadReplies.setVisibility(View.VISIBLE);
				employeeUnreadReplies.setText(String.valueOf(unreadCount));
			} else {
				employeeUnreadReplies.setVisibility(View.GONE);
			}

			return convertView;
		}

		private String getEmployeeStatus(Employee e) {
			int incompleteCount = ((TaskManagerApplication) getActivity()
					.getApplication()).getIncompleteTasksCount(e);
			int pendingCount = ((TaskManagerApplication) getActivity()
					.getApplication()).getPendingTasksCount(e);
			return incompleteCount + " tasks, " + pendingCount + " pending";
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("MyActivity", "MyClass.getView() � setup ");
		employees = ((TaskManagerApplication) getActivity().getApplication())
				.getEmployees();
		EmployeeListAdapter adapter = new EmployeeListAdapter(employees);
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
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.layout.employee_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		Employee employee = (Employee) getListAdapter().getItem(info.position);
		switch (item.getItemId()) {
		case R.id.edit_employee:
			Intent i = new Intent(getActivity(), EmployeeAddEditActivity.class);
			i.putExtra(Constants.EMPLOYEE_ID, employee.getId());
			i.putExtra(Constants.EMPLOYEE_NAME, employee.getName());
			i.putExtra(Constants.EMPLOYEE_PHONE, employee.getNumber());
			startActivityForResult(i, 0);
			return true;
		case R.id.delete_employee:
			((TaskManagerApplication)getActivity().getApplication()).deleteEmployee(employee);
			notifyDataSetChanged();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent i = new Intent(getActivity(), TaskListActivityUnused.class);
		@SuppressWarnings("unchecked")
		Employee e = ((EmployeeListAdapter) getListAdapter()).getItem(position);
		i.putExtra(Constants.EMPLOYEE_ID, e.getId());
		startActivityForResult(i, 0);
	}

	public void notifyDataSetChanged() {
		if (getActivity() != null) {
			employees = ((TaskManagerApplication) getActivity().getApplication())
					.getEmployees();
			EmployeeListAdapter adapter = new EmployeeListAdapter(employees);
			setListAdapter(adapter);
		}
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		notifyDataSetChanged();
	}
}
