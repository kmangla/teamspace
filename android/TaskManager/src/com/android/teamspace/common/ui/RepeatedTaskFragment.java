package com.android.teamspace.common.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.teamspace.R;
import com.android.teamspace.models.Employee;
import com.android.teamspace.models.Task;
import com.android.teamspace.utils.Constants;

public class RepeatedTaskFragment extends Fragment {
	private Task task;
	private EditText descriptionField;
	private TextView employeeName;
	private Employee employee;
	private Button saveButton;
	private Spinner frequencySpinner;
	private Spinner daySpinner;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		task = new Task();
		Bundle extras = getActivity().getIntent().getExtras();
		String value = "";
		if (extras != null) {
			value = extras.getString(Constants.EMPLOYEE_ID);
		}
		TaskManagerApplication applicationContext = ((TaskManagerApplication) getActivity()
				.getApplication());
		employee = applicationContext.getEmployee(value);
		task.setEmployeeID(value);
		task.setCompleted(false);
		task.setLastSent(Task.MIN_LAST_SENT);
		task.setRepeated(true);
		task.setCompletedCurrent(false);
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

		employeeName.setText(employee.getName());
		frequencySpinner = (Spinner) v.findViewById(R.id.repeated_task_spinner);
		daySpinner = (Spinner) v.findViewById(R.id.repeated_task_day_spinner);

		
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
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) 
		    {
		    }
		});
		frequencySpinner.setSelection(0);
		
		saveButton = (Button) v.findViewById(R.id.task_button);
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				task.setRepeatFrequency(getNumericFrequencyFromChoice(String
						.valueOf(frequencySpinner.getSelectedItem())));
				task.setRepeatDay(daySpinner.getSelectedItemPosition() + 1);

				TaskManagerApplication applicationContext = ((TaskManagerApplication) getFragmentActivity()
						.getApplication());
				applicationContext.addTask(task);
				getFragmentActivity().setResult(Activity.RESULT_OK);
				getFragmentActivity().finish();
			}
		});

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

	public FragmentActivity getFragmentActivity() {
		return this.getActivity();
	}
}