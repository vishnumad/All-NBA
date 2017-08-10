package com.gmail.jorgegilcavazos.ballislife.features.common;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.VoteDirection;

public interface OnCommentClickListener {

    void onVoteComment(Comment comment, VoteDirection voteDirection);

    void onSaveComment(Comment comment);

    void onUnsaveComment(Comment comment);

    void onReplyToComment(int position, Comment parentComment);
}
