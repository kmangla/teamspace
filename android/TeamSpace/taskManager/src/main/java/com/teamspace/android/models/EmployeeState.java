package com.teamspace.android.models;

import java.util.Date;

import com.teamspace.android.utils.TimeUtil;

public class EmployeeState implements Comparable<EmployeeState> {

	long TIME_WAIT_FOR_REPLY = 360L;
	
	long TIME_TO_HALT = 86400L * 3;
	
	private long lastTaskSentID;

	private boolean replyReceived;

	private long taskSentTime;

	private int remindersSent;

	private long taskSentReminderTime;

	private String employeeID;

	public String getEmployeeID() {
		return employeeID;
	}

	public void setEmployeeID(String employeeID) {
		this.employeeID = employeeID;
	}

	public long getTaskSentReminderTime() {
		return taskSentReminderTime;
	}

	public void setTaskSentReminderTime(long taskSentReminderTime) {
		this.taskSentReminderTime = taskSentReminderTime;
	}

	public int getRemindersSent() {
		return remindersSent;
	}

	public void setRemindersSent(int remindersSent) {
		this.remindersSent = remindersSent;
	}

	public long getLastTaskSentID() {
		return lastTaskSentID;
	}

	public void setLastTaskSentID(long lastTaskSentID) {
		this.lastTaskSentID = lastTaskSentID;
	}

	public boolean isReplyReceived() {
		return replyReceived;
	}

	public void setReplyReceived(boolean replyReceived) {
		this.replyReceived = replyReceived;
	}

	public long getTaskSentTime() {
		return taskSentTime;
	}

	public void setTaskSentTime(long taskSentTime) {
		this.taskSentTime = taskSentTime;
	}

	public boolean shouldSendReminder(long freq) {
		long seconds = TimeUtil.currentTimeSec();
		return ((seconds - this.getTaskSentTime()) > freq); 
	}

	public boolean resendTask() {
		long seconds = TimeUtil.currentTimeSec();
		Date nextSend = new Date((this.getTaskSentTime() + 86400L) * 1000L);
		Date now = new Date(seconds * 1000L);
		return TimeUtil.isSameOrLessDay(nextSend, now);
	}
	
	@Override
	public int compareTo(EmployeeState s) {
		if (this.isReplyReceived()) {
			if (s.isReplyReceived()) {
				return 0;
			} else {
				return 1;
			}
		}
		if (s.isReplyReceived()) {
			return -1;
		}
		if (this.getTaskSentReminderTime() <  s.getTaskSentReminderTime()) {
			return -1;
		} else {
			return 1;
		}
	}
	
}
