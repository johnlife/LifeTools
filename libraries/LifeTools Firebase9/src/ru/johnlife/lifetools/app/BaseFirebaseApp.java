package ru.johnlife.lifetools.app;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by yanyu on 4/7/2016.
 */
public class BaseFirebaseApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
