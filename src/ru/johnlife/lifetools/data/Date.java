package ru.johnlife.lifetools.data;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;

public class Date {
	@SuppressLint("SimpleDateFormat")
	private static final SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
	@SuppressLint("SimpleDateFormat")
	private static final SimpleDateFormat tf = new SimpleDateFormat("HH:mm");
	
	private Calendar cal = Calendar.getInstance(Constants.RUSSIAN);
	
	public Date() {}
	
	public Date(long millis) {
		cal.setTimeInMillis(millis);
	}

	public String getTime()	{
		return tf.format(cal.getTime());
	}
	
	public int getHours() {
		return cal.get(Calendar.HOUR_OF_DAY);
	}
	
	public int getMinutes() {
		return cal.get(Calendar.MINUTE);
	}
	
	public Calendar getCleanDate() {
		Calendar date = (Calendar) cal.clone();
		date.set(Calendar.MILLISECOND, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.HOUR_OF_DAY, 0);
		return date;
	}
	
	@Override
	public String toString() {
		return df.format(cal.getTime());
	}
	
	public long toLong() {
		return cal.getTimeInMillis();
	}
	
}
