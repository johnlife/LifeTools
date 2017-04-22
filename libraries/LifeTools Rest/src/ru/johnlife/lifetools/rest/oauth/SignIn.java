package ru.johnlife.lifetools.rest.oauth;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.SparseArray;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;

import ru.johnlife.lifetools.rest.oauth.activity.SigninActivity;
import ru.johnlife.lifetools.rest.task.OnTaskCompleteListener;

/**
 * Created by Yan Yurkin
 * 30 October 2016
 */

public class SignIn {
    private static abstract class Task extends AsyncTask<Void, Void, Void>{}
    private static class ServiceData {
        private OAuth10aService service;
        private OnTaskCompleteListener<String> verifierListener;

        public ServiceData(OAuth10aService service, OnTaskCompleteListener<String> verifierListener) {
            this.service = service;
            this.verifierListener = verifierListener;
        }

        public OAuth10aService getService() {
            return service;
        }

        public OnTaskCompleteListener<String> getVerifierListener() {
            return verifierListener;
        }
    }

    private static SparseArray<ServiceData> services = new SparseArray<>();

    public static void into(final Context context, final String name, final OAuth10aService service, final OnTaskCompleteListener<OAuth1AccessToken> listener) {
        new Task(){
            private String url;

            @Override
            protected Void doInBackground(Void... voids) {
                final OAuth1RequestToken requestToken = service.getRequestToken();
                url = service.getAuthorizationUrl(requestToken);
                services.put(name.hashCode(), new ServiceData(service, new OnTaskCompleteListener<String>() {
                    @Override
                    public void success(final String verifier) {
                        new Task(){
                            @Override
                            protected Void doInBackground(Void... voids) {
                                final OAuth1AccessToken accessToken = service.getAccessToken(requestToken, verifier);
                                listener.success(accessToken);
                                return null;
                            }
                        }.execute();
                    }

                    @Override
                    public void error(String message) {
                        listener.error(message);
                    }
                }));
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent dialogIntent = new Intent(context, SigninActivity.class);
                dialogIntent.putExtra(SigninActivity.EXTRA_SERVICE_NAME, name);
                dialogIntent.putExtra(SigninActivity.EXTRA_URL, url);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(dialogIntent);
            }
        }.execute();
    }

    public static OAuth10aService getServiceFor(String name) {
        ServiceData serviceData = services.get(name.hashCode());
        if (serviceData != null) {
            return serviceData.getService();
        }
        return null;
    }

    public static OnTaskCompleteListener<String> getListenerFor(String name) {
        ServiceData serviceData = services.get(name.hashCode());
        if (serviceData != null) {
            return serviceData.getVerifierListener();
        }
        return null;

    }
}
