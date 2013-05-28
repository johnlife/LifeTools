package ru.johnlife.lifetools.task;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION_CODES;

public class Executor {
    @SuppressLint("NewApi")
	public static <T> void execute(AsyncTask<T, ?, ?> task, T... params) {
    	if (Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
    		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);    		
    	} else {
    		task.execute(params);
    	}
    }


}
