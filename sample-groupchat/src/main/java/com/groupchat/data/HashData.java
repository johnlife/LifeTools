package com.groupchat.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.groupchat.app.GroupChatApp;
import com.groupchat.tools.SHA1;

import ru.johnlife.lifetools.data.AbstractFireBaseData;

/**
 * Created by yanyu on 4/13/2016.
 */
public class HashData extends AbstractFireBaseData {
    @JsonIgnore
    public static final String FIREBASE_ROOT = "hash";

    private String value;

    /*json*/ HashData() {
    }

    public HashData(UserData user, String password) {
        super();
        value = user.getId();
        setId(generatePassHash(password));
    }

    private String generatePassHash(String password) {
        return SHA1.hash(SHA1.hash(SHA1.hash(SHA1.hash(password)+GroupChatApp.class.getSimpleName())+getClass().getSimpleName())+value);
    }

    @JsonValue
    /*json*/ String getValue() {
        return value;
    }

    @Override
    public String getItemsRoot() {
        return FIREBASE_ROOT;
    }
}
