package com.groupchat.app;

import android.content.Intent;

import com.groupchat.Constants;

import ru.johnlife.lifetools.app.BaseFirebaseApp;

/**
 * Created by yanyu on 4/7/2016.
 */
public class GroupChatApp extends BaseFirebaseApp {
    @Override
    public void onCreate() {
        super.onCreate();
        startService(new Intent(this, Constants.CLASS_CONSTANTS.getBackgroundServiceClass()));
    }
}
