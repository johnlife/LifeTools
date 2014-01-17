package ru.johnlife.lifetools.db;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

public abstract class DeferredDatabaseHelper extends SQLiteOpenHelper {

	protected static final Object dbLock = new Object();
	private final Set<Persistable> changes = new HashSet<Persistable>(); 
	private Timer persistTimer = new Timer();
	private TimerTask persistTask = null;

	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public DeferredDatabaseHelper(Context context, String name,
			CursorFactory factory, int version,
			DatabaseErrorHandler errorHandler) {
		super(context, name, factory, version, errorHandler);
	}

	public DeferredDatabaseHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	
	/**
	 * Performs asynchronous delayed write. 
	 * All subsequent (during delay) calls are stacked.  
	 */
	public void persist() {
		synchronized (dbLock) {
			if (null != persistTask) {
				persistTask.cancel();
			}
			persistTask = new TimerTask() {
				@Override
				public void run() {
					persistSync();
					persistTask = null;
				}
			};
			persistTimer.schedule(persistTask, 3000);
		}
	}
	
	public void persist(Persistable what) {
		synchronized (changes) {
			changes.add(what);
		}
		persist();
	}

	public void persistSync() {
		synchronized (dbLock) {
			SQLiteDatabase db = getWritableDatabase();
			synchronized (changes) {
				db.beginTransaction();
				for (Persistable change : changes) {
					change.toDb(db);
				}
				db.setTransactionSuccessful();
				db.endTransaction();
				changes.clear();
			}
			db.close();
			if (null != persistTask) {
				persistTask.cancel();
				persistTask = null;
			}
		}
	}
	

}
