package com.gmail.jorgegilcavazos.ballislife.features.shared;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil;
import com.gmail.jorgegilcavazos.ballislife.util.RedditUtils;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.VoteDirection;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter used to hold all of the comments from a thread.
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    Context context;
    List<CommentNode> commentsList;
    OnCommentActionClickListener actionClickListener;

    public CommentAdapter(List<CommentNode> commentsList, OnCommentActionClickListener listener) {
        this.commentsList = commentsList;
        actionClickListener = listener;
    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_layout,
                parent, false);

        context = parent.getContext();
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CommentViewHolder holder, final int position) {
        final CommentNode commentNode = commentsList.get(position);

        final Comment comment = commentNode.getComment();
        String author = comment.getAuthor();
        String body = comment.getBody();
        String timestamp = DateFormatUtil.formatRedditDate(comment.getCreated());
        String score = String.valueOf(comment.getScore());
        String flair = RedditUtils.parseNbaFlair(String.valueOf(comment.getAuthorFlair()));

        holder.authorTextView.setText(author);
        holder.bodyTextView.setText(body);
        holder.timestampTextView.setText(timestamp);
        holder.scoreTextView.setText(context.getString(R.string.points, score));
        holder.flairTextView.setText(flair);
        holder.rlCommentActions.setVisibility(View.GONE);
        setBackgroundAndPadding(commentNode, holder, false /* dark */);


        // On comment click hide/show actions (upvote, downvote, save, etc...).
        holder.mCommentInnerRelLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.rlCommentActions.getVisibility() == View.VISIBLE) {
                    hideActions(holder, commentNode);
                } else {
                    showActions(holder, commentNode);
                }
            }
        });

        final int colorUpvoted = ContextCompat.getColor(context, R.color.commentUpvoted);
        final int colorDownvoted = ContextCompat.getColor(context, R.color.commentDownvoted);
        final int colorNeutral = ContextCompat.getColor(context, R.color.commentNeutral);

        // Set score color base on vote.
        if (comment.getVote() == VoteDirection.UPVOTE) {
            holder.scoreTextView.setTextColor(colorUpvoted);
        } else if (comment.getVote() == VoteDirection.DOWNVOTE) {
            holder.scoreTextView.setTextColor(colorDownvoted);
        } else {
            holder.scoreTextView.setTextColor(colorNeutral);
        }

        // Add a "*" to indicate that a comment has been edited.
        if (comment.hasBeenEdited()) {
            holder.timestampTextView.setText(timestamp + "*");
        }

        holder.btnUpvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.scoreTextView.getCurrentTextColor() == colorUpvoted) {
                    holder.scoreTextView.setTextColor(colorNeutral);
                    actionClickListener.onVote(comment, VoteDirection.NO_VOTE);
                } else {
                    holder.scoreTextView.setTextColor(colorUpvoted);
                    actionClickListener.onVote(comment, VoteDirection.UPVOTE);
                }
                hideActions(holder, commentNode);
            }
        });
        holder.btnDownvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.scoreTextView.getCurrentTextColor() == colorDownvoted) {
                    holder.scoreTextView.setTextColor(colorNeutral);
                    actionClickListener.onVote(comment, VoteDirection.NO_VOTE);
                } else {
                    holder.scoreTextView.setTextColor(colorDownvoted);
                    actionClickListener.onVote(comment, VoteDirection.DOWNVOTE);
                }
                hideActions(holder, commentNode);
            }
        });
        holder.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionClickListener.onSave(comment);
                hideActions(holder, commentNode);
            }
        });
        holder.btnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionClickListener.onReply(position, comment);
                hideActions(holder, commentNode);
            }
        });

    }

    @Override
    public int getItemCount() {
        return null != commentsList ? commentsList.size() : 0;
    }

    public void swap(List<CommentNode> data) {
        commentsList.clear();
        commentsList.addAll(data);
        notifyDataSetChanged();
    }

    private void setBackgroundAndPadding(CommentNode commentNode, CommentViewHolder holder,
                                         boolean dark) {
        int padding_in_dp = 5;
        final float scale = context.getResources().getDisplayMetrics().density;
        int padding_in_px = (int) (padding_in_dp * scale + 0.5F);

        int depth = commentNode.getDepth(); // From 1

        // Add color if it is not a top-level comment.
        if (depth > 1) {
            int depthFromZero = depth - 2;
            int res = (depthFromZero) % 5;
            switch (res) {
                case 0:
                    if (dark) {
                        holder.mCommentInnerRelLayout.setBackgroundResource(R.drawable.borderbluedark);
                    } else {
                        holder.mCommentInnerRelLayout.setBackgroundResource(R.drawable.borderblue);
                    }
                    break;
                case 1:
                    if (dark) {
                        holder.mCommentInnerRelLayout.setBackgroundResource(R.drawable.bordergreendark);
                    } else {
                        holder.mCommentInnerRelLayout.setBackgroundResource(R.drawable.bordergreen);
                    }
                    break;
                case 2:
                    if (dark) {
                        holder.mCommentInnerRelLayout.setBackgroundResource(R.drawable.borderbrowndark);
                    } else {
                        holder.mCommentInnerRelLayout.setBackgroundResource(R.drawable.borderbrown);
                    }
                    break;
                case 3:
                    if (dark) {
                        holder.mCommentInnerRelLayout.setBackgroundResource(R.drawable.borderorangedark);
                    } else {
                        holder.mCommentInnerRelLayout.setBackgroundResource(R.drawable.borderorange);
                    }
                    break;
                case 4:
                    if (dark) {
                        holder.mCommentInnerRelLayout.setBackgroundResource(R.drawable.borderreddark);
                    } else {
                        holder.mCommentInnerRelLayout.setBackgroundResource(R.drawable.borderred);
                    }
                    break;
            }
        } else {
            if (dark) {
                holder.mCommentInnerRelLayout.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.commentBgDark));
            } else {
                holder.mCommentInnerRelLayout.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.commentBgLight));
            }
        }
        // Add padding depending on level.
        holder.mCommentOuterRelLayout.setPadding(padding_in_px * (depth - 2), 0, 0, 0);
    }

    private void hideActions(CommentViewHolder holder, CommentNode commentNode) {
        holder.mCommentOuterRelLayout.setBackgroundColor(
                ContextCompat.getColor(context, R.color.white));
        holder.rlCommentActions.setVisibility(View.GONE);
        setBackgroundAndPadding(commentNode, holder, false /* dark */);
    }

    private void showActions(CommentViewHolder holder, CommentNode commentNode) {
        holder.rlCommentActions.setVisibility(View.VISIBLE);
        holder.mCommentOuterRelLayout.setBackgroundColor(
                ContextCompat.getColor(context, R.color.lightGray));
        setBackgroundAndPadding(commentNode, holder, true /* dark */);
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.rl_comment_outer) RelativeLayout mCommentOuterRelLayout;
        @BindView(R.id.comment_inner_relativeLayout) RelativeLayout mCommentInnerRelLayout;
        @BindView(R.id.comment_author) TextView authorTextView;
        @BindView(R.id.comment_score) TextView scoreTextView;
        @BindView(R.id.comment_timestamp) TextView timestampTextView;
        @BindView(R.id.comment_body) TextView bodyTextView;
        @BindView(R.id.comment_flair) TextView flairTextView;
        @BindView(R.id.layout_comment_actions) LinearLayout rlCommentActions;
        @BindView(R.id.button_comment_upvote) ImageButton btnUpvote;
        @BindView(R.id.button_comment_downvote) ImageButton btnDownvote;
        @BindView(R.id.button_comment_save) ImageButton btnSave;
        @BindView(R.id.button_comment_reply) ImageButton btnReply;
        @BindView(R.id.button_comment_options) ImageButton btnOptions;

        public CommentViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
