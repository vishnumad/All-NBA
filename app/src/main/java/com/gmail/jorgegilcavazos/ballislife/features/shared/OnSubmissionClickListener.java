package com.gmail.jorgegilcavazos.ballislife.features.shared;

import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

public interface OnSubmissionClickListener {

    void onSubmissionClick(Submission submission);

    void onVoteSubmission(Submission submission, VoteDirection voteDirection);

    void onSaveSubmission(Submission submission, boolean saved);
}
