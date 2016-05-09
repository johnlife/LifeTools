package com.groupchat.ui;

import android.view.View;
import android.widget.TextView;

import com.groupchat.R;
import com.groupchat.data.UserData;
import com.groupchat.service.BackgroundService;

import ru.johnlife.lifetools.auth.AbstractLoginPaneController;
import ru.johnlife.lifetools.fragment.BaseAbstractFragment;
import ru.johnlife.lifetools.service.OnObjectReadyListener;

/**
 * Created by yanyu on 4/12/2016.
 */
public class GroupchatLoginController extends AbstractLoginPaneController {

    private UserData loggedUser;
    private BaseAbstractFragment fragment;
    private View.OnClickListener findUserClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String nick = ((TextView) findViewById(R.id.nickname)).getText().toString().trim();
            if (nick.isEmpty()) return;
            BackgroundService service = BackgroundService.getInstance();
            if (!service.findUserByNickname(nick,
                    new OnObjectReadyListener<UserData>() {
                        @Override
                        public void objectReady(UserData user) {
                            //user found
                            loggedUser = user;
                            if (user.isPasswordSet()) {
                                nextStep(1);
                            } else {
                                nextStep(2);
                            }
                        }
                    })
                    ) {
                //create new user
                loggedUser = service.login(new UserData(nick));
                fragment.addMessage(String.format("Welcome, %s. You can create password.", loggedUser.getNickname()), getContext().getString(R.string.create_btn), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        show(3).onAnimationEnd(null);
                    }
                });
                done();
            }

        }
    };

    private View.OnClickListener checkPasswordClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String pass = ((TextView) findViewById(R.id.password)).getText().toString().trim();
            final TextView error = (TextView) findViewById(R.id.error);
            error.setVisibility(View.INVISIBLE);
            final BackgroundService service = BackgroundService.getInstance();
            service.checkPassword(loggedUser, pass, new BackgroundService.AuthenticationListener() {
                @Override
                public void failed() {
                    error.setVisibility(View.VISIBLE);
                    error.setText(R.string.error_wrong_password);
                }

                @Override
                public void success() {
                    loggedUser = service.login(loggedUser);
                    fragment.addMessage(String.format("Welcome back, %s", loggedUser.getNickname()));
                    done();
                }
            });
        }
    };

    private View.OnClickListener createPasswordClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String pass = ((TextView) findViewById(R.id.password)).getText().toString().trim();
            loggedUser.setPassword(pass);
            fragment.addMessage("Password created");
            BackgroundService.getInstance().login(loggedUser); //notify everyone
            done();
        }
    };

    private LoginStepDescriptor steps[] = new LoginStepDescriptor[]{
            new LoginStepDescriptor(
                    R.layout.login_step1,
                    new LoginStepDescriptor.Listeners(){{
                        put(R.id.do_login, findUserClickListener);
                    }},
                    LoginStepDescriptor.NO_BACK
            ),
            new LoginStepDescriptor(
                    R.layout.login_step2,
                    new LoginStepDescriptor.Listeners(){{
                        put(R.id.create, findUserClickListener);
                        put(R.id.do_login, checkPasswordClickListener);
                    }},
                    0
            ),
            new LoginStepDescriptor(
                    R.layout.login_step2a,
                    new LoginStepDescriptor.Listeners(){{
                        put(R.id.create, findUserClickListener);
                    }},
                    0
            ),
            new LoginStepDescriptor(
                    R.layout.login_step3,
                    new LoginStepDescriptor.Listeners(){{
                        put(R.id.create, createPasswordClickListener);
                    }},
                    LoginStepDescriptor.DONE
            ),
    };

    public GroupchatLoginController(View mainCard, BaseAbstractFragment fragment) {
        super(mainCard);
        this.fragment = fragment;
    }

    @Override
    protected LoginStepDescriptor[] getSteps() {
        return steps;
    }

    @Override
    protected void onDone() {
    }

    @Override
    protected void abort() {
        super.abort();
        BackgroundService.getInstance().logout();
    }
}
