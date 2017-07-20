package com.gmail.jorgegilcavazos.ballislife.features.profile;

import com.hannesdorfmann.mosby.mvp.MvpView;

import net.dean.jraw.models.Contribution;

import java.util.List;


public interface ProfileView extends MvpView {

    void setLoadingIndicator(boolean active);

    void showContent(List<Contribution> contentList, boolean clear);

    void hideContent();

    void openSubmission(String submissionId);

    void openSubmissionAndScrollToComment(String submissionId, String commentId);

    void dismissSnackbar();

    void scrollToTop();

    void resetScrollingState();

    void showNotAuthenticatedToast();

    void showNothingToShowSnackbar();

    void showContributionsLoadingFailedSnackbar(boolean reset);

    void showUnknownErrorToast(Throwable e);
}
