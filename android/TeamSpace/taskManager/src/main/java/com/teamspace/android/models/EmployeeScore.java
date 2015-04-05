package com.teamspace.android.models;


public class EmployeeScore implements Comparable<EmployeeScore> {

	private String employeeID;
	private long numUpdates;
	private long numClosed;
	private long numOpenTasks;

	public String getEmployeeID() {
		return employeeID;
	}

	public void setEmployeeID(String employeeID) {
		this.employeeID = employeeID;
	}

	public long getNumUpdates() {
		return numUpdates;
	}

	public void setNumUpdates(long numUpdates) {
		this.numUpdates = numUpdates;
	}

	public long getNumClosed() {
		return numClosed;
	}

	public void setNumClosed(long numClosed) {
		this.numClosed = numClosed;
	}

	public long getNumOpenTasks() {
		return numOpenTasks;
	}

	public void setNumOpenTasks(long numOpenTasks) {
		this.numOpenTasks = numOpenTasks;
	}

	public int compareTo(EmployeeScore score) {
		if (this.getNumUpdates() > score.getNumUpdates()) {
			return 1;
		} else if (this.getNumUpdates() < score.getNumUpdates()) {
			return -1;
		}

		if (this.getNumClosed() > score.getNumClosed()) {
			return 1;
		} else if (this.getNumClosed() < score.getNumClosed()) {
			return -1;
		}

		if (this.getNumOpenTasks() > score.getNumOpenTasks()) {
			return 1;
		} else if (this.getNumOpenTasks() < score.getNumOpenTasks()) {
			return -1;
		}
		return 0;
	}
}
