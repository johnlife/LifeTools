package ru.johnlife.lifetools.task;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;

public class Preferences {

	@SuppressLint("NewApi")
	public static void apply(SharedPreferences.Editor prefs) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			prefs.apply();
		} else {
			prefs.commit();
		}
	}
}
