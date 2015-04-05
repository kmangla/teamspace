package com.teamspace.android.unused;

import android.content.Context;

import com.teamspace.android.caching.DatabaseCache;
import com.teamspace.android.common.ui.TaskManagerApplication;
import com.teamspace.android.models.EmployeeState;

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
