package com.gmail.jorgegilcavazos.ballislife.features.shared;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.VoteDirection;

public interface OnCommentActionClickListener {

    void onVote(Comment comment, VoteDirection voteDirection);

}
