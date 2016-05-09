package com.groupchat.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.custom.camera.CropImageIntentBuilder;
import com.firebase.client.DataSnapshot;
import com.groupchat.Constants;
import com.groupchat.R;
import com.groupchat.data.UserData;
import com.groupchat.fragment.ChatFragment;
import com.groupchat.fragment.CreatePasswordHandler;
import com.groupchat.service.BackgroundService;
import com.groupchat.tools.IconLoader;

import java.io.File;

import ru.johnlife.lifetools.ClassConstantsProvider;
import ru.johnlife.lifetools.activity.BaseDrawerActivity;
import ru.johnlife.lifetools.fragment.BaseAbstractFragment;
import ru.johnlife.lifetools.listener.ChildAddedListener;
import ru.johnlife.lifetools.service.BaseBackgroundService;
import ru.johnlife.lifetools.service.BaseFirebaseBackgroundService;
import ru.johnlife.lifetools.service.OnObjectReadyListener;

/**
 * Created by yanyu on 4/7/2016.
 */
public class MainActivity extends BaseDrawerActivity {
    private static final int PICK_IMAGE = 0x13;
    private static final int CROP_DATA = 0x52;

    private SparseArray<String> chatsCache = new SparseArray<>();
    private ChatFragment lastChat;
    private MenuItem passItem;
    private MenuItem logoutItem;
    private SubMenu chatsList;
    private UserData loggedUser;
    private IconLoader iconLoader;

    private BroadcastReceiver loginBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!Constants.ACTION_LOGIN_CHANGED.equals(intent.getAction())) return;
            final BackgroundService service = BackgroundService.getInstance();
            service.getLoggedUser(new OnObjectReadyListener<UserData>() {
                @Override
                public void objectReady(UserData user) {
                    updateUserInfo(service, user);
                }
            });
        }
    };

    private ChildAddedListener chatListListener = new ChildAddedListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            final String key = dataSnapshot.getKey();
            BackgroundService.getInstance().getChatName(key, new OnObjectReadyListener<String>() {
                @Override
                public void objectReady(String name) {
                    int id = getId(key);
                    chatsList.add(R.id.chat_list, id, Menu.NONE, name);
                    chatsCache.put(id, key);
                }
            });
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            final String key = dataSnapshot.getValue().toString();
            int id = getId(key);
            chatsList.removeItem(id);
            chatsCache.remove(id);
            if (lastChat != null && lastChat.getChatId().equals(key)) {
                changeChat(Constants.CHAT_GENERAL);
            }
        }

        public int getId(String chatId) {
            return chatId.hashCode();
        }
    };

    private BaseFirebaseBackgroundService.ObjectChangesListener<UserData> profileChangesListener = new BaseFirebaseBackgroundService.ObjectChangesListener<UserData>(){
        @Override
        public void onObjectChanged(UserData user) {
            ImageView avatarView = (ImageView) findDrawerViewById(R.id.main_avatar);
            if (avatarView != null) {
                iconLoader.showAvatar(user, avatarView);
                avatarView.setOnClickListener(null == user ? null : changeAvatarListener);
            }
            TextView userName = (TextView) findDrawerViewById(R.id.user);
            if (userName != null) {
                if (user == null || user.getNickname() == null) {
                    userName.setText(R.string.anonymous);
                } else {
                    userName.setText(user.getNickname());
                }
            }
        }
    };

    private View.OnClickListener changeAvatarListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, PICK_IMAGE);
        }
    };

    @Override
    protected boolean shouldBeLoggedIn() {
        return false;
    }

    @Override
    protected ClassConstantsProvider getClassConstants() {
        return Constants.CLASS_CONSTANTS;
    }

    @NonNull
    @Override
    protected BaseAbstractFragment createInitialFragment() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iconLoader = new IconLoader(this);
        requestService(new BaseBackgroundService.Requester<BackgroundService>() {
            @Override
            public void requestService(final BackgroundService service) {
                service.getLoggedUser(new OnObjectReadyListener<UserData>() {
                    @Override
                    public void objectReady(UserData user) {
                        updateUserInfo(service, user);
                    }
                });
            }
        });
        registerReceiver(loginBroadcastReceiver, new IntentFilter(Constants.ACTION_LOGIN_CHANGED));
    }

    private void setLoginItemsVisibility(MenuItem logoutItem, MenuItem passItem, UserData user) {
        if (null != logoutItem) logoutItem.setVisible(null != user);
        if (null != passItem) passItem.setVisible((null != user) && !user.isPasswordSet());
    }

    private void updateUserInfo(final BackgroundService service, UserData user) {
        loggedUser = user;
        setLoginItemsVisibility(logoutItem, passItem, user);
        Menu menu = getMainMenu();
        if (menu != null) {
            if (chatsList == null) {
                chatsList = menu.findItem(R.id.chat_list_header).getSubMenu();
                if (null == chatsList) {
                    throw new IllegalStateException("Cannot find chatlist submenu in drawer");
                }
            }
            chatsList.clear();
            chatsCache.clear();
            service.listenForUserChanges(profileChangesListener, chatListListener);
            setLoginItemsVisibility(menu.findItem(R.id.log_out), menu.findItem(R.id.create_password), user);
        }
        profileChangesListener.onObjectChanged(user);
        if (user != null && lastChat != null) {
            service.checkUserAccess(user, lastChat.getChatId(),
                new BaseFirebaseBackgroundService.BooleanListener() {
                    @Override
                    public void onTrue() {}

                    @Override
                    public void onFalse() {
                        changeChat(Constants.CHAT_GENERAL);
                    }
                }
            );
        } else {
            changeChat(Constants.CHAT_GENERAL);
        }
    }

    private void changeChat(String chatId) {
        if (null == chatId || (lastChat != null && lastChat.getChatId().equals(chatId))) return;
        lastChat = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(Constants.ARG_CHAT_NAME, chatId);
        lastChat.setArguments(args);
        changeFragment(lastChat);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(loginBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean init = null == logoutItem;
        getMenuInflater().inflate(R.menu.main_menu, menu);
        passItem = menu.findItem(R.id.create_password);
        logoutItem = menu.findItem(R.id.log_out);
        if (init) {
            requestService(new BaseBackgroundService.Requester<BackgroundService>() {
                @Override
                public void requestService(final BackgroundService service) {
                    service.getLoggedUser(new OnObjectReadyListener<UserData>() {
                        @Override
                        public void objectReady(UserData user) {
                            setLoginItemsVisibility(logoutItem, passItem, user);
                        }
                    });
                }
            });
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create_password:
                createPassword();
                return true;
            case R.id.log_out:
                requestService(new BaseBackgroundService.Requester<BackgroundService>() {
                    @Override
                    public void requestService(final BackgroundService service) {
                        if (loggedUser.isPasswordSet()) {
                            service.logout();
                        } else {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(R.string.warning_logout)
                                    .setMessage(R.string.warning_logout_message)
                                    .setPositiveButton(R.string.warning_logout_set, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            createPassword();
                                        }
                                    })
                                    .setNegativeButton(R.string.warning_logout_kill, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            service.logout();
                                        }
                                    }).show();
                        }
                    }
                });
                return true;
            default:
                changeChat(chatsCache.get(item.getItemId()));
                return true;
        }
    }

    private void createPassword() {
        BaseAbstractFragment currentFragment = getCurrentFragment();
        if ((null != currentFragment) && (currentFragment instanceof CreatePasswordHandler)) {
            ((CreatePasswordHandler) currentFragment).createPasswordAction();
        }
    }

    @Override
    protected DrawerDescriptor getDrawerDescriptor() {
        return new DrawerDescriptor(R.layout.drawer_header, R.menu.drawer);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        File croppedImageFile = new File(getCacheDir() + "/croped.jpg");
        croppedImageFile.deleteOnExit();
        if(requestCode == PICK_IMAGE && data != null && data.getData() != null) {
            CropImageIntentBuilder cropImage = new CropImageIntentBuilder(512, 512, Uri.fromFile(croppedImageFile));
            cropImage.setOutlineColor(getResources().getColor(R.color.accent));
            cropImage.setSourceImage(data.getData());
            startActivityForResult(cropImage.getIntent(this), CROP_DATA);
        } else if (requestCode == CROP_DATA) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    final Bitmap bmp = BitmapFactory.decodeFile(croppedImageFile.getAbsolutePath());
                    requestService(new BaseBackgroundService.Requester<BackgroundService>() {
                        @Override
                        public void requestService(BackgroundService service) {
                            service.setUserAvatar(bmp);
                        }
                    });
                }
            }
        }
    }

}
