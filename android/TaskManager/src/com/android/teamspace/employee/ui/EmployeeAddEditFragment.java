/**
 * Fragment for Creating/Updating information about an Employee
 */

package com.android.teamspace.employee.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.QuickContactBadge;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.teamspace.R;
import com.android.teamspace.caching.DataManager;
import com.android.teamspace.caching.DataManagerCallback;
import com.android.teamspace.caching.DatabaseCache;
import com.android.teamspace.common.ui.TaskManagerApplication;
import com.android.teamspace.models.MigratedEmployee;
import com.android.teamspace.models.QuickContactHelperTask;
import com.android.teamspace.models.QuickContactHelperTask.ContactBadge;
import com.android.teamspace.utils.Constants;
import com.android.teamspace.utils.Utils;

public class EmployeeAddEditFragment extends Fragment implements OnItemSelectedListener {

	private MigratedEmployee employee;
	EditText employeeNameEditText;
	EditText employeeNumberEditText;
	private int countryIndexInCountryArray = 0;
	Button saveButton;
	Spinner spinner;
	QuickContactBadge contact_pic;

	String employeeID = "";
	String employeeName = "";
	String employeePhone = "";
	boolean editMode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Since we need to fetch employee data from the database which might require some time
		// and must be done on background thread, we allow the caller to pass us few basic
		// information like employee name and number which we can instantly show in the UI
		// while more details about the employee are fetched from the database.
		Bundle extras = getActivity().getIntent().getExtras();
		if (extras != null) {
			employeeID = extras.getString(Constants.EMPLOYEE_ID);
			employeeName = extras.getString(Constants.EMPLOYEE_NAME);
			employeePhone = extras.getString(Constants.EMPLOYEE_PHONE);
			if (Utils.isStringNotEmpty(employeeID)) {
				editMode = true;
			}
		}
		
		final TaskManagerApplication applicationContext = ((TaskManagerApplication) getFragmentActivity()
				.getApplication());
		if (Utils.isStringNotEmpty(employeeID)) {
			AsyncTask<String, Void, MigratedEmployee> asyncTask = new AsyncTask<String, Void, MigratedEmployee>() {
				@Override
				protected MigratedEmployee doInBackground(String... employeeIDs) {
					Utils.log("Trying to fetch employee details from database cache for emp id: " + employeeIDs[0]);
					return DatabaseCache.getInstance(applicationContext).getMigratedEmployeeBlockingCall(employeeIDs[0]);
				}
				
				@Override
			    protected void onPostExecute(MigratedEmployee cachedEmployee) {
					employee = cachedEmployee;
					refreshUIForEmployee();
			    }
			};
			asyncTask.execute(employeeID);
		} else {
			employee = new MigratedEmployee();
		}
		
		getActivity().setTitle(R.string.add_employee);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.employee_add_fragment, container, false);			
		contact_pic = (QuickContactBadge) v.findViewById(R.id.contact_image);

		// Button for Importing Contacts Via Contact Picker
		Button buttonPickContact = (Button)v.findViewById(R.id.pickcontact);
		buttonPickContact.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
				startActivityForResult(intent, 1);             
			}});
		
		employeeNameEditText = (EditText) v.findViewById(R.id.employee_name);
		employeeNumberEditText = (EditText) v.findViewById(R.id.employee_number);
		
		if (Utils.isStringNotEmpty(employeeName)) {
			employeeNameEditText.setText(employeeName);
		}
		if (Utils.isStringNotEmpty(employeePhone)) {
			employeeNumberEditText.setText(employeePhone);
		}		
		
		// If we are editing an existing employee, we don't need import
		// functionality
		if (editMode) {
			buttonPickContact.setVisibility(View.GONE);
			InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(
				      Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(employeeNameEditText, 0);
		} else {
			buttonPickContact.setVisibility(View.VISIBLE);
		}
		
		// Add country spinner for phone number field
		spinner = (Spinner) v.findViewById(R.id.country_spinner);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> countryAdapter = ArrayAdapter
				.createFromResource(getActivity(), R.array.country_array,
						R.layout.country_spinner_item);
		countryAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(countryAdapter);
		spinner.setOnItemSelectedListener(this);

		saveButton = (Button) v.findViewById(R.id.employee_button);
		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(final View v) {
				TaskManagerApplication applicationContext = ((TaskManagerApplication) getFragmentActivity()
						.getApplication());
				
				// Make sure we have the country code before saving the number
				String phoneNumberFromEditBox = employeeNumberEditText
						.getText().toString();
				
				if (!phoneNumberFromEditBox.startsWith("+")
						&& spinner.getVisibility() == View.GONE) {
					// Notify user about the error
					Toast.makeText(
							v.getContext(),
							v.getContext()
									.getResources()
									.getString(
											R.string.error_missing_country_code),
							Toast.LENGTH_SHORT).show();
					return;
				}

				// Make sure the country code is same as the country selected
				// from spinner
				String phoneNumberWithCountryCode = phoneNumberFromEditBox;
				if (spinner.getVisibility() == View.VISIBLE) {
					String countryCodeSelected = Utils.getCountryCodeForCountry(
							v.getContext(), countryIndexInCountryArray);
					
					if (!phoneNumberWithCountryCode.startsWith(countryCodeSelected)) {
						phoneNumberWithCountryCode = countryCodeSelected
								+ Utils.removeCountryPrefixFromPhoneNumber(
										v.getContext(), phoneNumberFromEditBox);
					}
				}	
				
				employee.setPhoneWithContryCode(phoneNumberWithCountryCode);
				Utils.log("Setting phone number " + phoneNumberWithCountryCode);
				
				employee.setName(employeeNameEditText.getText().toString());
				employee.setLastUpdated("0 days ago");				
				// TODO: Get logged in userID and companyID to set here.
				employee.setUserID("user_1");
				employee.setCompanyID("1"); 				
				
				DataManager dataMgr = DataManager.getInstance(applicationContext);
				Intent newIntent = new Intent();
				
				if (Utils.isStringNotEmpty(employeeID)) {
					employee.setEmployeeID(employeeID);
					dataMgr.updateEmployee(employee, new DataManagerCallback() {

						@Override
						public void onSuccess(String response) {							
						}

						@Override
						public void onFailure(String response) {
							// Notify user about the error
							Toast.makeText(
									v.getContext(),
									v.getContext().getResources()
											.getString(
													R.string.error_employee_update_failed),
									Toast.LENGTH_SHORT).show();
						}
					});					
				} else {
					employee.setEmployeeID(Constants.EMPTY_STRING);
					employee.setDesignation(Constants.EMPLOYEE);
					employee.setTaskCount("0");
					
					newIntent.putExtra(Constants.EMPLOYEE_NAME, employee.getName());
					newIntent.putExtra(Constants.EMPLOYEE_PHONE, employee.getPhoneWithCountryCode());
					
					dataMgr.createEmployee(employee, null);
				}
				
				getFragmentActivity().setResult(Activity.RESULT_OK, newIntent);
				getFragmentActivity().finish();
			}
		});

		return v;
	}
	
	private void refreshUIForEmployee() {
		employeeNameEditText.setText(employee.getName());
		employeeNumberEditText.setText(employee.getPhoneWithCountryCode());
		
		QuickContactHelperTask task = new QuickContactHelperTask();
		ContactBadge badge = new ContactBadge(getActivity(), contact_pic, employee.getEmployeeID(), null);
		task.execute(badge);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// If selected via Contact Picker get Name and Number and autofill.
		if(resultCode == getActivity().RESULT_OK){
			Uri contactData = data.getData();
			Cursor cursor =  getActivity().getContentResolver().query(contactData, null, null, null, null);
			cursor.moveToFirst();

			String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			String number =  cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));

			Utils.log("Cursor returned: " + name + " : " + number);

			employeeNameEditText.setText(name);
			employeeNumberEditText.setText(number);
			
			// If the country code is already pre-fixed, hide the country
			// spinner
			if (Utils.isStringNotEmpty(number)) {
				if (number.startsWith("+")) {
					spinner.setVisibility(View.GONE);
				}

				MigratedEmployee dummyEmp = new MigratedEmployee();
				dummyEmp.setName(name);
				dummyEmp.setPhoneWithContryCode(number);
				
				QuickContactHelperTask task = new QuickContactHelperTask();
				ContactBadge badge = new ContactBadge(getActivity(),
						contact_pic, dummyEmp, null);
				task.execute(badge);
			}
		}
	}
	
	public FragmentActivity getFragmentActivity() {
		return this.getActivity();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		countryIndexInCountryArray = position;
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}
}
