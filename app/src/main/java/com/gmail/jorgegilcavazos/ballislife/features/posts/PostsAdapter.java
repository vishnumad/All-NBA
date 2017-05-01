package com.gmail.jorgegilcavazos.ballislife.features.posts;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.features.model.SubscriberCount;
import com.gmail.jorgegilcavazos.ballislife.features.model.wrapper.CustomSubmission;
import com.gmail.jorgegilcavazos.ballislife.features.shared.FullCardViewHolder;
import com.gmail.jorgegilcavazos.ballislife.features.shared.OnSubmissionClickListener;
import com.gmail.jorgegilcavazos.ballislife.data.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.util.Constants;
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil;
import com.gmail.jorgegilcavazos.ballislife.util.RedditUtils;
import com.squareup.picasso.Picasso;

import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CONTENT = 1;
    private static final int TYPE_LOADING = 2;

    private Context context;
    private List<CustomSubmission> postsList;
    private PostsFragment.ViewType contentViewType;
    private OnSubmissionClickListener submissionClickListener;
    private SubscriberCount subscriberCount;
    private OnLoadMoreListener loadMoreListener;
    private boolean isLoading = false;
    private boolean loadingFailed = false;

    public PostsAdapter(Context context,
                        List<CustomSubmission> postsList,
                        PostsFragment.ViewType contentViewType,
                        OnSubmissionClickListener submissionClickListener) {
        this.context = context;
        this.postsList = postsList;
        this.contentViewType = contentViewType;
        this.submissionClickListener = submissionClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else if (position == getItemCount() - 1) {
            return TYPE_LOADING;
        }

        return TYPE_CONTENT;
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
            case FULL_CARD:
                view = inflater.inflate(R.layout.post_layout_card, parent, false);
                return new FullCardViewHolder(view);
            default:
                view = inflater.inflate(R.layout.post_layout_card, parent, false);
                return new FullCardViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bindData(context, subscriberCount);
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
                case FULL_CARD:
                    ((FullCardViewHolder) holder).bindData(context, customSubmission, true,
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
        postsList = submissions;
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

    /* View Holders **/

    static class HeaderViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_subscribers) TextView tvSubscribers;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindData(Context context, SubscriberCount subscriberCount) {
            if (subscriberCount != null) {
                String subscribers = String.valueOf(subscriberCount.getSubscribers());
                String activeUsers = String.valueOf(subscriberCount.getActiveUsers());

                tvSubscribers.setText(context.getString(R.string.subscriber_count,
                        subscribers, activeUsers));
            } else {
                tvSubscribers.setText(context.getString(R.string.subscriber_count,
                        String.valueOf(554843), String.valueOf(8133)));
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
