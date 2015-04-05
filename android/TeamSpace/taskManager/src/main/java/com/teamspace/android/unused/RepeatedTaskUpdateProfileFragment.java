/**
 * Fragment to update repeated tasks for a user.
 */
package com.teamspace.android.unused;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.teamspace.android.R;
import com.teamspace.android.common.ui.TaskManagerApplication;
import com.teamspace.android.models.Employee;
import com.teamspace.android.models.Task;
import com.teamspace.android.models.TaskUpdate;

public class RepeatedTaskUpdateProfileFragment extends Fragment {
	private Task task;
	private EditText descriptionField;
	private TextView employeeName;
	private Employee employee;
	private Button saveButton;
	private Spinner frequencySpinner;
	private Spinner daySpinner;
	private CheckBox isCompleted;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		task = new Task();
		Bundle extras = getActivity().getIntent().getExtras();
		long value = 0;
		if (extras != null) {
			value = extras.getLong("TASK_ID");
		}
		TaskManagerApplication applicationContext = ((TaskManagerApplication) getActivity()
				.getApplication());
		task = applicationContext.getTaskByID(value);
		getActivity().setTitle("Task #" + task.getId());
		String employeeID = task.getEmployeeID();
		employee = applicationContext.getEmployee(employeeID);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_repeated_task, container, false);

		descriptionField = (EditText) v.findViewById(R.id.repeated_task_description);
		employeeName = (TextView) v.findViewById(R.id.repeated_task_employee);

		descriptionField.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence c, int start, int before,
					int count) {
				task.setDescription(c.toString());
			}

			public void beforeTextChanged(CharSequence c, int start, int count,
					int after) {

			}

			public void afterTextChanged(Editable c) {

			}
		});

		descriptionField.setText(task.getDescription());
		employeeName.setText(employee.getName());
		frequencySpinner = (Spinner) v.findViewById(R.id.repeated_task_spinner);
		frequencySpinner.setSelection(getSpinnerPosition(task.getRepeatFrequency()));

		Log.i("task", task.getRepeatDay() + "");
		daySpinner = (Spinner) v.findViewById(R.id.repeated_task_day_spinner);
		daySpinner.setSelection((int)task.getRepeatDay() - 1);
		
		frequencySpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() 
		{
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) 
		    {
		    	
		    	ArrayAdapter<CharSequence> adapter;
		    	if (position == 0) {
		    		adapter = ArrayAdapter.createFromResource(getActivity(),
		    				R.array.weekly_task_day_array, android.R.layout.simple_spinner_item);
		    	} else {
		    		adapter = ArrayAdapter.createFromResource(getActivity(),
		    				R.array.monthly_task_day_array, android.R.layout.simple_spinner_item);
		    	}
		    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		    	daySpinner.setAdapter(adapter);
				daySpinner.setSelection((int)task.getRepeatDay() - 1);
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) 
		    {
		    }
		});

		
		saveButton = (Button) v.findViewById(R.id.task_button);
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				task.setRepeatFrequency(getNumericFrequencyFromChoice(String
						.valueOf(frequencySpinner.getSelectedItem())));
				task.setRepeatDay(daySpinner.getSelectedItemPosition() + 1);
				TaskUpdate.getInstance(v.getContext()).updateTask(task);
				getFragmentActivity().setResult(Activity.RESULT_OK);
				getFragmentActivity().finish();
			}
		});

		isCompleted = (CheckBox) v.findViewById(R.id.repeated_task_is_completed);
		isCompleted.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				task.setCompletedCurrent(isCompleted.isChecked());
			}
		});
		if (task.isCompletedCurrent()) {
			isCompleted.setChecked(true);
		} else {
			isCompleted.setChecked(false);
		}

		return v;
	}

	private long getNumericFrequencyFromChoice(String choice) {
		switch (choice) {
		case "Weekly":
			return 86400L * 7;
		case "Monthly":
			return 86400L * 31;
		default:
			return 86400L * 7;
		}
	}

	private int getSpinnerPosition(long frequency) {
		if (frequency == 86400L * 7) {
			return 0;
		} else if (frequency == 86400L * 31) {
			return 1;
		}
		return 0;
	}

	public FragmentActivity getFragmentActivity() {
		return this.getActivity();
	}

}