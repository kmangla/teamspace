package com.android.teamspace.models;

import java.util.Calendar;
import java.util.Date;

import com.android.teamspace.utils.TimeUtil;

public class Task implements Comparable<Task>{

	@Override
	public int compareTo(Task t) {
		if (this.getLastSent() > t.getLastSent()) {
			return 1;
		} else {
			return -1;
		}
	}

	private String description;
	private long id;
	private boolean completed;
	private String employeeID;
	private long lastSent;
	private long frequency;
	private boolean isRepeated;

	private long lastSeenTime;

	public static int MIN_LAST_SENT = 0;

	private long repeatFrequency;
	private boolean completedCurrent;
	private long repeatDay;
	
	public long getRepeatDay() {
		return repeatDay;
	}
	public void setRepeatDay(long repeatDay) {
		this.repeatDay = repeatDay;
	}
	public long getRepeatFrequency() {
		return repeatFrequency;
	}	
	public void setRepeatFrequency(long repeatFrequency) {
		this.repeatFrequency = repeatFrequency;
	}
	public boolean isCompletedCurrent() {
		return completedCurrent;
	}
	public void setCompletedCurrent(boolean completedCurrent) {
		this.completedCurrent = completedCurrent;
	} 

	
	public boolean isRepeated() {
		return isRepeated;
	}

	public void setRepeated(boolean isRepeated) {
		this.isRepeated = isRepeated;
	}

	public long getLastSeenTime() {
		return lastSeenTime;
	}

	public void setLastSeenTime(long lastSeenTime) {
		this.lastSeenTime = lastSeenTime;
	}

	public long getFrequency() {
		return frequency;
	}

	public void setFrequency(long frequency) {
		this.frequency = frequency;
	}

	public long getLastSent() {
		return lastSent;
	}

	public void setLastSent(long lastSent) {
		this.lastSent = lastSent;
	}

	@Override
	public String toString() {
		return this.getDescription() + " " + this.getRepeatDay();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public String getEmployeeID() {
		return employeeID;
	}

	public void setEmployeeID(String employeeID) {
		this.employeeID = employeeID;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public boolean shouldBeRestarted() {
		if (!this.isCompletedCurrent()) {
			return false;
		}
		long seconds = this.getLastSent();
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(seconds * 1000L);
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
		int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
		long currentTimeSec = TimeUtil.currentTimeSec();
		Calendar current = Calendar.getInstance();
		int currentDayOfWeek = current.get(Calendar.DAY_OF_WEEK);
		int currentDayOfMonth = current.get(Calendar.DAY_OF_MONTH);
		if (this.getRepeatFrequency() == 86400L * 7) {
			if ((currentTimeSec - seconds) > 86400L*7) {
				return true;
			}
			if (currentDayOfWeek >= dayOfWeek) {
				return (currentDayOfWeek >= this.getRepeatDay()) &&
						(dayOfWeek < this.getRepeatDay());
			} else {
				return (currentDayOfWeek >= this.getRepeatDay()) ||
						(dayOfWeek < this.getRepeatDay());
			}
		} else {
			if ((currentTimeSec - seconds) > 86400L*31) {
				return true;
			}
			if (currentDayOfMonth >= dayOfMonth) {
				return (currentDayOfMonth >= this.getRepeatDay()) &&
						(dayOfMonth < this.getRepeatDay());
			} else {
				return (currentDayOfMonth >= this.getRepeatDay()) ||
						(dayOfMonth < this.getRepeatDay());
			}
		}
		
	}

	
	public boolean needsToBeSent() {
		long seconds = (System.currentTimeMillis() / 1000L);
		Date nextSend;
		boolean completed = false;
		if (this.isRepeated()) {
			completed = this.isCompletedCurrent() || this.isCompleted();
			nextSend = new Date(
					(this.getLastSent() + 86400L) * 1000L);
		} else {
			nextSend = new Date(
					(this.getLastSent() + this.getFrequency()) * 1000L);
			completed = this.isCompleted();
		}
		Date now = new Date(seconds * 1000L);
		return !completed && TimeUtil.isSameOrLessDay(nextSend, now);
	}
}
