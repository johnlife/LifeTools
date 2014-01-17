package ru.johnlife.lifetools.db;

import android.database.sqlite.SQLiteDatabase;

public interface Persistable {

	public void toDb(SQLiteDatabase db);

}