package com.gmail.jorgegilcavazos.ballislife.features.posts;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.gmail.jorgegilcavazos.ballislife.features.model.SubscriberCount;
import com.gmail.jorgegilcavazos.ballislife.features.model.wrapper.CustomSubmission;
import com.gmail.jorgegilcavazos.ballislife.network.API.RedditService;
import com.gmail.jorgegilcavazos.ballislife.util.Constants;
import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil;
import com.gmail.jorgegilcavazos.ballislife.features.submission.SubmissionActivity;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.SchedulerProvider;

import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostsFragment extends Fragment implements PostsView,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "PostsFragment";

    public enum ViewType {
        FULL_CARD, SMALL_CARD, LIST
    }

    private static final String VIEW_TYPE = "viewType";

    @BindView(R.id.swipeRefreshLayout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView_posts) RecyclerView recyclerViewPosts;

    private ViewType viewType;
    private Snackbar snackbar;
    private PostsAdapter postsAdapter;

    private PostsPresenter presenter;

    public PostsFragment() {

    }

    public static PostsFragment newInstance(ViewType viewType) {
        PostsFragment fragment = new PostsFragment();
        Bundle args = new Bundle();
        args.putSerializable(VIEW_TYPE, viewType);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            viewType = (ViewType) getArguments().get(VIEW_TYPE);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posts, container, false);
        ButterKnife.bind(this, view);

        swipeRefreshLayout.setOnRefreshListener(this);

        postsAdapter = new PostsAdapter(null, viewType, new OnPostClickListener() {
            @Override
            public void onPostClick(Submission submission) {
                Intent intent = new Intent(getActivity(), SubmissionActivity.class);

                Bundle bundle = new Bundle();
                bundle.putString(Constants.THREAD_ID, submission.getId());
                bundle.putString(Constants.THREAD_TITLE, submission.getTitle());
                bundle.putString(Constants.THREAD_DESCRIPTION, submission.getSelftext());
                bundle.putString(Constants.THREAD_AUTHOR, submission.getAuthor());
                bundle.putString(Constants.THREAD_TIMESTAMP,
                        DateFormatUtil.formatRedditDate(submission.getCreated()));
                bundle.putString(Constants.THREAD_SCORE, String.valueOf(submission.getScore()));
                bundle.putString(Constants.THREAD_NUM_COMMENTS,
                        String.valueOf(submission.getCommentCount()));
                bundle.putString(Constants.THREAD_DOMAIN, submission.getDomain());
                bundle.putString(Constants.THREAD_URL, submission.getUrl());
                if (submission.getThumbnails() != null) {
                    bundle.putString(Constants.THREAD_IMAGE,
                            submission.getThumbnails().getSource().getUrl());
                } else {
                    bundle.putString(Constants.THREAD_IMAGE, null);
                }
                bundle.putBoolean(Constants.THREAD_SELF, submission.isSelfPost());

                intent.putExtras(bundle);
                startActivity(intent);
            }

            @Override
            public void onVote(Submission submission, VoteDirection voteDirection) {
                presenter.onVote(submission, voteDirection);
            }

            @Override
            public void onSave(Submission submission, boolean saved) {
                presenter.onSave(submission, saved);
            }
        }, null);

        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewPosts.setAdapter(postsAdapter);

        presenter = new PostsPresenter(new RedditService(), SchedulerProvider.getInstance());
        presenter.attachView(this);
        presenter.loadSubscriberCount();
        presenter.loadPosts();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.detachView();
        presenter.stop();
        dismissSnackbar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                presenter.loadSubscriberCount();
                presenter.loadPosts();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        presenter.loadSubscriberCount();
        presenter.loadPosts();
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        swipeRefreshLayout.setRefreshing(active);
    }

    @Override
    public void showPosts(List<CustomSubmission> submissions) {
        postsAdapter.setData(submissions);
        recyclerViewPosts.setVisibility(View.VISIBLE);
    }

    @Override
    public void hidePosts() {
        recyclerViewPosts.setVisibility(View.GONE);
    }

    @Override
    public void showPostsLoadingFailedSnackbar() {
        snackbar = Snackbar.make(getView(), R.string.posts_loading_failed,
                Snackbar.LENGTH_INDEFINITE);

        snackbar.setAction(R.string.retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.loadPosts();
            }
        });
    }

    @Override
    public void dismissSnackbar() {
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
        }
    }

    @Override
    public void showNotAuthenticatedToast() {
        Toast.makeText(getActivity(), R.string.not_authenticated, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSubscribers(SubscriberCount subscriberCount) {
        postsAdapter.setSubscriberCount(subscriberCount);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public interface OnPostClickListener {
        void onPostClick(Submission submission);

        void onVote(Submission submission, VoteDirection voteDirection);

        void onSave(Submission submission, boolean saved);
    }
}
