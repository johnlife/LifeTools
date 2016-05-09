package com.groupchat.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.groupchat.Constants;
import com.groupchat.service.BackgroundService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.johnlife.lifetools.data.WatchedData;

/**
 * Created by yanyu on 4/7/2016.
 */
public class UserData extends WatchedData {
    @JsonIgnore
    public static final String FIREBASE_ROOT = "users";

    private String nickname;
    private String avatar;
    private Map<String, String> chats;
    private boolean passwordSet = false;

    /*package*/ UserData() {
    }

    public UserData(String nickname) {
        this.nickname = nickname;
    }

    public UserData(String nickname, String avatar) {
        this.avatar = avatar;
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getNickname() {
        return nickname;
    }

    public void setPassword(String password) {
        BackgroundService service = BackgroundService.getInstance();
        service.persist(new HashData(this, password));
        passwordSet = true;
        service.persist(this);
    }

    public boolean isPasswordSet() {
        return passwordSet;
    }

    @Override
    public String getItemsRoot() {
        return FIREBASE_ROOT;
    }


    @Override
    public String toString() {
        return super.toString()+" | "+nickname;
    }

    public void addChat(final ChatData chat) {
        if (chats == null) {
            chats = new HashMap<>(1);
        }
        chats.put(chat.getId(), chat.getName());
    }

    public List<ChatData> getChats() {
        if (chats == null) {
            return Arrays.asList(new ChatData[]{new ChatData(Constants.CHAT_GENERAL, Constants.CHAT_GENERAL)});
        }
        List<ChatData> value = new ArrayList<>(chats.size());
        for (Map.Entry<String, String> entry : chats.entrySet()) {
            value.add(new ChatData(entry.getKey(), entry.getValue()));
        }
        Collections.sort(value);
        return value;
    }

    public static UserData template(String id) {
        UserData user = new UserData();
        user.setId(id);
        return user;
    }


    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

}
