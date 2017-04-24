package com.gmail.jorgegilcavazos.ballislife.features.shared;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.features.model.wrapper.CustomSubmission;
import com.gmail.jorgegilcavazos.ballislife.data.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil;
import com.gmail.jorgegilcavazos.ballislife.util.RedditUtils;
import com.squareup.picasso.Picasso;

import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FullCardViewHolder extends RecyclerView.ViewHolder {

    public @BindView(R.id.text_title) TextView tvTitle;
    public @BindView(R.id.text_author) TextView tvAuthor;
    public @BindView(R.id.text_timestamp) TextView tvTimestamp;
    public @BindView(R.id.text_domain) TextView tvDomain;
    public @BindView(R.id.image_thumbnail) ImageView ivThumbnail;
    public @BindView(R.id.button_upvote) ImageButton btnUpvote;
    public @BindView(R.id.text_points) TextView tvPoints;
    public @BindView(R.id.button_downvote) ImageButton btnDownvote;
    public @BindView(R.id.button_comments) ImageButton btnComments;
    public @BindView(R.id.text_comments) TextView tvComments;
    public @BindView(R.id.button_save) ImageButton btnSave;
    public @BindView(R.id.text_body) TextView tvBody;
    public @BindView(R.id.content_link) LinearLayout containerLink;
    public @BindView(R.id.text_domain_link) TextView tvDomainLink;
    public @BindView(R.id.text_link) TextView tvLink;

    public FullCardViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bindData(final Context context,
                  final CustomSubmission customSubmission,
                  boolean isDisplayedInList,
                  final OnSubmissionClickListener submissionClickListener) {

        String title, author, timestamp, commentCount, score, selfTextHtml, domain, thumbnail,
                highResThumbnail, thumbnailToShow;
        final String url;
        boolean isSelf, isStickied, isSaved;
        VoteDirection vote;

        // Get data from real submission if available, otherwise used data from fake one.
        if (customSubmission.getSubmission() == null) {
            title = customSubmission.getTitle();
            author = customSubmission.getAuthor();
            timestamp = customSubmission.getTimestamp();
            commentCount = String.valueOf(customSubmission.getCommentCount());
            score = String.valueOf(customSubmission.getScore());
            selfTextHtml = customSubmission.getSelfTextHtml();
            domain = customSubmission.getDomain();
            thumbnail = customSubmission.getThumbnail();
            url = customSubmission.getUrl();
            isSelf = customSubmission.isSelfPost();
            isStickied = customSubmission.isStickied();
            isSaved = customSubmission.isSaved();
            vote = customSubmission.getVoteDirection();
            highResThumbnail = customSubmission.getHighResThumbnail();
        } else {
            Submission submission = customSubmission.getSubmission();
            title = submission.getTitle();
            author = submission.getAuthor();
            timestamp = DateFormatUtil.formatRedditDate(submission.getCreated());
            commentCount = String.valueOf(submission.getCommentCount());
            score = String.valueOf(submission.getScore());
            selfTextHtml = submission.data("selftext_html");
            domain = submission.getDomain();
            thumbnail = submission.getThumbnail();
            url = submission.getUrl();
            isSelf = submission.isSelfPost();
            isStickied = submission.isStickied();
            isSaved = submission.isSaved();
            vote = submission.getVote();
            try {
                highResThumbnail = submission.getOEmbedMedia().getThumbnail().getUrl().toString();
            } catch (NullPointerException e) {
                highResThumbnail = null;
            }
        }

        // Show HD thumbnail over lower res version.
        if (highResThumbnail != null) {
            thumbnailToShow = highResThumbnail;
        } else {
            thumbnailToShow = thumbnail;
        }

        // Bind data to views.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            tvTitle.setText(Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvTitle.setText(Html.fromHtml(title));
        }

        tvAuthor.setText(author);
        tvTimestamp.setText(timestamp);
        tvComments.setText(commentCount);
        tvPoints.setText(score);

        if (isSelf) {
            if (!isDisplayedInList) {
                tvBody.setVisibility(View.VISIBLE);
                tvBody.setText(RedditUtils.bindSnuDown(selfTextHtml));
            } else {
                tvBody.setVisibility(View.GONE);
            }
            tvDomain.setText("self");
            ivThumbnail.setVisibility(View.GONE);
            containerLink.setVisibility(View.GONE);
        } else {
            tvBody.setVisibility(View.GONE);
            tvDomain.setText(domain);
            if (thumbnailToShow != null) {
                ivThumbnail.setVisibility(View.VISIBLE);
                containerLink.setVisibility(View.GONE);
                Picasso.with(context)
                        .load(thumbnailToShow)
                        .into(ivThumbnail);
            } else {
                ivThumbnail.setVisibility(View.GONE);
                containerLink.setVisibility(View.VISIBLE);
                tvDomainLink.setText(domain);
                tvLink.setText(url);
            }
        }

        // Set title font to green and bold if is stickied post.
        if (isStickied) {
            tvTitle.setTextColor(ContextCompat.getColor(context, R.color.stickiedColor));
            tvTitle.setTypeface(null, Typeface.BOLD);
        } else {
            tvTitle.setTextColor(ContextCompat.getColor(context, R.color.primaryText));
            tvTitle.setTypeface(null, Typeface.NORMAL);
        }

        // Set vote buttons colors if there submission has been voted on.
        if (vote == VoteDirection.UPVOTE) {
            RedditUtils.setUpvotedColors(context, this);
        } else if (vote == VoteDirection.DOWNVOTE) {
            RedditUtils.setDownvotedColors(context, this);
        } else {
            RedditUtils.setNoVoteColors(context, this);
        }

        // Set saved button color depending on whether the submission has been saved.
        if (isSaved) {
            RedditUtils.setSavedColors(context, this);
        } else {
            RedditUtils.setUnsavedColors(context, this);
        }

        final FullCardViewHolder holder = this;

        btnUpvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customSubmission.getVoteDirection() == VoteDirection.UPVOTE) {
                    submissionClickListener.onVoteSubmission(customSubmission.getSubmission(),
                            VoteDirection.NO_VOTE);
                    if (RedditAuthentication.getInstance().isUserLoggedIn()) {
                        customSubmission.setVoteDirection(VoteDirection.NO_VOTE);
                        RedditUtils.setNoVoteColors(context, holder);
                        tvPoints.setText(String.valueOf(
                                Integer.valueOf(tvPoints.getText().toString()) - 1));
                    }
                } else if (customSubmission.getVoteDirection() == VoteDirection.DOWNVOTE) {
                    submissionClickListener.onVoteSubmission(customSubmission.getSubmission(),
                            VoteDirection.UPVOTE);
                    if (RedditAuthentication.getInstance().isUserLoggedIn()) {
                        customSubmission.setVoteDirection(VoteDirection.UPVOTE);
                        RedditUtils.setUpvotedColors(context, holder);
                        holder.tvPoints.setText(String.valueOf(Integer.valueOf(
                                holder.tvPoints.getText().toString()) + 2));
                    }
                } else {
                    submissionClickListener.onVoteSubmission(customSubmission.getSubmission(),
                            VoteDirection.UPVOTE);
                    if (RedditAuthentication.getInstance().isUserLoggedIn()) {
                        customSubmission.setVoteDirection(VoteDirection.UPVOTE);
                        RedditUtils.setUpvotedColors(context, holder);
                        holder.tvPoints.setText(String.valueOf(Integer.valueOf(
                                holder.tvPoints.getText().toString()) + 1));
                    }
                }
            }
        });

        btnDownvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customSubmission.getVoteDirection() == VoteDirection.DOWNVOTE) {
                    submissionClickListener.onVoteSubmission(customSubmission.getSubmission(),
                            VoteDirection.NO_VOTE);
                    if (RedditAuthentication.getInstance().isUserLoggedIn()) {
                        customSubmission.setVoteDirection(VoteDirection.NO_VOTE);
                        RedditUtils.setNoVoteColors(context, holder);
                        holder.tvPoints.setText(String.valueOf(Integer.valueOf(
                                holder.tvPoints.getText().toString()) + 1));
                    }
                } else if (customSubmission.getVoteDirection() == VoteDirection.UPVOTE){
                    submissionClickListener.onVoteSubmission(customSubmission.getSubmission(),
                            VoteDirection.DOWNVOTE);
                    if (RedditAuthentication.getInstance().isUserLoggedIn()) {
                        customSubmission.setVoteDirection(VoteDirection.DOWNVOTE);
                        RedditUtils.setDownvotedColors(context, holder);
                        holder.tvPoints.setText(String.valueOf(Integer.valueOf(
                                holder.tvPoints.getText().toString()) - 2));
                    }
                } else {
                    submissionClickListener.onVoteSubmission(customSubmission.getSubmission(),
                            VoteDirection.DOWNVOTE);
                    if (RedditAuthentication.getInstance().isUserLoggedIn()) {
                        customSubmission.setVoteDirection(VoteDirection.DOWNVOTE);
                        RedditUtils.setDownvotedColors(context, holder);
                        holder.tvPoints.setText(String.valueOf(Integer.valueOf(
                                holder.tvPoints.getText().toString()) - 1));
                    }
                }
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customSubmission.isSaved()) {
                    submissionClickListener.onSaveSubmission(customSubmission.getSubmission(),
                            false);
                    if (RedditAuthentication.getInstance().isUserLoggedIn()) {
                        RedditUtils.setUnsavedColors(context, holder);
                        customSubmission.setSaved(false);
                    }
                } else {
                    submissionClickListener.onSaveSubmission(customSubmission.getSubmission(),
                            true);
                    if (RedditAuthentication.getInstance().isUserLoggedIn()) {
                        RedditUtils.setSavedColors(context, holder);
                        customSubmission.setSaved(true);
                    }
                }
            }
        });

        ivThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submissionClickListener.onContentClick(url);
            }
        });

        containerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submissionClickListener.onContentClick(url);
            }
        });

        if (isDisplayedInList) {
            // Enable buttons to navigate to SubmissionActivity.
            btnComments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    submissionClickListener.onSubmissionClick(customSubmission.getSubmission());
                }
            });

            tvTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    submissionClickListener.onSubmissionClick(customSubmission.getSubmission());
                }
            });
        }
    }
}
