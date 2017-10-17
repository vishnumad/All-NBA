package com.gmail.jorgegilcavazos.ballislife.data.reddit;

import net.dean.jraw.RedditClient;

import java.net.URL;

import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Manages a reddit user's authentication session.
 */
public interface RedditAuthentication {

    boolean isUserLoggedIn();

    Single<Boolean> checkUserLoggedIn();

    /**
     * Returns the reddit client used in this authenticator.
     */
    RedditClient getRedditClient();

    /**
     * Authenticates with user context if a refresh token is saved in shared preferences. Otherwise
     * authenticates without a user context.
     */
    Completable authenticate();

    /**
     * Authenticates with a user context. On success, saves the refresh token to a shared
     * preferences file.
     */
    Completable authenticateUser(String url);

    /**
     * De-authenticates the user if one is logged in.
     */
    Completable deAuthenticateUser();

    URL getAuthorizationUrl();
}
