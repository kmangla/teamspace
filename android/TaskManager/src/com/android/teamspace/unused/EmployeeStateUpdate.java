package com.android.teamspace.unused;

import android.content.Context;

import com.android.teamspace.caching.DatabaseCache;
import com.android.teamspace.common.ui.TaskManagerApplication;
import com.android.teamspace.models.EmployeeState;

public class EmployeeStateUpdate {

	private static EmployeeStateUpdate instance;
	private TaskManagerApplication mContext;

	private EmployeeStateUpdate(Context context) {
		this.mContext = (TaskManagerApplication) context.getApplicationContext();
	}

	public static EmployeeStateUpdate getInstance(Context context) {
		if (null == instance) {
			instance = new EmployeeStateUpdate(context);
		}
		return instance;
	}

	public void updateEmployeeState(EmployeeState state, String employeeID) {
		DatabaseCache.getInstance(mContext).updateEmployeeState(state,
				employeeID);
	}

}
