package org.emoflon.flight.model.util;

import java.util.Calendar;
import java.util.Date;

import Flights.FlightsFactory;
import Flights.TimeStamp;

public final class LongDateHelper {
	
	protected static FlightsFactory factory = FlightsFactory.eINSTANCE;
	
	/**
	 * one second in milliseconds
	 */
	public static long SECONDSINMS = 1000;
	/**
	 * one minute in milliseconds
	 */
	public static long MINUTEINMS = 60000;
	/**
	 * one hour in milliseconds
	 */
	public static long HOURINMS = 3600000;
	/**
	 * one day in milliseconds
	 */
	public static long DAYINMS = 86400000;
	/**
	 * one week in milliseconds
	 */
	public static long WEEKINMS = 604800000;
	
	public static TimeStamp createTimeStamp(long time) {
		TimeStamp stamp = factory.createTimeStamp();
		stamp.setTime(time);
		return stamp;
	}
	
	public static TimeStamp createTimeStamp(final TimeStamp stamp, long timeIncrement) {
		return createTimeStamp(stamp.getTime()+timeIncrement);
	}
	
	public static TimeStamp createTimeStamp(final TimeStamp stamp, int dayOfWeek, int hour, int min, boolean positive) {
		if(positive)
			return createTimeStamp(stamp.getTime()+getTimeInMs(dayOfWeek, hour, min));
		
		return createTimeStamp(stamp.getTime()-getTimeInMs(dayOfWeek, hour, min));
	}
	
	public static TimeStamp createTimeStamp(final TimeStamp stamp, int hour, int min, boolean positive) {
		if(positive)
			return createTimeStamp(stamp.getTime()+getTimeInMs(0, hour, min));
		
		return createTimeStamp(stamp.getTime()-getTimeInMs(0, hour, min));
	}
	
	public static TimeStamp createTimeStamp(final TimeStamp stamp, int min, boolean positive) {
		if(positive)
			return createTimeStamp(stamp.getTime()+getTimeInMs(0, 0, min));
		
		return createTimeStamp(stamp.getTime()-getTimeInMs(0, 0, min));
	}
	
	/**
	 * @param time1 in milliseconds (see Java-Date)
	 * @param time2 in milliseconds (see Java-Date)
	 * @return string in format Days:Hours:Mins
	 */
	public static String deltaAsString(long time1, long time2) {
		return deltaAsString(time2 - time1);
	}
	
	/**
	 * @param time delta in milliseconds (see Java-Date)
	 * @return string in format Days:Hours:Mins:Secs
	 */
	public static String deltaAsString(long delta) {
		long days = delta / DAYINMS;
		long hours = (delta - days * DAYINMS) / HOURINMS;
		long mins = (delta - days * DAYINMS - hours * HOURINMS) / MINUTEINMS;
		long secs = (delta - days * DAYINMS - hours * HOURINMS - mins * MINUTEINMS) / SECONDSINMS;
		return days + "d:"+ hours + "h:" + mins + "m:" + secs +"s";
	}
	
	/**
	 * @param time in milliseconds (see Java-Date)
	 * @return string in format hh:mm-DD:MM:YYYY, while DD represents the current day of the month (e.g. '01'), MM represents the month (e.g. 'Feb'), and YYYY the current year (e.g. '2020')
	 */
	public static String getString_mmhhDDMMYYYY(long time) {
		Date date = new Date(time);
		String dateString = date.toString();
		String[] splitDate = dateString.split(" ");
		String[] splitTime = splitDate[3].split(":");
		return  splitTime[0] + ":" + splitTime[1] + ":" + splitDate[2] + ":" + splitDate[1] + ":" + splitDate[5];
	}
	
	/**
	 * @param time in milliseconds (see Java-Date)
	 * @return string in format DDMMYYY, while DD represents the current day of the month (e.g. '01'), MM represents the month (e.g. 'Feb'), and YYYY the current year (e.g. '2020')
	 */
	public static String getStringDDMMYYYY(long time) {
		Date date = new Date(time);
		String dateString = date.toString();
		String[] splitDate = dateString.split(" ");
		return splitDate[2] + splitDate[1] + splitDate[5];
	}
	/**
	 * @param day of the date (e.g. '01')
	 * @param month of the date (e.g. '1'), while January is 0
	 * @param year of the date (e.g. '2020')
	 * @return String to the given date
	 */
	public static String getStringDDMMYYYY(int day, int month, int year) {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, day, 0, 0);
		return getStringDDMMYYYY(cal.getTimeInMillis());
	}
	/**
	 * @param day of the date (e.g. '01')
	 * @param month of the date (e.g. '1'), while January is 0
	 * @param year of the date (e.g. '2020')
	 * @param hour of the date (e.g. '12')
	 * @param min of the date (e.g. '30')
	 * @return time in milliseconds (see Java-Date)
	 */
	public static long getDate(int day, int month, int year, int hour, int min) {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, day, hour, min,0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
	}
	/**
	 * @param day of the date (e.g. '01')
	 * @param month of the date (e.g. '1'), while January is 0
	 * @param year of the date (e.g. '2020')
	 * @return time in milliseconds (see Java-Date)
	 */
	public static long getDate(int day, int month, int year) {
		return getDate(day, month, year, 0, 0);
	}
	/**
	 * @param dayOfWeek off offset
	 * @param hour of the offset
	 * @param min of the offset
	 * @return the time offset in ms
	 */
	public static long getTimeInMs(int dayOfWeek, int hour, int min) {
		return dayOfWeek*DAYINMS+hour*HOURINMS+min*MINUTEINMS;
	}
	/**
	 * @param hour of the offset
	 * @param min of the offset
	 * @return the time offset in ms
	 */
	public static long getTimeInMs(int hour, int min) {
		return getTimeInMs(0, hour, min);
	}
	
	/**
	 * @param hour of the offset
	 * @param min of the offset
	 * @return the time offset in ms
	 */
	public static long getTimeInMs(int min) {
		return getTimeInMs(0, 0, min);
	}
}
