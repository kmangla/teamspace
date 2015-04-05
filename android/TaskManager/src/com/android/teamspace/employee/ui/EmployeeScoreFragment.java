/**
 * Fragment for showing Employees Sorted by tasks completed in previous n days
 */

package com.android.teamspace.employee.ui;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.teamspace.R;
import com.android.teamspace.common.ui.TaskManagerApplication;
import com.android.teamspace.models.Employee;
import com.android.teamspace.models.EmployeeScore;
import com.android.teamspace.models.EmployeeScoreRanking;
import com.android.teamspace.tasklist.ui.TaskListActivity;
import com.android.teamspace.utils.Constants;

public abstract class EmployeeScoreFragment extends ListFragment {

	ArrayList<EmployeeScore> employeeScores;

	private class EmployeeScoreListAdapter extends ArrayAdapter<EmployeeScore> {
		public EmployeeScoreListAdapter(ArrayList<EmployeeScore> employees) {
			super(getActivity(), android.R.layout.simple_list_item_1, employees);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (null == convertView) {
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.employee_score_row, null);
			}

			EmployeeScore s = getItem(position);
			TaskManagerApplication context = (TaskManagerApplication) getActivity()
					.getApplication();
			Employee e = context.getEmployee(s.getEmployeeID());
			TextView employeeName = (TextView) convertView
					.findViewById(R.id.employee_score_name);
			employeeName.setText(e.getName());
			TextView employeeStatus = (TextView) convertView
					.findViewById(R.id.employee_score_status);
			employeeStatus.setText(getEmployeeStatus(s));

			return convertView;
		}

		private String getEmployeeStatus(EmployeeScore s) {
			return s.getNumUpdates() + " tasks updated, " + s.getNumClosed()
					+ " tasks completed";
		}

	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Rank employees by tasks finished in the past n days. 
		employeeScores = EmployeeScoreRanking.getRankedEmployees(getActivity(),
				getNumDays());
		EmployeeScoreListAdapter adapter = new EmployeeScoreListAdapter(
				employeeScores);
		setListAdapter(adapter);
	}

	@Override
	/**
	 * On clicking on an employee, open their tasks.
	 */
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent i = new Intent(getActivity(), TaskListActivity.class);
		@SuppressWarnings("unchecked")
		EmployeeScore e = ((EmployeeScoreListAdapter) getListAdapter()).getItem(position);
		i.putExtra(Constants.EMPLOYEE_ID, e.getEmployeeID());
		startActivityForResult(i, 0);
	}

	abstract public int getNumDays();
}
