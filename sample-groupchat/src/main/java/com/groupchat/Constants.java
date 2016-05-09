package com.groupchat;

import android.app.Activity;

import com.groupchat.service.BackgroundService;

import ru.johnlife.lifetools.ClassConstantsProvider;
import ru.johnlife.lifetools.service.BaseBackgroundService;

/**
 * Created by yanyu on 4/7/2016.
 */
public interface Constants {

    String FIREBASE_MAIN_URI = "https://just-chat-johnlife.firebaseio.com/";
    ClassConstantsProvider CLASS_CONSTANTS = new ClassConstantsProvider() {
        @Override
        public Class<? extends Activity> getLoginActivityClass() {
            return null;
        }

        @Override
        public Class<? extends BaseBackgroundService> getBackgroundServiceClass() {
            return BackgroundService.class;
        }
    };
    String ARG_CHAT_NAME = "chat.name";
    String CHAT_GENERAL = "general";
    String ACTION_LOGIN_CHANGED = "changed.groupchat.johnlife.ru";
}
