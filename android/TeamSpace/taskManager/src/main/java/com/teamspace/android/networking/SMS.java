package com.teamspace.android.networking;

import java.util.ArrayList;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;

import com.teamspace.android.common.ui.TaskManagerApplication;
import com.teamspace.android.models.Employee;
import com.teamspace.android.models.EmployeeState;
import com.teamspace.android.models.Task;
import com.teamspace.android.utils.Constants;

public class SMS {

	static SMS instance;

	private Context context;

	static String NEW_TASK = "NEW_TASK";
	static String REMINDER = "REMINDER";
	static String STRONG_REMINDER = "STRONG_REMINDER";

	public SMS(Context context) {
		this.context = context;
	}

	public static SMS getInstance(Context context) {
		if (instance == null) {
			instance = new SMS(context);
			registerReceivers(context);
		}
		return instance;
	}

	public void sendTaskSMS(Context context, Employee employee, Task task,
			String message, boolean isRepeated, String intentName) {
		Intent i = new Intent(intentName);
		i.putExtra(Constants.EMPLOYEE_ID, employee.getId());
		i.putExtra("TASK_ID", task.getId());
		i.putExtra("IS_REPEATED", isRepeated);
		PendingIntent sentPI = PendingIntent.getBroadcast(context,
				employee.getId().hashCode(), i, PendingIntent.FLAG_UPDATE_CURRENT);
		SmsManager sms = SmsManager.getDefault();
		ArrayList<String> parts = sms.divideMessage(message);
		ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
		for (int number = 0; number < parts.size(); number++) {
			sentIntents.add(sentPI);
		}
		Log.i("sms", "Sent " + employee.getId() + " " + task.getId());
		//if (employee.getNumber().compareTo("+918510006309") == 0) {
		sms.sendMultipartTextMessage(employee.getNumber(), null, parts,
				sentIntents, null);
		//}
	}

	public static void registerReceivers(Context context) {
		context.registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				String employeeID = "";
				long taskID = -1;
				Bundle extras = intent.getExtras();
				if (extras != null) {
					employeeID = extras.getString(Constants.EMPLOYEE_ID);
					taskID = extras.getLong("TASK_ID");
				}

				switch (getResultCode()) {
				case Activity.RESULT_OK:
					long seconds = (System.currentTimeMillis() / 1000L);
					TaskManagerApplication application = (TaskManagerApplication) context
							.getApplicationContext();
					Task task = application.getTaskByID(taskID);
					Employee employee = application.getEmployee(employeeID);
					EmployeeState state = application
							.getEmployeeState(employeeID);
					task.setLastSent(seconds);
					state.setLastTaskSentID((int) task.getId());
					state.setReplyReceived(true);
					state.setTaskSentTime(seconds);
					state.setRemindersSent(0);
					state.setTaskSentReminderTime(seconds);
					application.updateEmployeeState(state, employee.getId());
					application.updateTask(task);
					break;
				}
			}
		}, new IntentFilter(NEW_TASK));
		context.registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				String employeeID = "";
				long taskID = -1;

				Bundle extras = intent.getExtras();
				if (extras != null) {
					employeeID = extras.getString(Constants.EMPLOYEE_ID);
					taskID = extras.getLong("TASK_ID");
				}
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					TaskManagerApplication application = (TaskManagerApplication) context
							.getApplicationContext();
					Task task = application.getTaskByID(taskID);
					Employee employee = application.getEmployee(employeeID);
					EmployeeState state = application
							.getEmployeeState(employeeID);
					long seconds = (System.currentTimeMillis() / 1000L);
					task.setLastSent(seconds);
					state.setLastTaskSentID(task.getId());
					state.setReplyReceived(false);
					state.setTaskSentTime(seconds);
					state.setRemindersSent(0);
					application.updateEmployeeState(state, employee.getId());
					application.updateTask(task);

					break;
				}
			}
		}, new IntentFilter(STRONG_REMINDER));
		context.registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				String employeeID = "";
				long taskID = -1;
				boolean repeated = false;
				Bundle extras = intent.getExtras();
				if (extras != null) {
					employeeID = extras.getString(Constants.EMPLOYEE_ID);
					taskID = extras.getLong("TASK_ID");
					repeated = extras.getBoolean("IS_REPEATED");
				}

				Log.i("sms", "Got here " + employeeID + " " + taskID + " "
						+ getResultCode());
				switch (getResultCode()) {
				case Activity.RESULT_OK: 
					TaskManagerApplication application = (TaskManagerApplication) context
							.getApplicationContext();
					Task task = application.getTaskByID(taskID);
					Employee employee = application.getEmployee(employeeID);
					EmployeeState state = application
							.getEmployeeState(employeeID);
					long seconds = (System.currentTimeMillis() / 1000L);
					task.setLastSent(seconds);
					state.setLastTaskSentID((int) task.getId());
					state.setReplyReceived(false);
					state.setTaskSentTime(seconds);
					if (repeated) {
						state.setRemindersSent(state.getRemindersSent() + 1);
					} else {
						state.setTaskSentReminderTime(seconds);
						state.setRemindersSent(0);
					}
					application.updateEmployeeState(state, employee.getId());
					application.updateTask(task);
					Log.i("sms", "Got here " + task + " " + state);

					break;
				}
			}
		}, new IntentFilter(REMINDER));

	}

}
