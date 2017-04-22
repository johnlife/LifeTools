package ru.johnlife.lifetools.rest.oauth.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.scribejava.core.oauth.OAuth10aService;

import ru.johnlife.lifetools.rest.R;
import ru.johnlife.lifetools.rest.oauth.SignIn;
import ru.johnlife.lifetools.rest.task.OnTaskCompleteListener;

/**
 * Created by Yan Yurkin
 * 29 October 2016
 */

public class SigninActivity extends Activity {
    public static final String EXTRA_SERVICE_NAME = "service.name";
    public static final String EXTRA_URL = "url";
    private WebView webView;
    private View progress;
    private OAuth10aService service;
    private String serviceName;
    private boolean success = false;
    private WebViewClient webViewClient = new WebViewClient() {
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            progress.setVisibility(View.VISIBLE);
            Log.i(SigninActivity.class.getSimpleName(), url);
            if (url.contains(service.getConfig().getCallback())) {
                webView.stopLoading();
                Uri uri = Uri.parse(url);
                String[] params = uri.getQuery().split("&");
                for (String param : params) {
                    String[] pair = param.split("=");
                    String name = pair[0];
                    String value = pair[1];
                    Log.i(SigninActivity.class.getSimpleName(), param);
                    if ("oauth_verifier".equals(name)) {
                        OnTaskCompleteListener<String> listener = SignIn.getListenerFor(serviceName);
                        if (listener != null) {
                            listener.success(value);
                            success = true;
                        }
                        finish();
                    }
                }
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progress.setVisibility(View.GONE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serviceName = getIntent().getStringExtra(EXTRA_SERVICE_NAME);
        String url = getIntent().getStringExtra(EXTRA_URL);
        if (abortIfNull(serviceName) || abortIfNull(url)) return;
        service = SignIn.getServiceFor(serviceName);
        if (abortIfNull(service)) return;
        setContentView(R.layout.activity_login);
        DisplayMetrics m = getResources().getDisplayMetrics();
        progress = findViewById(R.id.progress);
        progress.setVisibility(View.GONE);
        webView = (WebView) findViewById(R.id.webview);
        ViewGroup.LayoutParams layout = webView.getLayoutParams();
        layout.width = m.widthPixels *8/10;
        layout.height = m.heightPixels *8/10;
        webView.requestLayout();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(webViewClient);
        webView.loadUrl(url);
    }

    /**
     *
     * @param suspect
     * @return true if aborted
     */
    private boolean abortIfNull(Object suspect) {
        if (suspect == null) {
            finish();
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webView.destroy();
        if (!success) {
            OnTaskCompleteListener<String> listener = SignIn.getListenerFor(serviceName);
            if (listener != null) {
                listener.error("Cancelled");
            }

        }
    }
}
