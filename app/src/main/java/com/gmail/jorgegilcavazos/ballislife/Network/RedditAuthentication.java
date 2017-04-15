package com.gmail.jorgegilcavazos.ballislife.network;

import android.content.SharedPreferences;

import com.gmail.jorgegilcavazos.ballislife.network.API.RedditService;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthHelper;

import java.net.URL;
import java.util.UUID;

import io.reactivex.Completable;
import io.reactivex.functions.Action;

/**
 * Singleton class responsible for authenticating with Reddit.
 *
 * There are two types of authentication:
 * (1) UserlessAuth: the user does not log in with a username and password. But the application
 * still needs to authenticate with Reddit to view content.
 * (2) UserAuth: the user enters a username and password. This method allows the application to view
 * content and perform actions from the logged in account (post, comment, upvote, etc..).
 *
 * Whenever UserAuth is used, a refresh token must be saved in shared preferences so that future
 * authentications attempts can use that instead of asking to the user to login again.
 */
public class RedditAuthentication {
    private static final String TAG = "RedditAuthentication";

    public static final String REDDIT_AUTH_PREFS = "RedditAuthPrefs";
    public static final String CLIENT_ID = "XDtA2eYVKp1wWA";
    public static final String REDIRECT_URL = "http://localhost/authorize_callback";
    public static final String REDDIT_TOKEN_KEY = "REDDIT_TOKEN";

    private static RedditAuthentication mInstance = null;

    private RedditClient mRedditClient;
    private RedditService redditService;

    private RedditAuthentication() {
        mRedditClient = new RedditClient(UserAgent.of("android",
                "com.gmail.jorgegilcavazos.ballislife", "v0.4.1", "Obi-Wan_Ginobili"));
        redditService = new RedditService();
    }

    public static RedditAuthentication getInstance() {
        if (mInstance == null) {
            mInstance = new RedditAuthentication();
        }
        return mInstance;
    }

    public RedditClient getRedditClient() {
        return mRedditClient;
    }

    public boolean isUserLoggedIn() {
        return mRedditClient != null && mRedditClient.isAuthenticated()
                && mRedditClient.hasActiveUserContext();
    }

    /**
     * Authenticates with user context if a refresh token is saved in shared preferences. Otherwise
     * authenticates without a user context.
     */
    public Completable authenticate(final SharedPreferences sharedPreferences) {
        if (mRedditClient.isAuthenticated()) {
            return Completable.complete();
        }

        String refreshToken = getRefreshTokenFromPrefs(sharedPreferences);
        if (refreshToken == null) {
            Credentials credentials = Credentials.userlessApp(CLIENT_ID, UUID.randomUUID());
            return redditService.userlessAuthentication(mRedditClient, credentials);
        } else {
            Credentials credentials = Credentials.installedApp(CLIENT_ID, REDIRECT_URL);
            return redditService.refreshToken(mRedditClient, credentials, refreshToken)
                    .doOnComplete(new Action() {
                        @Override
                        public void run() throws Exception {
                            saveRefreshTokenInPrefs(sharedPreferences);
                        }
                    });
        }
    }

    /**
     * Authenticates with a user context. On success, saves the refresh token to a shared
     * preferences file.
     */
    public Completable authenticateUser(String url, final SharedPreferences sharedPreferences) {
        Credentials credentials = Credentials.installedApp(CLIENT_ID, REDIRECT_URL);
        return redditService.userAuthentication(mRedditClient, credentials, url)
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        saveRefreshTokenInPrefs(sharedPreferences);
                    }
                });
    }

    /**
     * De-authenticates the user if one is logged in.
     */
    public Completable deAuthenticateUser(final SharedPreferences sharedPreferences) {
        if (!isUserLoggedIn()) {
            return Completable.complete();
        }

        clearRefreshTokenInPrefs(sharedPreferences);
        Credentials credentials = Credentials.installedApp(CLIENT_ID, REDIRECT_URL);
        return redditService.deAuthenticate(mRedditClient, credentials);
    }

    public URL getAuthorizationUrl() {
        OAuthHelper oAuthHelper = mRedditClient.getOAuthHelper();
        Credentials credentials = Credentials.installedApp(CLIENT_ID, REDIRECT_URL);
        String[] scopes = {"identity", "edit", "flair", "mysubreddits", "read", "vote",
                "submit", "subscribe", "history", "save"};
        return oAuthHelper.getAuthorizationUrl(credentials, true, true, scopes);
    }

    private String getRefreshTokenFromPrefs(SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(REDDIT_TOKEN_KEY, null);
    }

    public void saveRefreshTokenInPrefs(SharedPreferences sharedPreferences) {
        String refreshToken = mRedditClient.getOAuthData().getRefreshToken();
        if (refreshToken != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(REDDIT_TOKEN_KEY, refreshToken);
            editor.apply();
        }
    }

    public void clearRefreshTokenInPrefs(SharedPreferences sharedPreferences) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(REDDIT_TOKEN_KEY);
        editor.apply();
    }
}