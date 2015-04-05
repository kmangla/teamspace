/**
 * Service to send Reminder SMSs.
 */
package com.teamspace.android.networking;

import java.util.ArrayList;
import java.util.Random;

import android.util.Log;

import com.teamspace.android.common.ui.TaskManagerApplication;
import com.teamspace.android.models.Employee;
import com.teamspace.android.models.EmployeeState;
import com.teamspace.android.models.Task;
import com.teamspace.android.utils.TimeUtil;

public class ReminderSMSService {

	/**
	 * Send strong reminder for unupdated tasks.
	 * @param employee
	 * @param state
	 * @param task
	 * @param context
	 */
	public static void sendStrongSMSReminder(Employee employee, EmployeeState state,
			Task task, TaskManagerApplication context) {
		String message = createStrongReminder(employee, state, task, context);
		Log.i("test", message);
		SMS.getInstance(context).sendTaskSMS(context, employee, task,
				message, true, SMS.STRONG_REMINDER);
	}
	
	private static String createStrongReminder(Employee employee, EmployeeState state,
			Task task, TaskManagerApplication context) {
		long waitingSince = state.getTaskSentReminderTime();
		long currentTime = TimeUtil.currentTimeSec();
		long diff = currentTime - waitingSince;
		int days = (int)(diff/86400);
		if (days > 0) {
			return "No update has been received for last " + days + " days. Update your tasks today.";
		}
		return createSMSMessage(task);
	}

	/**
	 * Send standard reminder for a task to an employee via SMS
	 * @param employee
	 * @param state
	 * @param task
	 * @param context
	 * @param isRepeated
	 * @param update
	 */
	public static void sendSMSReminder(Employee employee, EmployeeState state,
			Task task, TaskManagerApplication context, Boolean isRepeated) {
		String message = "";
		message = createSMSMessage(task);
		SMS.getInstance(context).sendTaskSMS(context, employee, task,
				message, isRepeated, SMS.REMINDER);
	}
	
	private static String createMessage() {
		ArrayList<String> messages = new ArrayList<String>();
		messages.add("Reply via SMS");
		messages.add("Update status");
		messages.add("SMS the current status");
		messages.add("Send status in SMS NOW");
		messages.add("What is the progress");
		messages.add("Send reply SMS NOW with latest work done");
		messages.add("Send a SMS reply today");
		messages.add("SMS \"no update\" if no progress");
		messages.add("Reply today via SMS");
		Random r = new Random();
		int i1 = r.nextInt(9);
		return messages.get(i1);
	}
	
	private static String createSMSMessage(Task task) {
		String precursor = createMessage();
		String message = precursor + " for task \n " + task.getDescription();
		return message;
	}

}
