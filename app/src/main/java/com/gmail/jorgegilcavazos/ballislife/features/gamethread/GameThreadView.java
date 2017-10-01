package com.gmail.jorgegilcavazos.ballislife.features.gamethread;

import com.gmail.jorgegilcavazos.ballislife.features.model.ThreadItem;
import com.hannesdorfmann.mosby.mvp.MvpView;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;

import java.util.List;

public interface GameThreadView extends MvpView {

    void setLoadingIndicator(boolean active);

    void showComments(List<ThreadItem> comments);

    void hideComments();

    void addComment(int position, CommentNode comment);

    void hideText();

    void showNoThreadText();

    void showNoCommentsText();

    void showFailedToLoadCommentsText();

    void showReplySavedToast();

    void showReplyErrorToast();

    void showSavedToast();

    void showNotLoggedInToast();

    void showReplyToSubmissionFailedToast();

    void showSavingToast();

    void openReplyToCommentActivity(int position, Comment parentComment);

    void openReplyToSubmissionActivity();

    void setSubmissionId(String submissionId);
}
