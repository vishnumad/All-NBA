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
import com.gmail.jorgegilcavazos.ballislife.features.shared.OnSubmissionClickListener;
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
        SwipeRefreshLayout.OnRefreshListener, OnSubmissionClickListener {

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

        postsAdapter = new PostsAdapter(null, viewType, this, null);

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
    public void onSubmissionClick(Submission submission) {
        Intent intent = new Intent(getActivity(), SubmissionActivity.class);

        Bundle bundle = new Bundle();

        String highResThumbnailUrl;
        try {
            highResThumbnailUrl = submission.getOEmbedMedia().getThumbnail().getUrl().toString();
        } catch (NullPointerException e) {
            highResThumbnailUrl = submission.getThumbnail();
        }

        CustomSubmission customSubmission = new CustomSubmission(
                submission.getTitle(),
                submission.getAuthor(),
                DateFormatUtil.formatRedditDate(submission.getCreated()),
                submission.getDomain(),
                submission.isSelfPost(),
                submission.isStickied(),
                submission.getScore(),
                submission.getCommentCount(),
                highResThumbnailUrl,
                submission.getVote(),
                submission.isSaved()
        );

        bundle.putSerializable(Constants.THREAD_SUBMISSION, customSubmission);
        bundle.putString(Constants.THREAD_ID, submission.getId());

        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onVoteSubmission(Submission submission, VoteDirection voteDirection) {
        presenter.onVote(submission, voteDirection);
    }

    @Override
    public void onSaveSubmission(Submission submission, boolean saved) {
        presenter.onSave(submission, saved);
    }
}
