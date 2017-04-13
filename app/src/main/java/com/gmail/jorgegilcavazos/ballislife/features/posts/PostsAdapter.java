package com.gmail.jorgegilcavazos.ballislife.features.posts;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmail.jorgegilcavazos.ballislife.features.model.wrapper.CustomSubmission;
import com.gmail.jorgegilcavazos.ballislife.util.Constants;
import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil;
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
    private PostsFragment.OnPostClickListener listener;

    public PostsAdapter(List<CustomSubmission> postsList, PostsFragment.ViewType type,
                        PostsFragment.OnPostClickListener listener) {
        this.postsList = postsList;
        this.type = type;
        this.listener = listener;
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
            headerViewHolder.tvSubscribers.setText("554843 subscribers • 8133 fans online");
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

    class FullCardViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_title) TextView tvTitle;
        @BindView(R.id.text_author) TextView tvAuthor;
        @BindView(R.id.text_timestamp) TextView tvTimestamp;
        @BindView(R.id.text_domain) TextView tvDomain;
        @BindView(R.id.image_thumbnail) ImageView ivThumbnail;
        @BindView(R.id.button_upvote) ImageButton btnUpvote;
        @BindView(R.id.text_points) TextView tvPoints;
        @BindView(R.id.button_downvote) ImageButton btnDownvote;
        @BindView(R.id.button_comments) ImageButton btnComments;
        @BindView(R.id.text_comments) TextView tvComments;
        @BindView(R.id.button_save) ImageButton btnSave;
        //@BindView(R.id.text_save) TextView tvSave;

        public FullCardViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_subscribers) TextView tvSubscribers;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private void setFullCardViews(final FullCardViewHolder holder,
                                  final CustomSubmission customSubmission) {
        final Submission submission = customSubmission.getSubmission();

        holder.tvTitle.setText(submission.getTitle());

        if (submission.isStickied()) {
            holder.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.stickiedColor));
            holder.tvTitle.setTypeface(null, Typeface.BOLD);
        } else {
            holder.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.primaryText));
            holder.tvTitle.setTypeface(null, Typeface.NORMAL);
        }

        if (submission.getVote() == VoteDirection.UPVOTE) {
            setUpvotedColors(holder);
        } else if (submission.getVote() == VoteDirection.DOWNVOTE) {
            setDownvotedColors(holder);
        } else {
            setNoVoteColors(holder);
        }

        if (submission.isSaved()) {
            setSavedColors(holder);
        } else {
            setUnsavedColors(holder);
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
                    listener.onVote(submission, VoteDirection.NO_VOTE);
                    customSubmission.setVoteDirection(VoteDirection.NO_VOTE);
                    setNoVoteColors(holder);
                    holder.tvPoints.setText(String.valueOf(Integer.valueOf(
                            holder.tvPoints.getText().toString()) - 1));
                } else if (customSubmission.getVoteDirection() == VoteDirection.DOWNVOTE) {
                    listener.onVote(submission, VoteDirection.UPVOTE);
                    customSubmission.setVoteDirection(VoteDirection.UPVOTE);
                    setUpvotedColors(holder);
                    holder.tvPoints.setText(String.valueOf(Integer.valueOf(
                            holder.tvPoints.getText().toString()) + 2));
                } else {
                    listener.onVote(submission, VoteDirection.UPVOTE);
                    customSubmission.setVoteDirection(VoteDirection.UPVOTE);
                    setUpvotedColors(holder);
                    holder.tvPoints.setText(String.valueOf(Integer.valueOf(
                            holder.tvPoints.getText().toString()) + 1));
                }
            }
        });

        holder.btnDownvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customSubmission.getVoteDirection() == VoteDirection.DOWNVOTE) {
                    listener.onVote(submission, VoteDirection.NO_VOTE);
                    customSubmission.setVoteDirection(VoteDirection.NO_VOTE);
                    setNoVoteColors(holder);
                    holder.tvPoints.setText(String.valueOf(Integer.valueOf(
                            holder.tvPoints.getText().toString()) + 1));
                } else if (customSubmission.getVoteDirection() == VoteDirection.UPVOTE){
                    listener.onVote(submission, VoteDirection.DOWNVOTE);
                    customSubmission.setVoteDirection(VoteDirection.DOWNVOTE);
                    setDownvotedColors(holder);
                    holder.tvPoints.setText(String.valueOf(Integer.valueOf(
                            holder.tvPoints.getText().toString()) - 2));
                } else {
                    listener.onVote(submission, VoteDirection.DOWNVOTE);
                    customSubmission.setVoteDirection(VoteDirection.DOWNVOTE);
                    setDownvotedColors(holder);
                    holder.tvPoints.setText(String.valueOf(Integer.valueOf(
                            holder.tvPoints.getText().toString()) - 1));
                }
            }
        });

        holder.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customSubmission.isSaved()) {
                    listener.onSave(submission, false);
                    setUnsavedColors(holder);
                    customSubmission.setSaved(false);
                } else {
                    listener.onSave(submission, true);
                    setSavedColors(holder);
                    customSubmission.setSaved(true);
                }
            }
        });

        holder.btnComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onPostClick(submission);
            }
        });

        holder.tvTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onPostClick(submission);
            }
        });
    }

    private void setUpvotedColors(final FullCardViewHolder holder) {
        DrawableCompat.setTint(holder.btnUpvote.getDrawable().mutate(),
                ContextCompat.getColor(context, R.color.commentUpvoted));
        DrawableCompat.setTint(holder.btnDownvote.getDrawable().mutate(),
                ContextCompat.getColor(context, R.color.commentNeutral));
        holder.tvPoints.setTextColor(ContextCompat.getColor(context, R.color.commentUpvoted));
    }

    private void setDownvotedColors(final FullCardViewHolder holder) {
        DrawableCompat.setTint(holder.btnUpvote.getDrawable().mutate(),
                ContextCompat.getColor(context, R.color.commentNeutral));
        DrawableCompat.setTint(holder.btnDownvote.getDrawable().mutate(),
                ContextCompat.getColor(context, R.color.commentDownvoted));
        holder.tvPoints.setTextColor(ContextCompat.getColor(context, R.color.commentDownvoted));
    }

    private void setNoVoteColors(final FullCardViewHolder holder) {
        DrawableCompat.setTint(holder.btnUpvote.getDrawable().mutate(),
                ContextCompat.getColor(context, R.color.commentNeutral));
        DrawableCompat.setTint(holder.btnDownvote.getDrawable().mutate(),
                ContextCompat.getColor(context, R.color.commentNeutral));
        holder.tvPoints.setTextColor(ContextCompat.getColor(context, R.color.commentNeutral));
    }

    private void setSavedColors(final FullCardViewHolder holder) {
        DrawableCompat.setTint(holder.btnSave.getDrawable().mutate(),
                ContextCompat.getColor(context, R.color.amber));
    }

    private void setUnsavedColors(final FullCardViewHolder holder) {
        DrawableCompat.setTint(holder.btnSave.getDrawable().mutate(),
                ContextCompat.getColor(context, R.color.commentNeutral));
    }

}
