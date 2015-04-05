package com.android.teamspace.models;

public abstract class TaskMessage implements Comparable<TaskMessage> {

	abstract public String getText();

	abstract public String getEmployeeID();

	abstract public long getTaskID();

	abstract public long getTime();

	public int compareTo(TaskMessage message) {
		if (this.getTime() >= message.getTime()) {
			return 1;
		} else {
			return -1;
		}
	}

}
