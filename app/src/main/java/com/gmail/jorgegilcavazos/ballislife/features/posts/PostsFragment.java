package com.gmail.jorgegilcavazos.ballislife.features.posts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.features.model.SubscriberCount;
import com.gmail.jorgegilcavazos.ballislife.features.model.wrapper.CustomSubmission;
import com.gmail.jorgegilcavazos.ballislife.features.shared.OnSubmissionClickListener;
import com.gmail.jorgegilcavazos.ballislife.features.submission.SubmissionActivity;
import com.gmail.jorgegilcavazos.ballislife.features.videoplayer.VideoPlayerActivity;
import com.gmail.jorgegilcavazos.ballislife.network.API.RedditService;
import com.gmail.jorgegilcavazos.ballislife.util.Constants;
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.SchedulerProvider;

import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Context.MODE_PRIVATE;
import static com.gmail.jorgegilcavazos.ballislife.network.RedditAuthentication.REDDIT_AUTH_PREFS;

public class PostsFragment extends Fragment implements PostsView,
        SwipeRefreshLayout.OnRefreshListener, OnSubmissionClickListener,
        PostsAdapter.OnLoadMoreListener {

    private static final String TAG = "PostsFragment";

    public static final int TYPE_FIRST_LOAD = 0;
    public static final int TYPE_LOAD_MORE = 1;

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

        postsAdapter = new PostsAdapter(getActivity(), null, viewType, this);
        postsAdapter.setLoadMoreListener(this);
        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewPosts.setAdapter(postsAdapter);

        SharedPreferences preferences = getActivity().getSharedPreferences(REDDIT_AUTH_PREFS,
                MODE_PRIVATE);

        presenter = new PostsPresenter(new RedditService(), preferences, SchedulerProvider.getInstance());
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
    public void addPosts(List<CustomSubmission> submissions) {
        postsAdapter.addData(submissions);
    }

    @Override
    public void showPostsLoadingFailedSnackbar(final int loadType) {
        if (getView() != null) {
            snackbar = Snackbar.make(getView(), R.string.posts_loading_failed,
                    Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(R.string.retry, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (loadType == TYPE_FIRST_LOAD) {
                        presenter.loadPosts();
                    } else {
                        presenter.loadMorePosts();
                    }
                }
            });
            snackbar.show();
        }
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
    public void showNotLoggedInToast() {
        Toast.makeText(getActivity(), R.string.not_logged_in, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSubscribers(SubscriberCount subscriberCount) {
        postsAdapter.setSubscriberCount(subscriberCount);
    }

    @Override
    public void openContentTab(String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(getActivity(), Uri.parse(url));
    }

    @Override
    public void setLoadingFailed(boolean failed) {
        postsAdapter.setLoadingFailed(failed);
    }

    @Override
    public void showNothingToShowToast() {
        Toast.makeText(getActivity(), R.string.nothing_to_show, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void openStreamable(String shortcode) {
        Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);
        intent.putExtra(VideoPlayerActivity.SHORTCODE, shortcode);
        startActivity(intent);
    }

    @Override
    public void showContentUnavailableToast() {
        Toast.makeText(getActivity(), R.string.content_not_available, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSubmissionClick(Submission submission) {
        Intent intent = new Intent(getActivity(), SubmissionActivity.class);

        Bundle bundle = new Bundle();

        String highResThumbnailUrl;
        try {
            highResThumbnailUrl = submission.getOEmbedMedia().getThumbnail().getUrl().toString();
        } catch (NullPointerException e) {
            highResThumbnailUrl = null;
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
                submission.getThumbnail(),
                highResThumbnailUrl,
                submission.getVote(),
                submission.isSaved(),
                submission.data("selftext_html"),
                submission.getUrl()
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

    @Override
    public void onContentClick(String url) {
        presenter.onContentClick(url);
    }

    @Override
    public void onLoadMore() {
        presenter.loadMorePosts();
    }
}
