package com.gmail.jorgegilcavazos.ballislife.features.posts;

import com.gmail.jorgegilcavazos.ballislife.features.model.SubscriberCount;
import com.gmail.jorgegilcavazos.ballislife.features.model.wrapper.CustomSubmission;

import java.util.List;

public interface PostsView {

    void setLoadingIndicator(boolean active);

    void showPosts(List<CustomSubmission> submissions, boolean reset);

    void showPostsLoadingFailedSnackbar(boolean reset);

    void dismissSnackbar();

    void showNotAuthenticatedToast();

    void showNotLoggedInToast();

    void showSubscribers(SubscriberCount subscriberCount);

    void openContentTab(String url);

    void showNothingToShowToast();

    void openStreamable(String shortcode);

    void showContentUnavailableToast();

    void changeViewType(int viewType);

    void scrollToTop();

    void resetScrollState();
}
