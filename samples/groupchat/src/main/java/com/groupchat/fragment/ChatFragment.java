package com.groupchat.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.groupchat.Constants;
import com.groupchat.R;
import com.groupchat.data.ChatData;
import com.groupchat.data.MessageData;
import com.groupchat.data.UserData;
import com.groupchat.service.BackgroundService;
import com.groupchat.tools.IconLoader;
import com.groupchat.ui.GroupchatLoginController;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import ru.johnlife.lifetools.activity.BaseMainActivity;
import ru.johnlife.lifetools.adapter.BaseAdapter;
import ru.johnlife.lifetools.adapter.FireBaseAdapter;
import ru.johnlife.lifetools.auth.AbstractLoginPaneController;
import ru.johnlife.lifetools.fragment.BaseListFragment;
import ru.johnlife.lifetools.service.BaseBackgroundService;
import ru.johnlife.lifetools.service.OnObjectReadyListener;

import static ru.johnlife.lifetools.ui.AnimationUtils.hideDown;
import static ru.johnlife.lifetools.ui.AnimationUtils.showUp;

public class ChatFragment extends BaseListFragment<MessageData> implements BaseMainActivity.BackHandler, CreatePasswordHandler {
    private static final DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
    private static final DateFormat timeFormat = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT);

    private BroadcastReceiver loginBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!Constants.ACTION_LOGIN_CHANGED.equals(intent.getAction())) return;
            BackgroundService.getInstance().getLoggedUser(new OnObjectReadyListener<UserData>() {
                @Override
                public void objectReady(UserData user) {
                    loggedUser = user;
                    if (null == user) {
                        updateUiToState(STATE_LOCKED);
                    } else {
                        updateUiToState(STATE_UNLOCKED);
                        loggedUser = user;
                    }
                    getAdapter().notifyDataSetChanged();
                }
            });
        }
    };

    @Override
    protected BaseAdapter<MessageData> instantiateAdapter(final Context context) {
        return new FireBaseAdapter<MessageData>(R.layout.item_message, MessageData.class) {
            private View raisedCard = null;
            private static final String DATE = "A";
            private IconLoader loader = new IconLoader(context);
            private Date datetime = new Date();
            private Calendar cal = GregorianCalendar.getInstance();

            private Runnable scroller = new Runnable() {
                @Override
                public void run() {
                    getList().smoothScrollToPosition(getCount()-1);
                }
            };

            {
                getList().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != raisedCard) {
                            lowerListener.onClick(raisedCard);
                            raisedCard = null;
                        }
                    }
                });
            }

            private View.OnClickListener raiseListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != raisedCard) {
                        lowerListener.onClick(raisedCard);
                    }
                    raisedCard = v;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        v.animate().translationZ(4).start();
                    } else {
                        ((CardView) v).setCardElevation(4);
                    }
                    v.findViewById(R.id.toolbar).setVisibility(View.VISIBLE);
                    v.setOnClickListener(lowerListener);
                }
            };

            private View.OnClickListener lowerListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        v.animate().translationZ(0).start();
                    } else {
                        ((CardView) v).setCardElevation(0);
                    }
                    v.findViewById(R.id.toolbar).setVisibility(View.GONE);
                    v.setOnClickListener(raiseListener);
                    if (null != editingMessage) {
                        editingMessage = null;
                        message.setText("");
                    }
                }
            };



            private String formatTime(long timestamp) {
                datetime.setTime(timestamp);
                return timeFormat.format(datetime);
            }

            final class MessageDateData extends MessageData {
                private String date;

                public MessageDateData(long timestamp) {
                    datetime.setTime(timestamp);
                    this.date = dateFormat.format(datetime);
                }

                @Override
                public String getText() {
                    return date;
                }

                @Override
                public String getId() {
                    return DATE;
                }
            }

            @Override
            public int getItemViewType(int position) {
                return getItem(position).getId() == DATE ? R.layout.item_date : R.layout.item_message;
            }

            @Override
            public void add(MessageData item) {
                empty.setVisibility(View.GONE);
                boolean needsScroll = layoutManager.findLastVisibleItemPosition() == (getCount()-1);
                if (
                    (getCount() == 0) || (
                        (getItem(getCount()-1).getId() != DATE) &&
                        (getDay(item) != getDay(getItem(getCount()-1)))
                    )
                ) {
                    super.add(new MessageDateData(item.getTimestamp()));
                }
                super.add(item);
                item.setChat(chat);
                if (needsScroll) {
                    getList().postDelayed(scroller, 100);
                }
            }

            @SuppressWarnings("AccessStaticViaInstance")
            private int getDay(MessageData item) {
                cal.setTimeInMillis(item.getTimestamp());
                return cal.get(cal.DAY_OF_MONTH);
            }

            @Override
            protected ViewHolder<MessageData> createViewHolder(final View view) {
                return new FirebaseViewHolder(view) {
                    private TextView author = (TextView) view.findViewById(R.id.author);
                    private TextView text = (TextView) view.findViewById(R.id.text);
                    private TextView time = (TextView) view.findViewById(R.id.time);
                    private ImageView avatar = (ImageView) view.findViewById(R.id.avatar);
                    private ItemizedClickListener delete = new ItemizedClickListener() {
                        @Override
                        public void onClick(View v) {
                            BackgroundService.getInstance().delete(getItem());
                        }
                    };
                    private ItemizedClickListener edit = new ItemizedClickListener() {
                        @Override
                        public void onClick(View v) {
                            editMessage(getItem());
                        }
                    };

                    {
                        View deleteButton = view.findViewById(R.id.deleteButton);
                        if (null != deleteButton) deleteButton.setOnClickListener(delete);
                        View editButton = view.findViewById(R.id.editButton);
                        if (null != editButton) editButton.setOnClickListener(edit);
                    }

                    @SuppressWarnings("StringEquality")
                    @Override
                    protected void hold(MessageData item) {
                        text.setText(item.getText());
                        if (item.getId() == DATE) return;
                        time.setText(formatTime(item.getTimestamp()));
                        UserData user = item.getUser();
                        view.setOnClickListener(null);
                        if (null != user) {
                            author.setText(user.getNickname());
                            if (user.equals(loggedUser)) {
                                view.setOnClickListener(raiseListener);
                            }
                        }
                        delete.setItem(item);
                        edit.setItem(item);
                        loader.showAvatar(user, avatar);
                    }
                };
            }
        };
    }

    private static final int STATE_LOCKED = 1;
    private static final int STATE_LOGGING_IN = 2;
    private static final int STATE_UNLOCKED = 3;
    private static final int STATE_CREATE_PASS = 4;

    private ChatData chat;
    private UserData loggedUser;
    private View login;
    private AbstractLoginPaneController loginPane;
    private View messagePane;
    private TextView message;
    private View empty;
    private LinearLayoutManager layoutManager;
    private int currentState;
    private MessageData editingMessage;

    private void editMessage(MessageData item) {
        editingMessage = item;
        message.setText(item.getText());
    }

    @NonNull
    @Override
    protected LinearLayoutManager getListLayoutManager() {
        layoutManager = super.getListLayoutManager();
        return layoutManager;
    }

    private void updateUiToState(int state) {
        if (currentState == state) return;
        if (state == STATE_LOCKED) {
            showUp(messagePane, null);
            showUp(login, null);
            message.setEnabled(false);
        } else if (state == STATE_UNLOCKED) {
            hideDown(login, null);
            showUp(messagePane, null);
            message.setEnabled(true);
            message.requestFocus();
        } else if (state == STATE_LOGGING_IN || state == STATE_CREATE_PASS) {
            hideDown(login, null);
            hideDown(messagePane, loginPane.show(state == STATE_LOGGING_IN ? 0 : 3));
        }
        currentState = state;
    }

    @Override
    protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
        return createToolbarFrom(R.layout.toolbar_chat);
    }

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.createView(inflater, container, savedInstanceState);
        final String chatId = getChatId();
        empty = view.findViewById(R.id.empty_list);
        message = (TextView) view.findViewById(R.id.message);
        login = view.findViewById(R.id.login);
        loginPane = new GroupchatLoginController(view.findViewById(R.id.login_pane), this);
        messagePane = view.findViewById(R.id.message_pane);
        view.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = message.getText().toString();
                if (null == editingMessage) {
                    chat.addMessage(new MessageData(messageText, loggedUser));
                } else {
                    editingMessage.setText(messageText);
                    BackgroundService.getInstance().persist(editingMessage);
                    editingMessage = null;
                }
                message.setText("");
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUiToState(STATE_LOGGING_IN);
            }
        });
        requestService(new BaseBackgroundService.Requester<BackgroundService>() {
            @Override
            public void requestService(final BackgroundService service) {
                login.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        updateUiToState(service.isLoggedIn() ? STATE_UNLOCKED : STATE_LOCKED);
                        login.removeOnLayoutChangeListener(this);
                    }
                });
                service.getLoggedUser(new OnObjectReadyListener<UserData>() {
                    @Override
                    public void objectReady(UserData object) {
                        loggedUser = object;
                    }
                });
                service.get(new ChatData(chatId), new OnObjectReadyListener<ChatData>() {
                    @Override
                    public void objectReady(ChatData chatData) {
                        if (chatData == null) {
                            chatData = new ChatData(chatId);
                            service.persist(chatData);
                        }
                        chat = chatData;
                        setTitle(chat.getName());
                        service.getMessages(chatData).addChildEventListener(((FireBaseAdapter)getAdapter()).getListener());
                    }
                });
            }
        });
        message.getContext().registerReceiver(loginBroadcastReceiver, new IntentFilter(Constants.ACTION_LOGIN_CHANGED));
        return view;
    }

    @Override
    protected String getTitle(Resources res) {
        return getChatId();
    }

    public String getChatId() {
        return getArguments().getString(Constants.ARG_CHAT_NAME, Constants.CHAT_GENERAL);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_chat;
    }

    @Override
    protected void onItemClick(MessageData item) {

    }

    @Override
    public void onDestroy() {
        message.getContext().unregisterReceiver(loginBroadcastReceiver);
        BackgroundService.getInstance().getMessages(chat).removeEventListener(((FireBaseAdapter)getAdapter()).getListener());
        super.onDestroy();
    }

    @Override
    public boolean handleBack() {
        if (loginPane.isActive()) {
            loginPane.goBack();
            return true;
        }
        return false;
    }

    @Override
    public void createPasswordAction() {
        updateUiToState(STATE_CREATE_PASS);
    }
}
