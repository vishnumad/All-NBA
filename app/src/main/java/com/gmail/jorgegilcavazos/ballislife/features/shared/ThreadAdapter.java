package com.gmail.jorgegilcavazos.ballislife.features.shared;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.features.model.wrapper.CustomSubmission;
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil;
import com.gmail.jorgegilcavazos.ballislife.util.RedditUtils;
import com.squareup.picasso.Picasso;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter used to hold all of the comments from a thread. It also supports loading a header view
 * as the first element of the recycler view.
 * The (optional) header is leaded based on the hasHeader field of the constructor and when true
 * loads a {@link FullCardViewHolder} with information about the submission.
 *
 * Currently used to display comments only in the
 * {@link com.gmail.jorgegilcavazos.ballislife.features.gamethread.GameThreadFragment} and to
 * display comments AND a the card for a full submission in the
 * {@link com.gmail.jorgegilcavazos.ballislife.features.submission.SubmissionActivity}.
 */
public class ThreadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int HEADER = 0;
    private static final int CONTENT = 1;

    private Context context;
    private List<CommentNode> commentsList;
    private boolean hasHeader;
    private OnCommentClickListener commentClickListener;
    private OnSubmissionClickListener submissionClickListener;

    private CustomSubmission customSubmission;

    public ThreadAdapter(List<CommentNode> commentsList, boolean hasHeader,
                         OnCommentClickListener commentClickListener) {
        this.commentsList = commentsList;
        this.hasHeader = hasHeader;
        this.commentClickListener = commentClickListener;
    }

    public ThreadAdapter(List<CommentNode> commentsList, boolean hasHeader,
                         OnCommentClickListener commentClickListener,
                         OnSubmissionClickListener submissionClickListener,
                         CustomSubmission customSubmission) {
        this.commentsList = commentsList;
        this.hasHeader = hasHeader;
        this.commentClickListener = commentClickListener;
        this.submissionClickListener = submissionClickListener;
        this.customSubmission = customSubmission;
    }

    @Override
    public int getItemViewType(int position) {
        if (!hasHeader) {
            return CONTENT;
        }

        if (position == 0) {
            return HEADER;
        }
        return CONTENT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view;

        if (viewType == HEADER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_layout_large,
                    parent, false);
            return new FullCardViewHolder(view);
        }

        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_layout,
                parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof FullCardViewHolder) {
            final FullCardViewHolder cardViewHolder = (FullCardViewHolder) holder;

            cardViewHolder.tvTitle.setText(Html.fromHtml(customSubmission.getTitle()));
            cardViewHolder.tvAuthor.setText(customSubmission.getAuthor());
            cardViewHolder.tvTimestamp.setText(customSubmission.getTimestamp());
            cardViewHolder.tvComments.setText(String.valueOf(customSubmission.getCommentCount()));
            cardViewHolder.tvPoints.setText(String.valueOf(customSubmission.getScore()));

            if (customSubmission.isSelfPost()) {
                cardViewHolder.tvBody.setVisibility(View.VISIBLE);
                cardViewHolder.tvBody.setText(RedditUtils.bindSnuDown(customSubmission.getSelfTextHtml()));
                cardViewHolder.tvDomain.setText("self");
                cardViewHolder.ivThumbnail.setVisibility(View.GONE);
                cardViewHolder.containerLink.setVisibility(View.GONE);
            } else {
                cardViewHolder.tvBody.setVisibility(View.GONE);
                String domain = customSubmission.getDomain();
                cardViewHolder.tvDomain.setText(domain);
                if (customSubmission.getThumbnail() != null) {
                    cardViewHolder.ivThumbnail.setVisibility(View.VISIBLE);
                    cardViewHolder.containerLink.setVisibility(View.GONE);
                    Picasso.with(context)
                            .load(customSubmission.getThumbnail())
                            .into(cardViewHolder.ivThumbnail);
                } else {
                    cardViewHolder.ivThumbnail.setVisibility(View.GONE);
                    cardViewHolder.containerLink.setVisibility(View.VISIBLE);
                    cardViewHolder.tvDomainLink.setText(customSubmission.getDomain());
                    cardViewHolder.tvLink.setText(customSubmission.getUrl());
                }
            }

            cardViewHolder.btnUpvote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (customSubmission.getVoteDirection() == VoteDirection.UPVOTE) {
                        submissionClickListener.onVoteSubmission(customSubmission.getSubmission(),
                                VoteDirection.NO_VOTE);
                        customSubmission.setVoteDirection(VoteDirection.NO_VOTE);
                        RedditUtils.setNoVoteColors(context, cardViewHolder);
                        cardViewHolder.tvPoints.setText(String.valueOf(Integer.valueOf(
                                cardViewHolder.tvPoints.getText().toString()) - 1));
                    } else if (customSubmission.getVoteDirection() == VoteDirection.DOWNVOTE) {
                        submissionClickListener.onVoteSubmission(customSubmission.getSubmission(),
                                VoteDirection.UPVOTE);
                        customSubmission.setVoteDirection(VoteDirection.UPVOTE);
                        RedditUtils.setUpvotedColors(context, cardViewHolder);
                        cardViewHolder.tvPoints.setText(String.valueOf(Integer.valueOf(
                                cardViewHolder.tvPoints.getText().toString()) + 2));
                    } else {
                        submissionClickListener.onVoteSubmission(customSubmission.getSubmission(),
                                VoteDirection.UPVOTE);
                        customSubmission.setVoteDirection(VoteDirection.UPVOTE);
                        RedditUtils.setUpvotedColors(context, cardViewHolder);
                        cardViewHolder.tvPoints.setText(String.valueOf(Integer.valueOf(
                                cardViewHolder.tvPoints.getText().toString()) + 1));
                    }
                }
            });

            cardViewHolder.btnDownvote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (customSubmission.getVoteDirection() == VoteDirection.DOWNVOTE) {
                        submissionClickListener.onVoteSubmission(customSubmission.getSubmission(),
                                VoteDirection.NO_VOTE);
                        customSubmission.setVoteDirection(VoteDirection.NO_VOTE);
                        RedditUtils.setNoVoteColors(context, cardViewHolder);
                        cardViewHolder.tvPoints.setText(String.valueOf(Integer.valueOf(
                                cardViewHolder.tvPoints.getText().toString()) + 1));
                    } else if (customSubmission.getVoteDirection() == VoteDirection.UPVOTE){
                        submissionClickListener.onVoteSubmission(customSubmission.getSubmission(),
                                VoteDirection.DOWNVOTE);
                        customSubmission.setVoteDirection(VoteDirection.DOWNVOTE);
                        RedditUtils.setDownvotedColors(context, cardViewHolder);
                        cardViewHolder.tvPoints.setText(String.valueOf(Integer.valueOf(
                                cardViewHolder.tvPoints.getText().toString()) - 2));
                    } else {
                        submissionClickListener.onVoteSubmission(customSubmission.getSubmission(),
                                VoteDirection.DOWNVOTE);
                        customSubmission.setVoteDirection(VoteDirection.DOWNVOTE);
                        RedditUtils.setDownvotedColors(context, cardViewHolder);
                        cardViewHolder.tvPoints.setText(String.valueOf(Integer.valueOf(
                                cardViewHolder.tvPoints.getText().toString()) - 1));
                    }
                }
            });

            cardViewHolder.btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (customSubmission.isSaved()) {
                        submissionClickListener.onSaveSubmission(customSubmission.getSubmission(),
                                false);
                        RedditUtils.setUnsavedColors(context, cardViewHolder);
                        customSubmission.setSaved(false);
                    } else {
                        submissionClickListener.onSaveSubmission(customSubmission.getSubmission(),
                                true);
                        RedditUtils.setSavedColors(context, cardViewHolder);
                        customSubmission.setSaved(true);
                    }
                }
            });

            cardViewHolder.ivThumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    submissionClickListener.onContentClick(customSubmission.getUrl());
                }
            });

            cardViewHolder.containerLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    submissionClickListener.onContentClick(customSubmission.getUrl());
                }
            });

        } else {
            final CommentViewHolder commentHolder = (CommentViewHolder) holder;

            final CommentNode commentNode;
            if (hasHeader) {
                commentNode = commentsList.get(position - 1);
            } else {
                commentNode = commentsList.get(position);
            }

            final Comment comment = commentNode.getComment();
            String author = comment.getAuthor();
            CharSequence body = RedditUtils.bindSnuDown(comment.data("body_html"));
            String timestamp = DateFormatUtil.formatRedditDate(comment.getCreated());
            String score = String.valueOf(comment.getScore());
            String flair = RedditUtils.parseNbaFlair(String.valueOf(comment.getAuthorFlair()));

            commentHolder.authorTextView.setText(author);
            commentHolder.bodyTextView.setText(body);
            commentHolder.timestampTextView.setText(timestamp);
            commentHolder.scoreTextView.setText(context.getString(R.string.points, score));
            commentHolder.flairTextView.setText(flair);
            commentHolder.rlCommentActions.setVisibility(View.GONE);
            setBackgroundAndPadding(commentNode, commentHolder, false /* dark */);


            // On comment click hide/show actions (upvote, downvote, save, etc...).
            commentHolder.mCommentInnerRelLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (commentHolder.rlCommentActions.getVisibility() == View.VISIBLE) {
                        hideActions(commentHolder, commentNode);
                    } else {
                        showActions(commentHolder, commentNode);
                    }
                }
            });

            final int colorUpvoted = ContextCompat.getColor(context, R.color.commentUpvoted);
            final int colorDownvoted = ContextCompat.getColor(context, R.color.commentDownvoted);
            final int colorNeutral = ContextCompat.getColor(context, R.color.commentNeutral);

            // Set score color base on vote.
            if (comment.getVote() == VoteDirection.UPVOTE) {
                commentHolder.scoreTextView.setTextColor(colorUpvoted);
            } else if (comment.getVote() == VoteDirection.DOWNVOTE) {
                commentHolder.scoreTextView.setTextColor(colorDownvoted);
            } else {
                commentHolder.scoreTextView.setTextColor(colorNeutral);
            }

            // Add a "*" to indicate that a comment has been edited.
            if (comment.hasBeenEdited()) {
                commentHolder.timestampTextView.setText(timestamp + "*");
            }

            commentHolder.btnUpvote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (commentHolder.scoreTextView.getCurrentTextColor() == colorUpvoted) {
                        commentHolder.scoreTextView.setTextColor(colorNeutral);
                        commentClickListener.onVoteComment(comment, VoteDirection.NO_VOTE);
                    } else {
                        commentHolder.scoreTextView.setTextColor(colorUpvoted);
                        commentClickListener.onVoteComment(comment, VoteDirection.UPVOTE);
                    }
                    hideActions(commentHolder, commentNode);
                }
            });
            commentHolder.btnDownvote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (commentHolder.scoreTextView.getCurrentTextColor() == colorDownvoted) {
                        commentHolder.scoreTextView.setTextColor(colorNeutral);
                        commentClickListener.onVoteComment(comment, VoteDirection.NO_VOTE);
                    } else {
                        commentHolder.scoreTextView.setTextColor(colorDownvoted);
                        commentClickListener.onVoteComment(comment, VoteDirection.DOWNVOTE);
                    }
                    hideActions(commentHolder, commentNode);
                }
            });
            commentHolder.btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    commentClickListener.onSaveComment(comment);
                    hideActions(commentHolder, commentNode);
                }
            });
            commentHolder.btnReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    commentClickListener.onReplyToComment(position, comment);
                    hideActions(commentHolder, commentNode);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (hasHeader) {
            return null != commentsList ? commentsList.size() + 1 : 1;
        } else {
            return null != commentsList ? commentsList.size() : 0;
        }
    }

    public void swap(List<CommentNode> data) {
        commentsList.clear();
        commentsList.addAll(data);
        notifyDataSetChanged();
    }

    public void addComment(int position, CommentNode comment) {
        commentsList.add(position, comment);
        notifyDataSetChanged();
    }

    public void setSubmission(Submission submission) {
        customSubmission.setSubmission(submission);
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
