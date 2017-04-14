package com.gmail.jorgegilcavazos.ballislife.features.posts;

import com.gmail.jorgegilcavazos.ballislife.features.model.SubscriberCount;
import com.gmail.jorgegilcavazos.ballislife.features.model.wrapper.CustomSubmission;

import java.util.List;

public interface PostsView {

    void setLoadingIndicator(boolean active);

    void showPosts(List<CustomSubmission> submissions);

    void hidePosts();

    void showPostsLoadingFailedSnackbar();

    void dismissSnackbar();

    void showNotAuthenticatedToast();

    void showNotLoggedInToast();

    void showSubscribers(SubscriberCount subscriberCount);

}
