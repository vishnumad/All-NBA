package com.gmail.jorgegilcavazos.ballislife.features.posts;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.features.model.SubscriberCount;
import com.gmail.jorgegilcavazos.ballislife.features.model.wrapper.CustomSubmission;
import com.gmail.jorgegilcavazos.ballislife.features.shared.FullCardViewHolder;
import com.gmail.jorgegilcavazos.ballislife.features.shared.OnSubmissionClickListener;
import com.gmail.jorgegilcavazos.ballislife.features.shared.PostListViewHolder;
import com.gmail.jorgegilcavazos.ballislife.util.Constants;
import com.gmail.jorgegilcavazos.ballislife.util.RedditUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CONTENT_CARD = 1;
    private static final int TYPE_CONTENT_LIST = 3;
    private static final int TYPE_LOADING = 2;

    private Context context;
    private List<CustomSubmission> postsList;
    private int contentViewType;
    private OnSubmissionClickListener submissionClickListener;
    private SubscriberCount subscriberCount;
    private OnLoadMoreListener loadMoreListener;
    private String subreddit;
    private boolean isLoading = false;
    private boolean loadingFailed = false;

    public PostsAdapter(Context context,
                        List<CustomSubmission> postsList,
                        int contentViewType,
                        OnSubmissionClickListener submissionClickListener,
                        String subreddit) {
        this.context = context;
        this.postsList = postsList;
        this.contentViewType = contentViewType;
        this.submissionClickListener = submissionClickListener;
        this.subreddit = subreddit;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else if (position == getItemCount() - 1) {
            return TYPE_LOADING;
        }

        switch (contentViewType) {
            case Constants.VIEW_CARD:
                return TYPE_CONTENT_CARD;
            case Constants.VIEW_LIST:
                return TYPE_CONTENT_LIST;
        }

        return TYPE_CONTENT_CARD;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view;

        if (viewType == TYPE_HEADER) {
            view = inflater.inflate(R.layout.rnba_header_layout, parent, false);
            return new HeaderViewHolder(view);
        } else if (viewType == TYPE_LOADING) {
            view = inflater.inflate(R.layout.row_load_more, parent, false);
            return new LoadHolder(view);
        }

        switch (contentViewType) {
            case Constants.VIEW_CARD:
                view = inflater.inflate(R.layout.post_layout_card, parent, false);
                return new FullCardViewHolder(view);
            case Constants.VIEW_LIST:
                view = inflater.inflate(R.layout.post_layout_list, parent, false);
                return new PostListViewHolder(view);
            default:
                view = inflater.inflate(R.layout.post_layout_card, parent, false);
                return new FullCardViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bindData(context, subscriberCount, subreddit);
        } else if (holder instanceof LoadHolder) {
            LoadHolder loadHolder = (LoadHolder) holder;
            // Load more items if scroll position is last and is not already loading.
            if (position >= getItemCount() - 1 && !isLoading && !loadingFailed && loadMoreListener != null
                    && postsList != null && !postsList.isEmpty()) {
                isLoading = true;
                loadMoreListener.onLoadMore();
            }

            if (isLoading) {
                loadHolder.progressBar.setVisibility(View.VISIBLE);
            } else {
                loadHolder.progressBar.setVisibility(View.GONE);
            }
        } else {
            CustomSubmission customSubmission = postsList.get(position - 1);
            switch (contentViewType) {
                case Constants.VIEW_CARD:
                    ((FullCardViewHolder) holder).bindData(context, customSubmission, true,
                            submissionClickListener);
                    break;
                case Constants.VIEW_LIST:
                    ((PostListViewHolder) holder).bindData(context, customSubmission, true,
                            submissionClickListener);
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return null != postsList ? postsList.size() + 2 : 2;
    }

    public void notifyDataChanged() {
        isLoading = false;
        notifyDataSetChanged();
    }

    public void setData(List<CustomSubmission> submissions) {
        loadingFailed = false;
        if (postsList != null) {
            postsList.clear();
        } else {
            postsList = new ArrayList<>();
        }
        postsList.addAll(submissions);
        notifyDataChanged();
    }

    public void addData(List<CustomSubmission> submissions) {
        if (postsList != null) {
            loadingFailed = false;
            postsList.addAll(submissions);
            notifyDataChanged();
        }
    }

    public void setSubscriberCount(SubscriberCount subscriberCount) {
        this.subscriberCount = subscriberCount;
        notifyDataChanged();
    }

    public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }

    public void setLoadingFailed(boolean failed) {
        loadingFailed = failed;
        notifyDataChanged();
    }

    interface OnLoadMoreListener {
        void onLoadMore();
    }

    public void setContentViewType(int viewType) {
        contentViewType = viewType;
        notifyDataChanged();
    }

    /* View Holders **/

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

    static class LoadHolder extends  RecyclerView.ViewHolder {

        @BindView(R.id.progressBar) ProgressBar progressBar;

        public LoadHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
