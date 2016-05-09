package com.groupchat.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.groupchat.Constants;
import com.groupchat.service.BackgroundService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.johnlife.lifetools.data.AbstractFireBaseData;
import ru.johnlife.lifetools.data.Path;
import ru.johnlife.lifetools.service.OnObjectReadyListener;

/**
 * Created by yanyu on 4/9/2016.
 */
public class ChatData extends AbstractFireBaseData implements Comparable<ChatData>{
    @JsonIgnore
    public static final String FIREBASE_ROOT = "chats";

    private String name;
    private List<UserData> users = new ArrayList<>();

    /*package*/ ChatData() {}

    public ChatData(String id) {
        this(id, null);
    }

    /*package*/ ChatData(String id, String name) {
        super();
        setId(id);
        this.name = name;
    }

    @JsonIgnore
    private OnObjectReadyListener<UserData> userReadyListener = new ListObjectReadyListener<>(users);

    @JsonProperty("users")
    private Map<String, String> getUsersJ() {
        return toIds(users);
    }

    @JsonProperty("users")
    private void setUsersJ(Map<String, String> ids) {
        fromIds(ids, new UserData(), userReadyListener);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addMessage(MessageData message) {
        UserData user = message.getUser();
        BackgroundService service = BackgroundService.getInstance();
        if (!users.contains(user)) {
            user.addChat(this);
            service.persist(user);
            users.add(user);
            service.addToCollection(this, "users", user);
        }
        message.setChat(this);
        service.persist(message);
    }

    @Override
    public String getItemsRoot() {
        return FIREBASE_ROOT;
    }

    public String getMessagesRoot() {
        return Path.append(getItemPath(),MessageData.FIREBASE_ROOT);
    }

    @Override
    public int compareTo(ChatData another) {
        if (Constants.CHAT_GENERAL.equals(getId())) {
            return -1;
        } else if (Constants.CHAT_GENERAL.equals(another.getId())) {
            return 1;
        } else return 0; //TODO: add meaningfull sort
    }
}
