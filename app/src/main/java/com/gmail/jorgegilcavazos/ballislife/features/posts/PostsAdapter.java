package com.gmail.jorgegilcavazos.ballislife.features.posts;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.features.common.OnSubmissionClickListener;
import com.gmail.jorgegilcavazos.ballislife.features.common.PostListViewHolder;
import com.gmail.jorgegilcavazos.ballislife.features.model.SubmissionWrapper;
import com.gmail.jorgegilcavazos.ballislife.features.model.SubscriberCount;
import com.gmail.jorgegilcavazos.ballislife.util.Pair;
import com.gmail.jorgegilcavazos.ballislife.util.RedditUtils;
import com.gmail.jorgegilcavazos.ballislife.util.Utilities;
import com.google.common.base.Optional;
import com.google.firebase.crash.FirebaseCrash;
import com.squareup.picasso.Picasso;

import net.dean.jraw.models.Submission;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import static com.gmail.jorgegilcavazos.ballislife.util.Constants.POSTS_VIEW_LIST;
import static com.gmail.jorgegilcavazos.ballislife.util.Constants.POSTS_VIEW_WIDE_CARD;
import static com.gmail.jorgegilcavazos.ballislife.util.Constants.VIEW_HEADER;

public class PostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private RedditAuthentication redditAuthentication;
    private List<SubmissionWrapper> postsList;
    private int contentViewType;
    private OnSubmissionClickListener submissionClickListener;
    private SubscriberCount subscriberCount;
    private String subreddit;
    private int textColor;

    private PublishSubject<Submission> sharePublishSubject = PublishSubject.create();

    public PostsAdapter(Context context,
                        RedditAuthentication redditAuthentication, List<SubmissionWrapper>
                                postsList,
                        int contentViewType,
                        OnSubmissionClickListener submissionClickListener, String subreddit, int
                                textColor) {
        this.context = context;
        this.redditAuthentication = redditAuthentication;
        this.postsList = postsList;
        this.contentViewType = contentViewType;
        this.submissionClickListener = submissionClickListener;
        this.subreddit = subreddit;
        this.textColor = textColor;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view;
        if (viewType == VIEW_HEADER) {
            view = inflater.inflate(R.layout.rnba_header_layout, parent, false);
            return new HeaderViewHolder(view);
        }

        switch (viewType) {
            case POSTS_VIEW_LIST:
                view = inflater.inflate(R.layout.post_layout_list, parent, false);
                return new PostListViewHolder(view, textColor);
            case POSTS_VIEW_WIDE_CARD:
                view = inflater.inflate(R.layout.post_layout_card_wide, parent, false);
                return new WideCardViewHolder(view, textColor);
            default:
                throw new IllegalStateException("Posts view type is not valid: " + contentViewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bindData(context, subscriberCount, subreddit);
        } else {
            SubmissionWrapper submissionWrapper = postsList.get(position - 1);
            switch (contentViewType) {
                case POSTS_VIEW_LIST:
                    ((PostListViewHolder) holder).bindData(
                            context, redditAuthentication, submissionWrapper,
                            true,
                            submissionClickListener);
                    break;
                case POSTS_VIEW_WIDE_CARD:
                    ((WideCardViewHolder) holder).bindData(
                            context, redditAuthentication, submissionWrapper,
                            submissionClickListener,
                            sharePublishSubject);
                    break;
                default:
                    throw new IllegalStateException("Invalid view type in bind view holder: "
                            + contentViewType);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_HEADER;
        }
        if (contentViewType == 0) {
            FirebaseCrash.report(new IllegalArgumentException("contentViewType should not be 0"));
            return POSTS_VIEW_WIDE_CARD;
        }
        return contentViewType;

    }

    @Override
    public int getItemCount() {
        // Add 1 for header.
        return null != postsList ? postsList.size() + 1 : 1;
    }

    public void setData(List<SubmissionWrapper> submissions) {
        if (postsList != null) {
            postsList.clear();
        } else {
            postsList = new ArrayList<>();
        }
        postsList.addAll(submissions);
        preFetchImages(submissions);
        notifyDataSetChanged();
    }

    public void addData(List<SubmissionWrapper> submissions) {
        if (postsList == null) {
            postsList = new ArrayList<>();
        }
        postsList.addAll(submissions);
        preFetchImages(submissions);
        notifyDataSetChanged();
    }

    public void setSubscriberCount(SubscriberCount subscriberCount) {
        this.subscriberCount = subscriberCount;
        notifyDataSetChanged();
    }

    public void setContentViewType(int viewType) {
        contentViewType = viewType;
        notifyDataSetChanged();
    }

    public Observable<Submission> getShareObservable() {
        return sharePublishSubject;
    }

    private void preFetchImages(List<SubmissionWrapper> submissions) {
        for (SubmissionWrapper submission : submissions) {
            Optional<Pair<Utilities.ThumbnailType, String>> thumbnailTypeUrl =
                    Utilities.getThumbnailToShowFromCustomSubmission(submission);
            if (thumbnailTypeUrl.isPresent()) {
                Picasso.with(context)
                        .load(thumbnailTypeUrl.get().second)
                        .fetch();
            }
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_logo) ImageView ivLogo;
        @BindView(R.id.text_subreddit) TextView tvSubreddit;
        @BindView(R.id.text_subscribers) TextView tvSubscribers;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindData(Context context, SubscriberCount subscriberCount, String subreddit) {
            ivLogo.setImageResource(RedditUtils.getTeamSnoo(subreddit));
            tvSubreddit.setText("r/" + subreddit);

            if (subscriberCount != null) {
                String subscribers = String.valueOf(subscriberCount.getSubscribers());
                String activeUsers = String.valueOf(subscriberCount.getActiveUsers());

                tvSubscribers.setVisibility(View.VISIBLE);
                tvSubscribers.setText(context.getString(R.string.subscriber_count,
                        subscribers, activeUsers));
            } else {
                tvSubscribers.setVisibility(View.INVISIBLE);
                tvSubscribers.setText(context.getString(R.string.subscriber_count,
                        String.valueOf(0), String.valueOf(0)));
            }
        }
    }
}
