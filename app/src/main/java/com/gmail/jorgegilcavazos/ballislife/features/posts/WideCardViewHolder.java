package com.gmail.jorgegilcavazos.ballislife.features.posts;

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
import com.gmail.jorgegilcavazos.ballislife.features.shared.OnSubmissionClickListener;
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil;
import com.gmail.jorgegilcavazos.ballislife.util.Pair;
import com.gmail.jorgegilcavazos.ballislife.util.RedditUtils;
import com.gmail.jorgegilcavazos.ballislife.util.Utilities;
import com.gmail.jorgegilcavazos.ballislife.util.Utilities.ThumbnailType;
import com.google.common.base.Optional;
import com.squareup.picasso.Picasso;

import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.subjects.PublishSubject;

/**
 * Holder that binds Reddit {@link CustomSubmission} data to it's view.
 * TODO: Refactor to user PublishSubject instead of listeners for all click actions.
 */
public class WideCardViewHolder extends RecyclerView.ViewHolder {

    public @BindView(R.id.layout_container) View layoutContainer;
    public @BindView(R.id.image_thumbnail) ImageView ivThumbnail;
    public @BindView(R.id.text_title) TextView tvTitle;
    public @BindView(R.id.layout_small_image_title) View layoutSmallImageTitle;
    public @BindView(R.id.image_thumbnail_small) ImageView ivThumbnailSmall;
    public @BindView(R.id.text_title_small) TextView tvTitleSmall;
    public @BindView(R.id.text_body) TextView tvBody;
    public @BindView(R.id.button_upvote) ImageButton btnUpvote;
    public @BindView(R.id.button_downvote) ImageButton btnDownvote;
    public @BindView(R.id.text_points) TextView tvPoints;
    public @BindView(R.id.text_comments) TextView tvComments;
    public @BindView(R.id.button_more) ImageButton btnMore;
    public @BindView(R.id.button_save) ImageButton btnSave;
    public @BindView(R.id.button_share) ImageButton btnShare;
    public @BindView(R.id.text_posted_details) TextView tvPostedDetails;

    public WideCardViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    private static String getUsableRedditDate(Context context, Date timestamp) {
        Pair<Integer, Optional<Long>> usableDate = DateFormatUtil.formatRedditDateLong(timestamp);
        switch (usableDate.first) {
            case DateFormatUtil.TIME_UNIT_JUST_NOW:
                return context.getString(R.string.just_now);
            case DateFormatUtil.TIME_UNIT_MINUTES:
                return context.getString(R.string.minutes_ago, usableDate.second.get());
            case DateFormatUtil.TIME_UNIT_HOURS:
                return context.getString(R.string.hours_ago, usableDate.second.get());
            case DateFormatUtil.TIME_UNIT_DAYS:
                return context.getString(R.string.days_ago, usableDate.second.get());
            default:
                throw new IllegalStateException("Invalid TIME UNIT: " + usableDate.first);
        }
    }

    public void bindData(final Context context,
                         RedditAuthentication redditAuthentication,
                         final CustomSubmission customSubmission,
                         final OnSubmissionClickListener submissionClickListener,
                         final PublishSubject<Submission> shareSubject) {
        String title = customSubmission.getTitle();
        String author = customSubmission.getAuthor();
        long timestamp = customSubmission.getCreated();
        int commentCount = customSubmission.getCommentCount();
        int score = customSubmission.getScore();
        String selfTextHtml = customSubmission.getSelfTextHtml();
        String domain = customSubmission.getDomain();
        String url = customSubmission.getUrl();
        boolean isSelf = customSubmission.isSelfPost();
        boolean isStickied = customSubmission.isStickied();
        boolean isSaved = customSubmission.isSaved();
        VoteDirection vote = customSubmission.getVoteDirection();

        Optional<Pair<ThumbnailType, String>> thumbnailTypeUrl = Utilities
                .getThumbnailToShowFromCustomSubmission(customSubmission);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            tvTitle.setText(Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY));
            tvTitleSmall.setText(Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvTitle.setText(Html.fromHtml(title));
            tvTitleSmall.setText(Html.fromHtml(title));
        }

        // Show / hide thumbnails and titles depending on which thumbnail is available.
        ivThumbnail.setVisibility(View.GONE);
        ivThumbnailSmall.setVisibility(View.GONE);
        tvTitle.setVisibility(View.GONE);
        tvTitleSmall.setVisibility(View.GONE);
        layoutSmallImageTitle.setVisibility(View.GONE);
        boolean shouldShowThumbnail = !isSelf && thumbnailTypeUrl.isPresent();
        if (shouldShowThumbnail) {
            ThumbnailType thumbnailType = thumbnailTypeUrl.get().first;
            String thumbnailUrl = thumbnailTypeUrl.get().second;
            switch (thumbnailType) {
                case LOW_RES:
                    Picasso.with(context).load(thumbnailUrl).into(ivThumbnailSmall);
                    ivThumbnailSmall.setVisibility(View.VISIBLE);
                    layoutSmallImageTitle.setVisibility(View.VISIBLE);
                    tvTitleSmall.setVisibility(View.VISIBLE);
                    break;
                case HIGH_RES:
                    Picasso.with(context).load(thumbnailUrl).into(ivThumbnail);
                    ivThumbnail.setVisibility(View.VISIBLE);
                    tvTitle.setVisibility(View.VISIBLE);
                    break;
                default:
                    throw new IllegalStateException("Invalid thumbnail type: " + thumbnailType);
            }
        } else {
            tvTitle.setVisibility(View.VISIBLE);
        }

        tvPoints.setText(context.getString(R.string.num_points, score));
        tvComments.setText(context.getString(R.string.num_comments, commentCount));

        tvPostedDetails.setText(context.getString(R.string.posted_details,
                getUsableRedditDate(context, new Date(timestamp)), author, domain));

        // Set title font to green and bold if is stickied post.
        if (isStickied) {
            tvTitle.setTextColor(ContextCompat.getColor(context, R.color.stickiedColor));
            tvTitle.setTypeface(null, Typeface.BOLD);
        } else {
            tvTitle.setTextColor(ContextCompat.getColor(context, R.color.primaryText));
            tvTitle.setTypeface(null, Typeface.NORMAL);
        }

        if (isSelf && selfTextHtml != null && !selfTextHtml.isEmpty()) {
            tvBody.setVisibility(View.VISIBLE);
            tvBody.setText(RedditUtils.bindSnuDown(selfTextHtml));
        } else {
            tvBody.setVisibility(View.GONE);
        }

        // Set vote buttons colors if the submission has been voted on.
        if (vote == VoteDirection.UPVOTE) {
            setUpvotedColors(context);
        } else if (vote == VoteDirection.DOWNVOTE) {
            setDownvotedColors(context);
        } else {
            setNoVoteColors(context);
        }

        // Set saved button color depending on whether the submission has been saved.
        if (isSaved) {
            setSavedIcon();
        } else {
            setUnsavedIcon();
        }

        initSaveBtnListener(redditAuthentication, customSubmission, submissionClickListener);
        initShareBtnListener(customSubmission.getSubmission(), shareSubject);
        initUpvoteBtnListener(context, redditAuthentication, customSubmission,
                submissionClickListener);
        initDownvoteBtnListener(context, redditAuthentication, customSubmission,
                submissionClickListener);
        initThumbnailListener(url, submissionClickListener);
        initContainerListener(customSubmission, submissionClickListener);
    }

    private void setUpvotedColors(Context context) {
        setUpvoteIcon(true);
        setDownvoteIcon(false);
        tvPoints.setTextColor(ContextCompat.getColor(context, R.color.commentUpvoted));
    }

    private void setDownvotedColors(Context context) {
        setUpvoteIcon(false);
        setDownvoteIcon(true);
        tvPoints.setTextColor(ContextCompat.getColor(context, R.color.commentDownvoted));
    }

    private void setNoVoteColors(Context context) {
        setUpvoteIcon(false);
        setDownvoteIcon(false);
        tvPoints.setTextColor(ContextCompat.getColor(context, R.color.commentNeutral));
    }

    private void setUpvoteIcon(boolean active) {
        if (active) {
            btnUpvote.setImageResource(R.drawable.ic_arrow_upward_orange_18dp);
        } else {
            btnUpvote.setImageResource(R.drawable.ic_arrow_upward_black_18dp);
        }
    }

    private void setDownvoteIcon(boolean active) {
        if (active) {
            btnDownvote.setImageResource(R.drawable.ic_arrow_downward_purple_18dp);
        } else {
            btnDownvote.setImageResource(R.drawable.ic_arrow_downward_black_18dp);
        }
    }

    private void setSavedIcon() {
        btnSave.setImageResource(R.drawable.ic_bookmark_black_18dp);
    }

    private void setUnsavedIcon() {
        btnSave.setImageResource(R.drawable.ic_bookmark_border_black_18dp);
    }

    private void initSaveBtnListener(final RedditAuthentication redditAuthentication,
                                     final CustomSubmission customSubmission,
                                     final OnSubmissionClickListener submissionClickListener) {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customSubmission.isSaved()) {
                    submissionClickListener.onSaveSubmission(customSubmission, false);
                    if (redditAuthentication.isUserLoggedIn()) {
                        setUnsavedIcon();
                        customSubmission.setSaved(false);
                    }
                } else {
                    submissionClickListener.onSaveSubmission(customSubmission, true);
                    if (redditAuthentication.isUserLoggedIn()) {
                        setSavedIcon();
                        customSubmission.setSaved(true);
                    }
                }
            }
        });
    }

    private void initUpvoteBtnListener(final Context context,
                                       final RedditAuthentication redditAuthentication,
                                       final CustomSubmission customSubmission,
                                       final OnSubmissionClickListener submissionClickListener) {
        btnUpvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customSubmission.getVoteDirection() == VoteDirection.UPVOTE) {
                    submissionClickListener.onVoteSubmission(customSubmission,
                            VoteDirection.NO_VOTE);
                    if (redditAuthentication.isUserLoggedIn()) {
                        customSubmission.setVoteDirection(VoteDirection.NO_VOTE);
                        setNoVoteColors(context);
                    }
                } else if (customSubmission.getVoteDirection() == VoteDirection.DOWNVOTE) {
                    submissionClickListener.onVoteSubmission(customSubmission,
                            VoteDirection.UPVOTE);
                    if (redditAuthentication.isUserLoggedIn()) {
                        customSubmission.setVoteDirection(VoteDirection.UPVOTE);
                        setUpvotedColors(context);
                    }
                } else {
                    submissionClickListener.onVoteSubmission(customSubmission,
                            VoteDirection.UPVOTE);
                    if (redditAuthentication.isUserLoggedIn()) {
                        customSubmission.setVoteDirection(VoteDirection.UPVOTE);
                        setUpvotedColors(context);
                    }
                }
            }
        });
    }

    private void initDownvoteBtnListener(final Context context,
                                         final RedditAuthentication redditAuthentication,
                                         final CustomSubmission customSubmission,
                                         final OnSubmissionClickListener submissionClickListener) {
        btnDownvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customSubmission.getVoteDirection() == VoteDirection.DOWNVOTE) {
                    submissionClickListener.onVoteSubmission(customSubmission,
                            VoteDirection.NO_VOTE);
                    if (redditAuthentication.isUserLoggedIn()) {
                        customSubmission.setVoteDirection(VoteDirection.NO_VOTE);
                        setNoVoteColors(context);
                    }
                } else if (customSubmission.getVoteDirection() == VoteDirection.UPVOTE) {
                    submissionClickListener.onVoteSubmission(customSubmission,
                            VoteDirection.DOWNVOTE);
                    if (redditAuthentication.isUserLoggedIn()) {
                        customSubmission.setVoteDirection(VoteDirection.DOWNVOTE);
                        setDownvotedColors(context);
                    }
                } else {
                    submissionClickListener.onVoteSubmission(customSubmission,
                            VoteDirection.DOWNVOTE);
                    if (redditAuthentication.isUserLoggedIn()) {
                        customSubmission.setVoteDirection(VoteDirection.DOWNVOTE);
                        setDownvotedColors(context);
                    }
                }
            }
        });
    }

    private void initShareBtnListener(final Submission submission,
                                      final PublishSubject<Submission> shareSubject) {
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareSubject.onNext(submission);
            }
        });
    }

    private void initThumbnailListener(final String url,
                                       final OnSubmissionClickListener submissionClickListener) {
        ivThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submissionClickListener.onContentClick(url);
            }
        });
        ivThumbnailSmall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submissionClickListener.onContentClick(url);
            }
        });
    }

    private void initContainerListener(final CustomSubmission customSubmission,
                                       final OnSubmissionClickListener submissionClickListener) {
        layoutContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submissionClickListener.onSubmissionClick(customSubmission);
            }
        });
    }
}
