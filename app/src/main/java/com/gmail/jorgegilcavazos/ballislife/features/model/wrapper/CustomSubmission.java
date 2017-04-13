package com.gmail.jorgegilcavazos.ballislife.features.model.wrapper;

import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

public class CustomSubmission {

    private Submission submission;
    private VoteDirection voteDirection;
    private boolean saved;

    public CustomSubmission(Submission submission, VoteDirection voteDirection, boolean saved) {
        this.submission = submission;
        this.voteDirection = voteDirection;
        this.saved = saved;
    }

    public Submission getSubmission() {
        return submission;
    }

    public void setSubmission(Submission submission) {
        this.submission = submission;
    }

    public VoteDirection getVoteDirection() {
        return voteDirection;
    }

    public void setVoteDirection(VoteDirection voteDirection) {
        this.voteDirection = voteDirection;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }
}
