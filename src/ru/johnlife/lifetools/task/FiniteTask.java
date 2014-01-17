package ru.johnlife.lifetools.task;

import android.os.AsyncTask;

public abstract class FiniteTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
	public boolean isFinished() {
		return getStatus() == AsyncTask.Status.FINISHED;
	}

}
