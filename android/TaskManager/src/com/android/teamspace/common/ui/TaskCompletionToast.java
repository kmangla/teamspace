package com.android.teamspace.common.ui;

import android.widget.Toast;

import com.android.teamspace.models.EmployeeState;
import com.android.teamspace.models.Task;

public class TaskCompletionToast {

	public static void run(TaskManagerApplication context, Task oldTask,
			Task newTask) {
		if (oldTask.isCompleted() || !newTask.isCompleted()) {
			return;
		}
		EmployeeState state = context.getEmployeeState(oldTask.getEmployeeID());
		if ((state.getLastTaskSentID() == oldTask.getId())
				&& !state.isReplyReceived()) {
			Toast.makeText(
					context.getBaseContext(),
					"This task is closed. However, the employee will be required to submit a final update to end the task",
					Toast.LENGTH_LONG).show();
		}
	}
}
