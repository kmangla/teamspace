package com.android.teamspace.models;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.teamspace.utils.ISO8601DateParser;
import com.android.teamspace.utils.Utils;

public class MigratedMessage {

	// Task that was replied to
	private String taskID;
	// Text of reply
	private String text;
	// Employee id for employee that send the reply
	private String employeeID;
	// Time in sec when the reply was received
	private long time;
	// Unique id for the reply
	private String messageID;
	
	private static String FOR_TASK = "forTask";
	private static String TASK_ID = "taskID";
	
	private static String TEXT = "description";
	private static String SENDER = "sentBy";
	private static String EMPLOYEE_ID = "employeeID";
	private static String TIME = "time";
	private static String MESSAGE_ID = "messageID";
	private static String ID = "id";
	private static String UPDATED = "updatedAt";
	
	public String getTaskID() {
		return taskID;
	}
	public void setTaskID(String taskID) {
		this.taskID = taskID;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getEmployeeID() {
		return employeeID;
	}
	public void setEmployeeID(String employeeID) {
		this.employeeID = employeeID;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public String getMessageID() {
		return messageID;
	}
	public void setMessageID(String messageID) {
		this.messageID = messageID;
	}
	
	
//	public JSONObject toJSONObject() throws JSONException { 
//		JSONObject obj  = new JSONObject();
//		obj.put(TASK_ID, this.getTaskID());
//		obj.put(MESSAGE_ID, this.getMessageID());
//		obj.put(TEXT, this.getText());
//		obj.put(TIME, this.getTime());
//		obj.put(EMPLOYEE_ID, this.getEmployeeID());
//		obj.put(TIME, ISO8601DateParser.toString(new Date(this.getTime())));
//
//		return obj;
//	}
	
	public HashMap<String, String> toMapObject() { 
		HashMap<String, String> obj  = new HashMap<String, String>();
		obj.put(TASK_ID, this.getTaskID());
		obj.put(TEXT, this.getText());
		obj.put(SENDER, this.getEmployeeID());
		return obj;
	}

	public static MigratedMessage parseJSON(JSONObject object) throws JSONException, ParseException {
		MigratedMessage message = new MigratedMessage();
		message.taskID = object.getString(FOR_TASK);
		message.messageID = object.getString(ID);
		message.text = object.getString(TEXT);
//		MigratedEmployee emp = MigratedEmployee.parseJSON(object.getJSONObject(SENDER));
//		message.setEmployeeID(emp.getEmployeeID());
		
		message.setEmployeeID(Utils.getSignedInUserId());
		
		try {
			message.time = ((Date)ISO8601DateParser.parse(object.getString(UPDATED))).getTime();
		} catch (JSONException e) {
			message.time = System.currentTimeMillis(); 
		}
		return message;
	}

}
