/**
 * Data for a single reply by an employee
 */
package com.teamspace.android.models;

public class Reply extends TaskMessage {

	// Task that was replied to
	private long taskID;
	// Text of reply
	private String replyText;
	// Employee id for employee that send the reply
	private String employeeID;
	// Time in sec when the reply was received
	private long receivedTime;
	// Unique id for the reply
	private long id;

	public String toString() {
		return this.getReplyText();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getTaskID() {
		return taskID;
	}

	public void setTaskID(long taskID) {
		this.taskID = taskID;
	}

	public String getReplyText() {
		return replyText;
	}

	public void setReplyText(String replyText) {
		this.replyText = replyText;
	}

	public String getEmployeeID() {
		return employeeID;
	}

	public void setEmployeeID(String employeeID) {
		this.employeeID = employeeID;
	}

	public long getReceivedTime() {
		return receivedTime;
	}

	public void setReceivedTime(long receivedTime) {
		this.receivedTime = receivedTime;
	}

	@Override
	public String getText() {
		return getReplyText();
	}

	@Override
	public long getTime() {
		return getReceivedTime();
	}
}
