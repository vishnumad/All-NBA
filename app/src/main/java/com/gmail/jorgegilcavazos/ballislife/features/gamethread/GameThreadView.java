package com.gmail.jorgegilcavazos.ballislife.features.gamethread;

import com.hannesdorfmann.mosby.mvp.MvpView;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;

import java.util.List;

public interface GameThreadView extends MvpView {

    void setLoadingIndicator(boolean active);

    void showComments(List<CommentNode> comments);

    void hideComments();

    void addComment(int position, CommentNode comment);

    void hideText();

    void showNoThreadText();

    void showNoCommentsText();

    void showFailedToLoadCommentsText();

    void showReplySavedToast();

    void showReplyErrorToast();

    void showSavedToast();

    void showFailedToSaveToast();

    void showNotLoggedInToast();

    void showReplyToSubmissionSavedToast();

    void showReplyToSubmissionFailedToast();

    void showSavingToast();
}
