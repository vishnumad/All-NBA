package com.gmail.jorgegilcavazos.ballislife.features.login;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.features.application.BallIsLifeApplication;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider;

import java.net.URL;

import javax.inject.Inject;

import butterknife.BindView;
import io.reactivex.observers.DisposableCompletableObserver;

import static com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthenticationImpl
        .REDDIT_AUTH_PREFS;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    @Inject
    RedditAuthentication redditAuthentication;

    @Inject
    BaseSchedulerProvider schedulerProvider;

    @BindView(R.id.login_webview) WebView webView;

    private int origin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BallIsLifeApplication.getAppComponent().inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle("Log in to Reddit");

        final SharedPreferences preferences = getSharedPreferences(REDDIT_AUTH_PREFS, MODE_PRIVATE);

        URL authURL = redditAuthentication.getAuthorizationUrl();
        webView = (WebView) findViewById(R.id.login_webview);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (url.contains("code=")) {
                    webView.stopLoading();
                    redditAuthentication.authenticateUser(url, preferences)
                            .subscribeOn(schedulerProvider.io())
                            .observeOn(schedulerProvider.ui())
                            .subscribeWith(new DisposableCompletableObserver() {
                                @Override
                                public void onComplete() {
                                    finish();
                                }

                                @Override
                                public void onError(Throwable e) {
                                    finish();
                                }
                            });
                    //TODO: show spinner
                }
            }
        });
        webView.loadUrl(authURL.toExternalForm());
    }

    @Override
    protected void onDestroy() {
        webView.destroy();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
