package com.gmail.jorgegilcavazos.ballislife.features.posts;

import com.gmail.jorgegilcavazos.ballislife.features.model.SubscriberCount;
import com.gmail.jorgegilcavazos.ballislife.features.model.wrapper.CustomSubmission;

import java.util.List;

public interface PostsView {

    void setLoadingIndicator(boolean active);

    void showPosts(List<CustomSubmission> submissions);

    void addPosts(List<CustomSubmission> submissions);

    void showPostsLoadingFailedSnackbar(int loadType);

    void dismissSnackbar();

    void showNotAuthenticatedToast();

    void showNotLoggedInToast();

    void showSubscribers(SubscriberCount subscriberCount);

    void openContentTab(String url);

    void setLoadingFailed(boolean failed);

    void showNothingToShowToast();

    void openStreamable(String shortcode);

    void showContentUnavailableToast();

    void changeViewType(int viewType);

}
