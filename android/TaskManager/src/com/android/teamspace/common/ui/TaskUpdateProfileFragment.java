package com.android.teamspace.common.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.teamspace.R;
import com.android.teamspace.models.Employee;
import com.android.teamspace.models.Task;
import com.android.teamspace.models.TaskUpdate;

public class TaskUpdateProfileFragment extends Fragment {
	private Task task;
	private EditText descriptionField;
	private TextView employeeName;
	private Employee employee;
	private Button saveButton;
	private Spinner frequencySpinner;
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
		View v = inflater.inflate(R.layout.fragment_task, container, false);

		descriptionField = (EditText) v.findViewById(R.id.task_description);
		employeeName = (TextView) v.findViewById(R.id.task_employee);

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
		frequencySpinner = (Spinner) v.findViewById(R.id.task_spinner);
		frequencySpinner.setSelection(getSpinnerPosition(task.getFrequency()));

		saveButton = (Button) v.findViewById(R.id.task_button);
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				task.setFrequency(getNumericFrequencyFromChoice(String
						.valueOf(frequencySpinner.getSelectedItem())));
				TaskUpdate.getInstance(v.getContext()).updateTask(task);
				getFragmentActivity().setResult(Activity.RESULT_OK);
				getFragmentActivity().finish();
			}
		});

		isCompleted = (CheckBox) v.findViewById(R.id.task_is_completed);
		isCompleted.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				task.setCompleted(isCompleted.isChecked());
			}
		});
		if (task.isCompleted()) {
			isCompleted.setChecked(true);
		} else {
			isCompleted.setChecked(false);
		}

		return v;
	}

	private long getNumericFrequencyFromChoice(String choice) {
		switch (choice) {
		case "Daily":
			return 86400L;
		case "Weekly":
			return 86400L * 7;
		case "Monthly":
			return 86400L * 31;
		default:
			return 86400L * 7;
		}
	}

	private int getSpinnerPosition(long frequency) {
		if (frequency == 86400L) {
			return 0;
		} else if (frequency == 86400L * 7) {
			return 1;
		} else if (frequency == 86400L * 31) {
			return 2;
		}
		return 0;
	}

	public FragmentActivity getFragmentActivity() {
		return this.getActivity();
	}

}
