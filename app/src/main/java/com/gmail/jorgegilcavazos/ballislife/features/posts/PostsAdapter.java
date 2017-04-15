package com.gmail.jorgegilcavazos.ballislife.features.posts;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gmail.jorgegilcavazos.ballislife.features.model.SubscriberCount;
import com.gmail.jorgegilcavazos.ballislife.features.model.wrapper.CustomSubmission;
import com.gmail.jorgegilcavazos.ballislife.features.shared.FullCardViewHolder;
import com.gmail.jorgegilcavazos.ballislife.features.shared.OnSubmissionClickListener;
import com.gmail.jorgegilcavazos.ballislife.util.Constants;
import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil;
import com.gmail.jorgegilcavazos.ballislife.util.RedditUtils;
import com.squareup.picasso.Picasso;

import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int HEADER = 0;
    private static final int CONTENT = 1;

    private Context context;
    private List<CustomSubmission> postsList;
    private PostsFragment.ViewType type;
    private OnSubmissionClickListener listener;
    private SubscriberCount subscriberCount;

    public PostsAdapter(List<CustomSubmission> postsList, PostsFragment.ViewType type,
                        OnSubmissionClickListener listener,
                        SubscriberCount subscriberCount) {
        this.postsList = postsList;
        this.type = type;
        this.listener = listener;
        this.subscriberCount = subscriberCount;
    }

    @Override
    public int getItemViewType(int position) {
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
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rnba_header_layout,
                    parent, false);
            return new HeaderViewHolder(view);
        }

        switch (type) {
            case FULL_CARD:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_layout_large,
                        parent, false);
                return new FullCardViewHolder(view);
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_layout_large,
                        parent, false);
                return new FullCardViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;

            if (subscriberCount != null) {
                String subscribers = String.valueOf(subscriberCount.getSubscribers());
                String activeUsers = String.valueOf(subscriberCount.getActiveUsers());

                headerViewHolder.tvSubscribers.setText(context.getString(R.string.subscriber_count,
                        subscribers, activeUsers));
            } else {
                headerViewHolder.tvSubscribers.setText(context.getString(R.string.subscriber_count,
                        String.valueOf(554843), String.valueOf(8133)));
            }
        } else {
            CustomSubmission submission = postsList.get(position - 1);

            switch (type) {
                case FULL_CARD:
                    FullCardViewHolder myHolder = (FullCardViewHolder) holder;
                    setFullCardViews(myHolder, submission);
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return null != postsList ? postsList.size() + 1 : 1;
    }

    public void setData(List<CustomSubmission> submissions) {
        postsList = submissions;
        notifyDataSetChanged();
    }

    public void setSubscriberCount(SubscriberCount subscriberCount) {
        this.subscriberCount = subscriberCount;
        notifyDataSetChanged();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_subscribers) TextView tvSubscribers;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private void setFullCardViews(final FullCardViewHolder holder,
                                  final CustomSubmission customSubmission) {
        final Submission submission = customSubmission.getSubmission();

        holder.tvBody.setVisibility(View.GONE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            holder.tvTitle.setText(Html.fromHtml(submission.getTitle(), Html.FROM_HTML_MODE_LEGACY));
        } else {
            holder.tvTitle.setText(Html.fromHtml(submission.getTitle()));
        }

        if (submission.isStickied()) {
            holder.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.stickiedColor));
            holder.tvTitle.setTypeface(null, Typeface.BOLD);
        } else {
            holder.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.primaryText));
            holder.tvTitle.setTypeface(null, Typeface.NORMAL);
        }

        if (submission.getVote() == VoteDirection.UPVOTE) {
            RedditUtils.setUpvotedColors(context, holder);
        } else if (submission.getVote() == VoteDirection.DOWNVOTE) {
            RedditUtils.setDownvotedColors(context, holder);
        } else {
            RedditUtils.setNoVoteColors(context, holder);
        }

        if (submission.isSaved()) {
            RedditUtils.setSavedColors(context, holder);
        } else {
            RedditUtils.setUnsavedColors(context, holder);
        }

        holder.tvPoints.setText(String.valueOf(submission.getScore()));
        holder.tvAuthor.setText(submission.getAuthor());
        holder.tvTimestamp.setText(DateFormatUtil.formatRedditDate(submission.getCreated()));
        holder.tvComments.setText(String.valueOf(submission.getCommentCount()));

        String highResThumbnailUrl;
        try {
            highResThumbnailUrl = submission.getOEmbedMedia().getThumbnail().getUrl().toString();
        } catch (NullPointerException e) {
            highResThumbnailUrl = submission.getThumbnail();
        }

        if (submission.isSelfPost()) {
            holder.tvDomain.setText("self");
            holder.ivThumbnail.setVisibility(View.GONE);
        } else {
            String domain = submission.getDomain();
            holder.tvDomain.setText(domain);
            if (highResThumbnailUrl != null) {
                holder.ivThumbnail.setVisibility(View.VISIBLE);
                Picasso.with(context)
                        .load(highResThumbnailUrl)
                        .into(holder.ivThumbnail);

                // TODO: add view to play content

                if (domain.equals(Constants.YOUTUBE_DOMAIN)
                        || domain.equals(Constants.INSTAGRAM_DOMAIN)
                        || domain.equals(Constants.STREAMABLE_DOMAIN)) {
                    //thumbnailType.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                } else if (domain.equals(Constants.IMGUR_DOMAIN )
                        || domain.equals(Constants.GIPHY_DOMAIN)) {
                    //thumbnailType.setImageResource(R.drawable.ic_gif_black_24dp);
                } else {
                    //thumbnailType.setVisibility(View.GONE);
                }
            } else {
                holder.ivThumbnail.setVisibility(View.GONE);
            }
        }

        holder.btnUpvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customSubmission.getVoteDirection() == VoteDirection.UPVOTE) {
                    listener.onVoteSubmission(submission, VoteDirection.NO_VOTE);
                    customSubmission.setVoteDirection(VoteDirection.NO_VOTE);
                    RedditUtils.setNoVoteColors(context, holder);
                    holder.tvPoints.setText(String.valueOf(Integer.valueOf(
                            holder.tvPoints.getText().toString()) - 1));
                } else if (customSubmission.getVoteDirection() == VoteDirection.DOWNVOTE) {
                    listener.onVoteSubmission(submission, VoteDirection.UPVOTE);
                    customSubmission.setVoteDirection(VoteDirection.UPVOTE);
                    RedditUtils.setUpvotedColors(context, holder);
                    holder.tvPoints.setText(String.valueOf(Integer.valueOf(
                            holder.tvPoints.getText().toString()) + 2));
                } else {
                    listener.onVoteSubmission(submission, VoteDirection.UPVOTE);
                    customSubmission.setVoteDirection(VoteDirection.UPVOTE);
                    RedditUtils.setUpvotedColors(context, holder);
                    holder.tvPoints.setText(String.valueOf(Integer.valueOf(
                            holder.tvPoints.getText().toString()) + 1));
                }
            }
        });

        holder.btnDownvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customSubmission.getVoteDirection() == VoteDirection.DOWNVOTE) {
                    listener.onVoteSubmission(submission, VoteDirection.NO_VOTE);
                    customSubmission.setVoteDirection(VoteDirection.NO_VOTE);
                    RedditUtils.setNoVoteColors(context, holder);
                    holder.tvPoints.setText(String.valueOf(Integer.valueOf(
                            holder.tvPoints.getText().toString()) + 1));
                } else if (customSubmission.getVoteDirection() == VoteDirection.UPVOTE){
                    listener.onVoteSubmission(submission, VoteDirection.DOWNVOTE);
                    customSubmission.setVoteDirection(VoteDirection.DOWNVOTE);
                    RedditUtils.setDownvotedColors(context, holder);
                    holder.tvPoints.setText(String.valueOf(Integer.valueOf(
                            holder.tvPoints.getText().toString()) - 2));
                } else {
                    listener.onVoteSubmission(submission, VoteDirection.DOWNVOTE);
                    customSubmission.setVoteDirection(VoteDirection.DOWNVOTE);
                    RedditUtils.setDownvotedColors(context, holder);
                    holder.tvPoints.setText(String.valueOf(Integer.valueOf(
                            holder.tvPoints.getText().toString()) - 1));
                }
            }
        });

        holder.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customSubmission.isSaved()) {
                    listener.onSaveSubmission(submission, false);
                    RedditUtils.setUnsavedColors(context, holder);
                    customSubmission.setSaved(false);
                } else {
                    listener.onSaveSubmission(submission, true);
                    RedditUtils.setSavedColors(context, holder);
                    customSubmission.setSaved(true);
                }
            }
        });

        holder.btnComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onSubmissionClick(submission);
            }
        });

        holder.tvTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onSubmissionClick(submission);
            }
        });
    }

}
