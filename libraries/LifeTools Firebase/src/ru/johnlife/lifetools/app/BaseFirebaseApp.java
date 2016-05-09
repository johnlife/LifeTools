package ru.johnlife.lifetools.app;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by yanyu on 4/7/2016.
 */
public class BaseFirebaseApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
    }
}
