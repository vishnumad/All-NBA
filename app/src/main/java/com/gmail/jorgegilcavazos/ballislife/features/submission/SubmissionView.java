package com.gmail.jorgegilcavazos.ballislife.features.submission;

import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.Submission;

import java.util.List;

public interface SubmissionView {

    void setLoadingIndicator(boolean active);

    void showComments(List<CommentNode> commentNodes, Submission submission);

    void addComment(CommentNode comment, int position);

    void showErrorAddingComment();

    void showNotLoggedInError();

    void showSavingToast();

    void showSavedToast();

    void showErrorSavingToast();
}
