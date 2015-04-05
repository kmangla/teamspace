package com.teamspace.android.models;

import android.content.Context;

import com.teamspace.android.caching.DatabaseCache;
import com.teamspace.android.unused.TaskCompletionToast;
import com.teamspace.android.common.ui.TaskManagerApplication;

public class TaskUpdate {

	private static TaskUpdate instance;
	private TaskManagerApplication mContext;

	private TaskUpdate(Context context) {
		this.mContext = (TaskManagerApplication) context.getApplicationContext();
	}

	public static TaskUpdate getInstance(Context context) {
		if (null == instance) {
			instance = new TaskUpdate(context);
		}
		return instance;
	}

	public void updateTask(Task task) {
		Task original = mContext.getTaskByID(task.getId());
		TaskCompletionToast.run(mContext, original, task);
		DatabaseCache.getInstance(mContext).updateTask(task);
	}
	
}
