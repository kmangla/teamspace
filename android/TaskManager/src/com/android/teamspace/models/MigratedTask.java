package com.android.teamspace.models;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.teamspace.caching.DatabaseCache;
import com.android.teamspace.common.ui.TaskManagerApplication;
import com.android.teamspace.utils.Constants;
import com.android.teamspace.utils.ISO8601DateParser;
import com.android.teamspace.utils.Utils;

public class MigratedTask {

	private String taskID;
	private String status;
	private String description;
	private String title;
	private String userID;
	private String employeeID;
	private String employeeName;
	private String employeeNumber;
	private String assignedBy;
	private String companyID;
	private long frequency;
	private long createdOn;
	private long lastReminder;
	private long lastUpdate;
	private long lastSeen;
	private long updateCount;
	
	private static String TASK_ID = "taskID";
	private static String STATUS = "status";
	private static String DESCRIPTION = "description";
	private static String TITLE = "title";
	private static String USER_ID = "userID";
	private static String EMPLOYEE_ID = "employeeID";
	private static String EMPLOYEE_NAME = "employeeName";
	private static String EMPLOYEE_NUMBER = "employeeNumber";
	private static String COMPANY_ID = "companyID";
	private static String FREQUENCY = "frequency";
	private static String CREATED_ON = "createdOn";
	private static String LAST_REMINDER = "lastReminder";
	private static String LAST_UPDATE = "lastUpdate";
	private static String LAST_SEEN = "lastSeen";
	private static String UPDATE_COUNT = "updateCount";
	private static String ASSIGNED_BY = "assignedBy";
	private static String ID = "id";
	private static String ASSIGNED_TO = "assignedTo";
	

	public long getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(long createdOn) {
		this.createdOn = createdOn;
	}
	public long getLastReminder() {
		return lastReminder;
	}
	public void setLastReminder(long lastReminder) {
		this.lastReminder = lastReminder;
	}
	public long getLastUpdate() {
		return lastUpdate;
	}
	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	public long getLastSeen() {
		return lastSeen;
	}
	public long getUpdateCount() {
		return updateCount;
	}
	public void setLastSeen(long lastSeen) {
		this.lastSeen = lastSeen;
	}
	public String getTaskID() {
		return taskID;
	}
	public void setTaskID(String taskID) {
		this.taskID = taskID;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setUpdateCount(long count) {
		this.updateCount = count;
	}
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	public String getEmployeeID() {
		return employeeID;
	}
	public String getEmployeeName() {
		return employeeName;
	}
	public String getEmployeeNumber() {
		return employeeNumber;
	}
	public void setEmployeeID(String employeeID) {
		this.employeeID = employeeID;
		MigratedEmployee emp = DatabaseCache.getInstance(
				TaskManagerApplication.getAppContext())
				.getMigratedEmployeeBlockingCall(employeeID);		
		if (emp != null) {
			this.employeeName = emp.getName();
			this.employeeNumber = emp.getPhoneWithCountryCode();
		}
	}
	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}
	public void setEmployeeNumber(String employeeNumber) {
		this.employeeNumber = employeeNumber;
	}
	public String getCompanyID() {
		return companyID;
	}
	public void setCompanyID(String companyID) {
		this.companyID = companyID;
	}
	public long getFrequency() {
		return frequency;
	}
	public void setFrequency(long frequency) {
		this.frequency = frequency;
	} 
	
	@Override
	public String toString() {
		return this.taskID;
	}
	
	public JSONObject toJSONObject() throws JSONException { 
		JSONObject obj  = new JSONObject();
		obj.put(TASK_ID, this.getTaskID());
		obj.put(USER_ID, this.getUserID());
		obj.put(COMPANY_ID, this.getCompanyID());
		obj.put(EMPLOYEE_ID, this.getEmployeeID());
		obj.put(EMPLOYEE_NAME, this.getEmployeeName());
		obj.put(EMPLOYEE_NUMBER, this.getEmployeeNumber());
		obj.put(DESCRIPTION, this.getDescription());
		obj.put(TITLE, this.getTitle());
		obj.put(STATUS, this.getStatus());
		obj.put(FREQUENCY, this.getFrequency());
		obj.put(CREATED_ON, ISO8601DateParser.toString(new Date(this.getCreatedOn())));
		obj.put(LAST_REMINDER, ISO8601DateParser.toString(new Date(this.getLastReminder())));
		obj.put(LAST_UPDATE, ISO8601DateParser.toString(new Date(this.getLastUpdate())));
		obj.put(LAST_SEEN, ISO8601DateParser.toString(new Date(this.getLastSeen())));
		obj.put(UPDATE_COUNT, this.getUpdateCount() + "");

		return obj;
	}
	
	// These are the params required in POST requests to the server for creating or
	// updating a particular task.
	public HashMap<String, String> toMapObject() { 
		HashMap<String, String> obj  = new HashMap<String, String>();
		if (this.getTaskID() != null) {
			obj.put(TASK_ID, this.getTaskID());
		}
		obj.put(EMPLOYEE_ID, this.getEmployeeID());
		obj.put(DESCRIPTION, this.getDescription());
		obj.put(TITLE, this.getTitle());
		obj.put(STATUS, this.getStatus());
		obj.put(FREQUENCY, this.getFrequency() + "");
		obj.put(Constants.KEY, Utils.getSignedInUserPhoneNumber());
		return obj;
	}
	
	public static MigratedTask parseJSON(JSONObject object) throws JSONException, ParseException {
		MigratedTask task = new MigratedTask();
		task.taskID = object.getString(ID);
		task.userID = object.getString(ASSIGNED_BY);
		task.companyID = object.getString(ASSIGNED_BY);		
		task.description = object.getString(DESCRIPTION);
		task.title = object.getString(TITLE);
		task.status = object.getString(STATUS);
		task.frequency = object.getLong(FREQUENCY);
		
		try {
			task.updateCount = object.getLong(UPDATE_COUNT);
		} catch (JSONException e) {
			task.updateCount = 0; 
		}
		try {
			task.assignedBy = object.getString(ASSIGNED_BY);
		} catch (JSONException e) {
			 
		}
		try {
			MigratedEmployee emp = MigratedEmployee.parseJSON(object.getJSONObject(ASSIGNED_TO));
			task.setEmployeeID(emp.getEmployeeID());
			task.setEmployeeName(emp.getName());
			task.setEmployeeNumber(emp.getPhoneWithCountryCode());
		} catch (JSONException e) {
			
		}
		try {
			task.createdOn = ((Date)ISO8601DateParser.parse(object.getString(CREATED_ON))) .getTime();
		} catch (JSONException e) {
			task.createdOn = 0; 
		}
		try { 
			task.lastReminder = ((Date)ISO8601DateParser.parse(object.getString(LAST_REMINDER))) .getTime();
		} catch (JSONException e) { 
			task.lastReminder = 0; 
		}
		try {
			task.lastUpdate = ((Date)ISO8601DateParser.parse(object.getString(LAST_UPDATE))) .getTime();
		} catch (JSONException e) {
			task.lastUpdate = 0;
		}
		try {
			task.lastSeen = ((Date)ISO8601DateParser.parse(object.getString(LAST_SEEN))) .getTime();
		} catch (JSONException e) {
			task.lastSeen = 0; 
		}

		return task;
	}
	public void setAssignedTo1(MigratedEmployee assignedTo) {
	}
	public String getAssignedBy() {
		return assignedBy;
	}
	public void setAssignedBy(String assignedBy) {
		this.assignedBy = assignedBy;
	}
}
