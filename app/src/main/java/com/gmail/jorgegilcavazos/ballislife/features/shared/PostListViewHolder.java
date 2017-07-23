package com.gmail.jorgegilcavazos.ballislife.features.shared;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.features.model.wrapper.CustomSubmission;
import com.gmail.jorgegilcavazos.ballislife.util.Constants;
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil;
import com.gmail.jorgegilcavazos.ballislife.util.RedditUtils;
import com.squareup.picasso.Picasso;

import net.dean.jraw.models.VoteDirection;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostListViewHolder extends RecyclerView.ViewHolder {

    public @BindView(R.id.content_container) View contentContainer;
    public @BindView(R.id.text_title) TextView tvTitle;
    public @BindView(R.id.text_author) TextView tvAuthor;
    public @BindView(R.id.text_timestamp) TextView tvTimestamp;
    public @BindView(R.id.text_domain) TextView tvDomain;
    public @BindView(R.id.thumbnail_container) View thumbnailContainer;
    public @BindView(R.id.image_thumbnail) ImageView ivThumbnail;
    public @BindView(R.id.image_thumbnail_type) ImageView ivThumbnailType;
    public @BindView(R.id.button_upvote) ImageButton btnUpvote;
    public @BindView(R.id.text_points) TextView tvPoints;
    public @BindView(R.id.button_downvote) ImageButton btnDownvote;
    public @BindView(R.id.text_comments) TextView tvComments;

    public PostListViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bindData(final Context context,
                         final RedditAuthentication redditAuthentication,
                         final CustomSubmission customSubmission,
                         boolean isDisplayedInList,
                         final OnSubmissionClickListener submissionClickListener) {
        String title = customSubmission.getTitle();
        String author = customSubmission.getAuthor();
        long timestamp = customSubmission.getCreated();
        int commentCount = customSubmission.getCommentCount();
        String score = String.valueOf(customSubmission.getScore());
        String selfTextHtml = customSubmission.getSelfTextHtml();
        String domain = customSubmission.getDomain();
        String thumbnail = customSubmission.getThumbnail();
        final String url = customSubmission.getUrl();
        boolean isSelf = customSubmission.isSelfPost();
        boolean isStickied = customSubmission.isStickied();
        boolean isSaved = customSubmission.isSaved();
        VoteDirection vote = customSubmission.getVoteDirection();
        String highResThumbnail = customSubmission.getHighResThumbnail();

        // Show low res thumbnail over lower res version.
        String thumbnailToShow = thumbnail;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            tvTitle.setText(Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvTitle.setText(Html.fromHtml(title));
        }

        tvAuthor.setText(author);
        tvTimestamp.setText(DateFormatUtil.formatRedditDate(new Date(timestamp)));
        tvComments.setText(context.getString(R.string.num_comments, commentCount));
        tvPoints.setText(score);

        if (isSelf) {
            tvDomain.setText("self");
            ivThumbnail.setVisibility(View.GONE);
            ivThumbnailType.setVisibility(View.GONE);
        } else {
            tvDomain.setText(domain);
            if (thumbnailToShow != null) {
                ivThumbnail.setVisibility(View.VISIBLE);
                Picasso.with(context)
                        .load(thumbnailToShow)
                        .into(ivThumbnail);
            } else {
                ivThumbnail.setVisibility(View.VISIBLE);
            }

            ivThumbnailType.setVisibility(View.GONE);
            if (domain.equals(Constants.STREAMABLE_DOMAIN)
                    || domain.equals(Constants.YOUTUBE_DOMAIN)) {
                ivThumbnailType.setVisibility(View.VISIBLE);
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

        final PostListViewHolder holder = this;

        btnUpvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customSubmission.getVoteDirection() == VoteDirection.UPVOTE) {
                    submissionClickListener.onVoteSubmission(customSubmission,
                            VoteDirection.NO_VOTE);
                    if (redditAuthentication.isUserLoggedIn()) {
                        customSubmission.setVoteDirection(VoteDirection.NO_VOTE);
                        RedditUtils.setNoVoteColors(context, holder);
                    }
                } else if (customSubmission.getVoteDirection() == VoteDirection.DOWNVOTE) {
                    submissionClickListener.onVoteSubmission(customSubmission,
                            VoteDirection.UPVOTE);
                    if (redditAuthentication.isUserLoggedIn()) {
                        customSubmission.setVoteDirection(VoteDirection.UPVOTE);
                        RedditUtils.setUpvotedColors(context, holder);
                    }
                } else {
                    submissionClickListener.onVoteSubmission(customSubmission,
                            VoteDirection.UPVOTE);
                    if (redditAuthentication.isUserLoggedIn()) {
                        customSubmission.setVoteDirection(VoteDirection.UPVOTE);
                        RedditUtils.setUpvotedColors(context, holder);
                    }
                }
            }
        });

        btnDownvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customSubmission.getVoteDirection() == VoteDirection.DOWNVOTE) {
                    submissionClickListener.onVoteSubmission(customSubmission,
                            VoteDirection.NO_VOTE);
                    if (redditAuthentication.isUserLoggedIn()) {
                        customSubmission.setVoteDirection(VoteDirection.NO_VOTE);
                        RedditUtils.setNoVoteColors(context, holder);
                    }
                } else if (customSubmission.getVoteDirection() == VoteDirection.UPVOTE){
                    submissionClickListener.onVoteSubmission(customSubmission,
                            VoteDirection.DOWNVOTE);
                    if (redditAuthentication.isUserLoggedIn()) {
                        customSubmission.setVoteDirection(VoteDirection.DOWNVOTE);
                        RedditUtils.setDownvotedColors(context, holder);
                    }
                } else {
                    submissionClickListener.onVoteSubmission(customSubmission,
                            VoteDirection.DOWNVOTE);
                    if (redditAuthentication.isUserLoggedIn()) {
                        customSubmission.setVoteDirection(VoteDirection.DOWNVOTE);
                        RedditUtils.setDownvotedColors(context, holder);
                    }
                }
            }
        });

        thumbnailContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submissionClickListener.onContentClick(url);
            }
        });

        if (isDisplayedInList) {
            // Enable buttons to navigate to SubmissionActivity.
            contentContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    submissionClickListener.onSubmissionClick(customSubmission);
                }
            });
        }

    }
}
