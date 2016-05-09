package com.groupchat.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ru.johnlife.lifetools.adapter.FireBaseAdapter;
import ru.johnlife.lifetools.data.AbstractFireBaseData;
import ru.johnlife.lifetools.data.WatchedData;
import ru.johnlife.lifetools.service.OnObjectReadyListener;

public class MessageData extends AbstractFireBaseData implements WatchedData.Watcher<UserData> {
    @JsonIgnore
    public static final String FIREBASE_ROOT = "messages";
    private String text;
    private long timestamp;
    private UserData user;
    @JsonIgnore
    private ChatData chat;

    protected MessageData() {
    }

    public MessageData(String text, UserData user) {
        this.text = text;
        this.user = user;
        timestamp = System.currentTimeMillis();
    }

    public String getText() {
        return text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @JsonProperty("user")
    private String getUserKey() {
        return user.getId();
    }

    @JsonProperty("user")
    private void setUserFromKey(String id) {
        AbstractFireBaseData.getById(UserData.template(id), new OnObjectReadyListener<UserData>(){
            @Override
            public void objectReady(UserData object) {
                user = object;
                notifyChanged();
            }
        });
    }

    @Override
    public void addListener(FireBaseAdapter.BaseFirebaseListChildListener listener) {
        super.addListener(listener);
        if (null != user) {
            user.watchedBy(this);
        }
    }

    @Override
    public boolean removeListener(FireBaseAdapter.BaseFirebaseListChildListener listener) {
        boolean empty = super.removeListener(listener);
        if (empty && (null != user)) {
            user.unwatchedBy(this);
        }
        return empty;
    }

    @Override
    public String getItemsRoot() {
        if (chat == null) throw new IllegalStateException("Cannot persist message outside of chat");
        return chat.getMessagesRoot();
    }

    public UserData getUser() {
        return user;
    }

    /*package*/ void setUser(UserData user) {
        this.user = user;
    }

    @Override
    public void replacedBy(UserData newVersion) {
        user = newVersion;
        notifyChanged();
    }

    public void setChat(ChatData chat) {
        this.chat = chat;
    }

    public void setText(String text) {
        this.text = text;
    }
}
