package com.gmail.jorgegilcavazos.ballislife.data.reddit;

import android.content.SharedPreferences;
import android.util.Log;

import com.gmail.jorgegilcavazos.ballislife.data.local.LocalRepository;
import com.gmail.jorgegilcavazos.ballislife.data.service.RedditService;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthHelper;

import java.net.URL;
import java.util.Date;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Single;
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
@Singleton
public class RedditAuthenticationImpl implements RedditAuthentication {
    public static final String REDDIT_AUTH_PREFS = "RedditAuthPrefs";
    public static final String CLIENT_ID = "XDtA2eYVKp1wWA";
    public static final String REDIRECT_URL = "http://localhost/authorize_callback";
    public static final String REDDIT_TOKEN_KEY = "REDDIT_TOKEN";
    public static final String TOKEN_EXPIRATION_KEY = "TOKEN_EXPIRATION";
    private static final String TAG = "RedditAuthImpl";
    private LocalRepository localRepository;
    private RedditClient mRedditClient;
    private RedditService redditService;

    @Inject
    public RedditAuthenticationImpl(LocalRepository localRepository, RedditService redditService) {
        this.localRepository = localRepository;
        this.redditService = redditService;

        mRedditClient = new RedditClient(UserAgent.of("android",
                "com.gmail.jorgegilcavazos.ballislife", "v0.5.3", "Obi-Wan_Ginobili"));
    }

    @Override
    public boolean isUserLoggedIn() {
        return mRedditClient != null && mRedditClient.isAuthenticated()
                && mRedditClient.getOAuthData().getExpirationDate().after(new Date())
                && mRedditClient.hasActiveUserContext();
    }

    @Override
    public Single<Boolean> checkUserLoggedIn() {
        return Single.just(isUserLoggedIn());
    }

    @Override
    public RedditClient getRedditClient() {
        return mRedditClient;
    }

    /**
     * Authenticates with user context if a refresh token is saved in shared preferences. Otherwise
     * authenticates without a user context.
     */
    @Override
    public Completable authenticate(final SharedPreferences sharedPreferences) {
        if (mRedditClient.isAuthenticated() && isTokenValid(sharedPreferences)) {
            Log.d(TAG, "Is authenticated and token is valid");
            return Completable.complete();
        }

        String refreshToken = getRefreshTokenFromPrefs(sharedPreferences);
        if (refreshToken == null) {
            Log.d(TAG, "Starting userless auth");
            Credentials credentials = Credentials.userlessApp(CLIENT_ID, UUID.randomUUID());
            return redditService.userlessAuthentication(mRedditClient, credentials)
                    .doOnComplete(new Action() {
                        @Override
                        public void run() throws Exception {
                            Log.d(TAG, "Finished userless auth, saving expiration token date");
                            saveTokenExpirationInPrefs(sharedPreferences, mRedditClient
                                    .getOAuthData().data("expires_in", Integer.class) * 1000);
                        }
                    });
        } else {
            Log.d(TAG, "Starting refreshing token");
            Credentials credentials = Credentials.installedApp(CLIENT_ID, REDIRECT_URL);
            return redditService.refreshToken(mRedditClient, credentials, refreshToken)
                    .doOnComplete(new Action() {
                        @Override
                        public void run() throws Exception {
                            Log.d(TAG, "Finished refreshing token, saving token and expiration");
                            saveRefreshTokenInPrefs(sharedPreferences);
                            saveTokenExpirationInPrefs(sharedPreferences, mRedditClient
                                    .getOAuthData().data("expires_in", Integer.class) * 1000);
                            localRepository.saveUsername(mRedditClient.getAuthenticatedUser());
                        }
                    });
        }
    }

    /**
     * Authenticates with a user context. On success, saves the refresh token to a shared
     * preferences file.
     */
    @Override
    public Completable authenticateUser(String url, final SharedPreferences sharedPreferences) {
        Log.d(TAG, "Starting user auth");
        Credentials credentials = Credentials.installedApp(CLIENT_ID, REDIRECT_URL);
        return redditService.userAuthentication(mRedditClient, credentials, url)
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d(TAG, "Finished user auth, saving refresh token and expiration");
                        saveRefreshTokenInPrefs(sharedPreferences);
                        saveTokenExpirationInPrefs(sharedPreferences, mRedditClient
                                .getOAuthData().data("expires_in", Integer.class) * 1000);
                        localRepository.saveUsername(mRedditClient.getAuthenticatedUser());
                    }
                });
    }

    /**
     * De-authenticates the user if one is logged in.
     */
    @Override
    public Completable deAuthenticateUser(final SharedPreferences sharedPreferences) {
        if (!isUserLoggedIn()) {
            return Completable.complete();
        }

        clearRefreshTokenInPrefs(sharedPreferences);
        clearTokenExpirationInPrefs(sharedPreferences);
        localRepository.saveUsername(null);
        Credentials credentials = Credentials.installedApp(CLIENT_ID, REDIRECT_URL);
        return redditService.deAuthenticate(mRedditClient, credentials);
    }

    @Override
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

    private boolean isTokenValid(SharedPreferences sharedPreferences) {
        long expirationLong = getTokenExpirationFromPrefs(sharedPreferences);
        if (expirationLong == -1) {
            return false;
        }
        return new Date(expirationLong).after(new Date());
    }

    private long getTokenExpirationFromPrefs(SharedPreferences sharedPreferences) {
        return sharedPreferences.getLong(TOKEN_EXPIRATION_KEY, -1);
    }

    private void saveTokenExpirationInPrefs(SharedPreferences sharedPreferences, long duration) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(TOKEN_EXPIRATION_KEY, new Date().getTime() + duration);
        editor.apply();
    }

    private void clearTokenExpirationInPrefs(SharedPreferences sharedPreferences) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(TOKEN_EXPIRATION_KEY);
        editor.apply();
    }
}