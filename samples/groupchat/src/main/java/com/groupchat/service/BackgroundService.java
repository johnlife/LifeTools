package com.groupchat.service;

import android.content.Intent;
import android.graphics.Bitmap;
import android.util.SparseArray;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.groupchat.Constants;
import com.groupchat.data.ChatData;
import com.groupchat.data.HashData;
import com.groupchat.data.UserData;
import ru.johnlife.lifetools.tools.Base64Bitmap;

import ru.johnlife.lifetools.ClassConstantsProvider;
import ru.johnlife.lifetools.data.AbstractFireBaseData;
import ru.johnlife.lifetools.data.Path;
import ru.johnlife.lifetools.listener.ChildAddedListener;
import ru.johnlife.lifetools.listener.DataChangedListener;
import ru.johnlife.lifetools.service.BaseFirebaseBackgroundService;
import ru.johnlife.lifetools.service.OnObjectReadyListener;

/**
 * Created by yanyu on 4/7/2016.
 */
public class BackgroundService extends BaseFirebaseBackgroundService {
    private static BackgroundService instance = null;
    private UserData loggedUser = null;

    private SparseArray<String> userIds = new SparseArray<>();
    private ChildAddedListener userChatsListener;
    private ObjectChangesListener userProfileListener;

    public static BackgroundService getInstance() {
        return instance;
    }

    private ChildEventListener userCacheListener = new DataChangedListener<UserData>(UserData.class) {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            cache(getItem(dataSnapshot));
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            cache(getItem(dataSnapshot));
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            UserData userData = getItem(dataSnapshot);
            userIds.remove(userData.getNickname().toLowerCase().hashCode());
            evict(userData);
        }

        private void cache(UserData data) {
            userIds.put(data.getNickname().toLowerCase().hashCode(), data.getId());
            BackgroundService.this.cache(data);
        }
    };

    @Override
    protected void onFirebaseInitialized() {
        instance = this;
        getUsers().addChildEventListener(userCacheListener);
    }

    public void getLoggedUser(final OnObjectReadyListener<UserData> listener) {
        if (null != loggedUser || null == getSessionId()) {
            listener.objectReady(loggedUser);
        } else {
            AbstractFireBaseData.getById(UserData.template(getSessionId()), new OnObjectReadyListener<UserData>() {
                @Override
                public void objectReady(UserData user) {
                    loggedUser = user;
                    listener.objectReady(loggedUser);
                }
            });
        }
    }

    public boolean findUserByNickname(String nickname, OnObjectReadyListener<UserData> userFound) {
        String id = userIds.get(nickname.toLowerCase().hashCode());
        if (null != id) {
            get(UserData.template(id), userFound);
        }
        return (null != id);
    }

    public void checkUserAccess(UserData user, String chatId, BooleanListener listener) {
        isExist(
            Path.base(user.getItemPath()).append(ChatData.FIREBASE_ROOT).last(chatId), listener
        );
    }
    public void setUserAvatar(Bitmap bmp) {
        if (null == loggedUser) return;
        loggedUser.setAvatar(Base64Bitmap.encodeToBase64(bmp));
        persist(loggedUser);
    }

    public void listenForUserChanges(ObjectChangesListener profileListener, ChildAddedListener chatListener) {
        if (loggedUser == null || chatListener == userChatsListener) return;
        if (userChatsListener != null || userProfileListener != null) {
            unlistenForUserChanges();
        }
        userProfileListener = profileListener;
        userChatsListener = chatListener;
        getChatList().addChildEventListener(chatListener);
        listenForChanges(loggedUser, userProfileListener);
    }

    private void unlistenForUserChanges() {
        if (userChatsListener != null) {
            getChatList().removeEventListener(userChatsListener);
            userChatsListener = null;
        }
        if (userProfileListener != null) {
            stopListening(loggedUser, userProfileListener);
            userProfileListener = null;
        }
    }

    private Firebase getChatList() {
        return getDb().child(Path.append(loggedUser.getItemPath(), ChatData.FIREBASE_ROOT));
    }

    public void getChatName(String id, OnObjectReadyListener<String> listener) {
        get(Path.base(ChatData.FIREBASE_ROOT).append(id).last("name"), String.class, listener);
    }

    public interface AuthenticationListener {
        void failed();
        void success();
    }

    public void checkPassword(final UserData user, String password, final AuthenticationListener listener) {
        final HashData suspectHash = new HashData(user, password);
        getDb().child(suspectHash.getItemPath()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((null == dataSnapshot) || (null == dataSnapshot.getValue())) {
                    listener.failed();
                } else  if (dataSnapshot.getValue().toString().equals(user.getId())) {
                    listener.success();
                } else {
                    listener.failed();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });
    }

    @Override
    protected String getFirebaseMainUri() {
        return Constants.FIREBASE_MAIN_URI;
    }

    @Override
    protected ClassConstantsProvider getClassConstants() {
        return Constants.CLASS_CONSTANTS;
    }

    public Firebase getMessages(ChatData chat) {
        return getDb().child(chat.getMessagesRoot());
    }

    public Firebase getUsers() {
        return getDb().child(UserData.FIREBASE_ROOT);
    }

    public UserData login(UserData user) {
        persist(user);
        loggedUser = user;
        storeSession(user.getId());
        sendBroadcast(new Intent(Constants.ACTION_LOGIN_CHANGED));
        return user;
    }

    public void logout() {
        unlistenForUserChanges();
        loggedUser = null;
        storeSession(null);
        sendBroadcast(new Intent(Constants.ACTION_LOGIN_CHANGED));
    }
}
