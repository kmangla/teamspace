package com.android.teamspace.unused;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.teamspace.caching.DatabaseCache;
import com.android.teamspace.common.ui.TaskManagerApplication;
import com.android.teamspace.models.Employee;
import com.android.teamspace.models.EmployeeState;
import com.android.teamspace.models.Task;
import com.android.teamspace.networking.ReminderSMSService;
import com.android.teamspace.utils.TimeUtil;

public class TaskUpdater extends BroadcastReceiver {

	long TIME_BETWEEN_RUNS = 600L;

	@Override
	public void onReceive(Context arg0, Intent arg1) {

		/*
		TaskManagerApplication applicationContext = ((TaskManagerApplication) arg0
				.getApplicationContext());
		long seconds = (System.currentTimeMillis() / 1000L);
		if ((seconds - applicationContext.getLastRunTime()) < TIME_BETWEEN_RUNS) {
			return;
		}
		applicationContext.setLastRunTime(seconds);

		ArrayList<Employee> employees = applicationContext.getEmployees();


		Iterator it = employees.iterator();
		while (it.hasNext()) {

			Employee employee = (Employee) it.next();
			if (!employeeIsContactable(employee, arg0)) {
				continue;
			}
			String employeeID = employee.getId();
			EmployeeState state = applicationContext
					.getEmployeeState(employeeID);

			Log.i("task_test", "Considering Update for " + employeeID);
			Log.i("task_test", "" + state);
		
			if (!state.isReplyReceived() && state.resendTask()) {
				long taskID = state.getLastTaskSentID();
				Task task = applicationContext.getTaskByID(taskID);
				ReminderSMSService.sendStrongSMSReminder(employee, state, task, applicationContext);
				continue;
			} else if (!state.isReplyReceived() && state.shouldSendReminder(pref.getReminderFrequency())) {
				long taskID = state.getLastTaskSentID();
				Task task = applicationContext.getTaskByID(taskID);
				ReminderSMSService.sendSMSReminder(employee, state, task, applicationContext,
						true);
				continue;
			} else if (!state.isReplyReceived()) {
				continue;
			}

			ArrayList<Task> taskList = applicationContext.getTasks(employeeID);
			Iterator itTasks = taskList.iterator();

			while (itTasks.hasNext()) {
				Task task = (Task) itTasks.next();
				if (task.needsToBeSent()
						&& !applicationContext.isTaskPaused(task)) {
					ReminderSMSService.sendSMSReminder(employee, state, task, applicationContext,
							false); 
					break;
				} 
			}
		}*/
	}

	/*
	private boolean employeeIsContactable(Employee employee, Context context) {
		int currentHour = TimeUtil.currentHour();
		DatabaseCache data = DatabaseCache.getInstance(context);
		OwnerPreference pref = data.getOwnerPreference();
		return (currentHour <= pref.getEndTime() && currentHour >= pref.getStartTime());
	}*/
}
