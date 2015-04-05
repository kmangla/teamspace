/**
 * Service to check if a task is a repeated task.
 * Repeated Tasks need to be restarted on specific dates. This code checks all repeated
 * tasks and selected those that need to be restarted and restarts them.
 */
package com.teamspace.android.unused;

import java.util.ArrayList;
import java.util.Iterator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.teamspace.android.caching.DatabaseCache;
import com.teamspace.android.models.Task;
import com.teamspace.android.models.TaskUpdate;

public class RepeatedTaskChecker extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		ArrayList<Task> tasks = DatabaseCache.getInstance(arg0)
				.getAllRepeatedTasks();
		Log.i("test", "here");
		Iterator<Task> it = tasks.iterator();
		while (it.hasNext()) {
			Task task = (Task) it.next();
			Log.i("test", task + " " + task.shouldBeRestarted());
			if (task.shouldBeRestarted()) {
				task.setCompletedCurrent(false);
				TaskUpdate.getInstance(arg0).updateTask(task);
			}
		}
	}

}
