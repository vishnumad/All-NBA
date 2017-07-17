package com.gmail.jorgegilcavazos.ballislife.features.shared;

import com.gmail.jorgegilcavazos.ballislife.features.model.wrapper.CustomSubmission;

import net.dean.jraw.models.VoteDirection;

public interface OnSubmissionClickListener {

    void onSubmissionClick(CustomSubmission customSubmission);

    void onVoteSubmission(CustomSubmission submission, VoteDirection voteDirection);

    void onSaveSubmission(CustomSubmission submission, boolean saved);

    void onContentClick(String url);
}
