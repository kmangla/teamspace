package com.teamspace.android.utils;

import java.util.Calendar;
import java.util.Date;

public class TimeUtil {

	private static final long MILLIS_PER_DAY = 86400000L;

	public static boolean isSameOrLessDay(Date date1, Date date2) {

		// Strip out the time part of each date.
		long julianDayNumber1 = date1.getTime() / MILLIS_PER_DAY;
		long julianDayNumber2 = date2.getTime() / MILLIS_PER_DAY;

		// If they now are equal then it is the same day.
		return (julianDayNumber1 == julianDayNumber2)
				|| (date1.compareTo(date2) <= 0);
	}

	public static long currentTimeSec() {
		long seconds = (System.currentTimeMillis() / 1000L);
		return seconds;
	}

	public static int currentHour() {
		return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
	}
}
