package com.android.teamspace.models;

import android.content.Context;

import com.android.teamspace.caching.DatabaseCache;
import com.android.teamspace.common.ui.TaskCompletionToast;
import com.android.teamspace.common.ui.TaskManagerApplication;

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
