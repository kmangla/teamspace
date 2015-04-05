package com.teamspace.android.networking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.teamspace.android.caching.DatabaseCache;
import com.teamspace.android.models.Employee;
import com.teamspace.android.models.EmployeeState;
import com.teamspace.android.models.Reply;
import com.teamspace.android.models.Task;
import com.teamspace.android.models.TaskUpdate;
import com.teamspace.android.utils.TimeUtil;

public class SmsReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent intent) {

		if (intent.getAction()
				.equals("android.provider.Telephony.SMS_RECEIVED")) {

			Bundle bundle = intent.getExtras(); // ---get the SMS message passed
												// in---
			SmsMessage[] msgs = null;
			if (bundle != null) {
				// ---retrieve the SMS message received---
				try {
					DatabaseCache taskManagerData = DatabaseCache
							.getInstance(arg0);

					Object[] pdus = (Object[]) bundle.get("pdus");
					msgs = new SmsMessage[pdus.length];
					for (int i = 0; i < msgs.length; i++) {
						msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
						String msgFrom = msgs[i].getOriginatingAddress();
						String msgBody = msgs[i].getMessageBody();

						Employee employee = taskManagerData
								.getEmployeeByPhone(msgFrom);
						EmployeeState state = taskManagerData
								.getEmployeeState(employee.getId());
						Reply reply = new Reply();
						Log.i("task_test_receive", "SmsReceiver " + employee);
						Log.i("task_test_receive", "SmsReceiver " + state);

						if (state.isReplyReceived()) {
							continue;
						}

						Log.i("task_test_receive", "Not need reply received");
						state.setReplyReceived(true);

						long taskID = state.getLastTaskSentID();
						Task task = taskManagerData.getTaskByID(taskID);

						reply.setReplyText(msgBody);
						reply.setEmployeeID(employee.getId());
						reply.setTaskID(taskID);
						reply.setReceivedTime(TimeUtil.currentTimeSec());
						task.setLastSent(TimeUtil.currentTimeSec());
						long replyID = taskManagerData.addReply(reply);
						taskManagerData.updateEmployeeState(state,
								employee.getId());
						TaskUpdate.getInstance(arg0).updateTask(task);
						NotificationSender.sendNotification(arg0, employee, task);
					}
				} catch (Exception e) {
					Log.e("Exception caught", e.getMessage());
				}
			}
		}

	}

}
