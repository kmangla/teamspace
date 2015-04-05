/**
 * Fragment for Creating/Updating information about an Employee
 */

package com.android.teamspace.tasklist.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.teamspace.R;
import com.android.teamspace.caching.DataManager;
import com.android.teamspace.caching.DataManagerCallback;
import com.android.teamspace.caching.DatabaseCache;
import com.android.teamspace.common.ui.TaskManagerApplication;
import com.android.teamspace.models.MigratedEmployee;
import com.android.teamspace.models.MigratedTask;
import com.android.teamspace.utils.Constants;
import com.android.teamspace.utils.Utils;

public class TaskAddEditFragment extends Fragment implements
		OnItemSelectedListener {

	private MigratedTask task;
	EditText taskTitleEditText;
	EditText taskDescriptionEditText;
	Spinner freqSpinner;
	Spinner empSpinner;

	Button saveButton;

	String taskId = "";
	String taskTitle = "";
	String taskEmployeeName = "";

	ArrayList<MigratedEmployee> allEmployees;
	String[] allEmployeeNames;
	private boolean hideKeyboard;
	private boolean editMode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Since we need to fetch task data from the database which might
		// require some time
		// and must be done on background thread, we allow the caller to pass us
		// few basic
		// information like task title which we can instantly show in the UI
		// while more details about the task are fetched from the database.
		Bundle extras = getActivity().getIntent().getExtras();
		if (extras != null) {
			taskId = extras.getString(Constants.TASK_ID);
			taskTitle = extras.getString(Constants.TASK_TITLE);
			taskEmployeeName = extras.getString(Constants.EMPLOYEE_NAME);
			hideKeyboard = extras.getBoolean(Constants.HIDE_KEYBOARD);
			if (Utils.isStringNotEmpty(taskId)) {
				editMode = true;
			}
		}

		final TaskManagerApplication applicationContext = ((TaskManagerApplication) getFragmentActivity()
				.getApplication());
		if (Utils.isStringNotEmpty(taskId)) {
			AsyncTask<String, Void, MigratedTask> asyncTask = new AsyncTask<String, Void, MigratedTask>() {
				@Override
				protected MigratedTask doInBackground(String... taskIds) {
					Utils.log("Trying to fetch task details from database cache for task id: "
							+ taskIds[0]);
					return DatabaseCache.getInstance(applicationContext)
							.getMigratedTaskBlockingCall(taskIds[0]);
				}

				@Override
				protected void onPostExecute(MigratedTask cachedTask) {
					task = cachedTask;
					refreshUIForTask();
				}
			};
			asyncTask.execute(taskId);
		} else {
			task = new MigratedTask();
		}

		getActivity().setTitle(R.string.task_detail_page);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.task_add_fragment, container, false);

		taskTitleEditText = (EditText) v.findViewById(R.id.task_title);
		taskDescriptionEditText = (EditText) v.findViewById(R.id.task_details);

		if (Utils.isStringNotEmpty(taskTitle)) {
			taskTitleEditText.setText(taskTitle);
		}

		// Add spinner for selecting employee
		empSpinner = (Spinner) v.findViewById(R.id.employee_spinner);
		allEmployees = DatabaseCache.getInstance(v.getContext())
				.getMigratedEmployeesBlockingCall();
		allEmployeeNames = new String[allEmployees.size()];

		for (int i = 0; i < allEmployees.size(); i++) {
			allEmployeeNames[i] = allEmployees.get(i).getName();
		}

		// Create an ArrayAdapter using the list of employees
		ArrayAdapter<String> empAdapter = new ArrayAdapter<String>(
				getActivity(), R.layout.task_spinner_item);
		empAdapter.addAll(allEmployeeNames);
		empAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		empSpinner.setAdapter(empAdapter);
		empSpinner.setOnItemSelectedListener(this);

		// Add spinner for selecting frequency
		freqSpinner = (Spinner) v.findViewById(R.id.frequency);

		// Create an ArrayAdapter using the frequency array
		ArrayAdapter<CharSequence> freqAdapter = ArrayAdapter
				.createFromResource(getActivity(), R.array.frequency_array,
						R.layout.task_spinner_item);
		freqAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		freqSpinner.setAdapter(freqAdapter);
		freqSpinner.setOnItemSelectedListener(this);

		saveButton = (Button) v.findViewById(R.id.save_button);
		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(final View v) {
				TaskManagerApplication applicationContext = ((TaskManagerApplication) getFragmentActivity()
						.getApplication());
				DataManager dataMgr = DataManager
						.getInstance(applicationContext);

				task.setTitle(taskTitleEditText.getText().toString());
				task.setDescription(taskDescriptionEditText.getText()
						.toString());
				MigratedEmployee emp = allEmployees.get(empSpinner
						.getSelectedItemPosition());
				task.setEmployeeID(emp.getEmployeeID());
				task.setEmployeeName(emp.getName());
				task.setEmployeeNumber(emp.getPhoneWithCountryCode());

				long freq = Constants.DAILY;
				// Convert freq to seconds
				switch (freqSpinner.getSelectedItemPosition()) {
				case 0:
					freq = Constants.DAILY;
					break;
				case 1:
					freq = Constants.WEEKLY;
					break;
				case 2:
					freq = Constants.BIWEEKLY;
					break;
				case 3:
					freq = Constants.MONTHLY;
					break;
				default:
					freq = Constants.DAILY;
					break;
				}

				task.setFrequency(freq);

				if (Utils.isStringNotEmpty(taskId)) {
					task.setTaskID(taskId);
					dataMgr.updateTask(task, new DataManagerCallback() {

						@Override
						public void onSuccess(String response) {
						}

						@Override
						public void onFailure(String response) {
							// Notify user about the error
							Toast.makeText(
									v.getContext(),
									v.getContext()
											.getResources()
											.getString(
													R.string.error_employee_update_failed),
									Toast.LENGTH_SHORT).show();
						}
					});
				} else {
					task.setTaskID(Constants.EMPTY_STRING);
					task.setStatus(Constants.OPEN);
					dataMgr.createTask(task, null);
				}

				Intent newIntent = new Intent();
				newIntent.putExtra(Constants.TASK_ID, task.getTaskID());
				newIntent.putExtra(Constants.TASK_TITLE, task.getTitle());
				newIntent.putExtra(Constants.DETAILS, task.getDescription());
				newIntent.putExtra(Constants.FREQUENCY, task.getFrequency());
				newIntent.putExtra(Constants.EMPLOYEE_ID, task.getEmployeeID());
				String name = allEmployees.get(
						empSpinner.getSelectedItemPosition()).getName();
				String number = allEmployees.get(
						empSpinner.getSelectedItemPosition())
						.getPhoneWithCountryCode();
				newIntent.putExtra(Constants.EMPLOYEE_NAME, name);
				newIntent.putExtra(Constants.EMPLOYEE_PHONE, number);

				getFragmentActivity().setResult(Activity.RESULT_OK, newIntent);
				getFragmentActivity().finish();
			}
		});

		return v;
	}

	public void onResume() {
		super.onResume();

		// Check if we need to show the keyboard
		if (hideKeyboard) {
			InputMethodManager imm = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(taskTitleEditText.getWindowToken(), 0);
		} else {
			InputMethodManager imm = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(taskTitleEditText, 0);
		}
	}

	private void refreshUIForTask() {
		taskTitleEditText.setText(task.getTitle());
		taskDescriptionEditText.setText(task.getDescription());

		for (int i = 0; i < allEmployees.size(); i++) {
			if (allEmployees.get(i).getEmployeeID()
					.equalsIgnoreCase(task.getEmployeeID())) {
				empSpinner.setSelection(i);
				break;
			}
		}

		// Convert seconds to frequency (daily, weekly, monthly etc.)
		int freq = 0; // Daily
		if (task.getFrequency() <= Constants.DAILY) {
			freq = 0;
		} else if (task.getFrequency() <= Constants.WEEKLY) {
			freq = 1;
		} else if (task.getFrequency() <= Constants.BIWEEKLY) {
			freq = 2;
		} else if (task.getFrequency() <= Constants.MONTHLY) {
			freq = 3;
		}

		freqSpinner.setSelection(freq);
	}

	public FragmentActivity getFragmentActivity() {
		return this.getActivity();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub

	}
}
