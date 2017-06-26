package com.gmail.jorgegilcavazos.ballislife.features.posts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.data.local.LocalRepository;
import com.gmail.jorgegilcavazos.ballislife.data.repository.posts.PostsRepository;
import com.gmail.jorgegilcavazos.ballislife.features.application.BallIsLifeApplication;
import com.gmail.jorgegilcavazos.ballislife.features.model.SubscriberCount;
import com.gmail.jorgegilcavazos.ballislife.features.model.wrapper.CustomSubmission;
import com.gmail.jorgegilcavazos.ballislife.features.shared.EndlessRecyclerViewScrollListener;
import com.gmail.jorgegilcavazos.ballislife.features.shared.OnSubmissionClickListener;
import com.gmail.jorgegilcavazos.ballislife.features.submission.SubmissionActivity;
import com.gmail.jorgegilcavazos.ballislife.features.videoplayer.VideoPlayerActivity;
import com.gmail.jorgegilcavazos.ballislife.util.Constants;
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil;

import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class PostsFragment extends Fragment implements PostsView,
        SwipeRefreshLayout.OnRefreshListener, OnSubmissionClickListener {

    private static final String TAG = "PostsFragment";
    private static final String SUBREDDIT = "subreddit";
    private static final String LIST_STATE = "listState";

    @Inject
    LocalRepository localRepository;

    @Inject
    @Named("redditSharedPreferences")
    SharedPreferences redditSharedPreferences;

    @Inject
    PostsRepository postsRepository;

    @BindView(R.id.swipeRefreshLayout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView_posts) RecyclerView recyclerViewPosts;
    Parcelable listState;
    private int viewType;
    private String subreddit;
    private Snackbar snackbar;
    private PostsAdapter postsAdapter;
    private LinearLayoutManager linearLayoutManager;
    private EndlessRecyclerViewScrollListener scrollListener;
    private PostsPresenter presenter;
    private Unbinder unbinder;
    private Sorting sorting = Sorting.HOT;
    private TimePeriod timePeriod = TimePeriod.ALL;
    private Menu menu;

    public PostsFragment() {
        BallIsLifeApplication.getAppComponent().inject(this);
    }

    public static PostsFragment newInstance(String subreddit) {
        PostsFragment fragment = new PostsFragment();
        Bundle args = new Bundle();
        args.putString(SUBREDDIT, subreddit);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save layout manager state to restore scroll position after config changes.
        listState = linearLayoutManager.onSaveInstanceState();
        outState.putParcelable(LIST_STATE, listState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            listState = savedInstanceState.getParcelable(LIST_STATE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            subreddit = getArguments().getString(SUBREDDIT);
        }

        if (localRepository.getFavoritePostsViewType() != -1) {
            viewType = localRepository.getFavoritePostsViewType();
        } else {
            viewType = Constants.VIEW_CARD;
        }

        linearLayoutManager = new LinearLayoutManager(getActivity());
        postsAdapter = new PostsAdapter(getActivity(), null, viewType, this, subreddit);

        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load cached data if available, or from network if not.
        presenter.loadFirstAvailable(sorting, timePeriod);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posts, container, false);
        unbinder = ButterKnife.bind(this, view);

        swipeRefreshLayout.setOnRefreshListener(this);

        recyclerViewPosts.setLayoutManager(linearLayoutManager);
        recyclerViewPosts.setAdapter(postsAdapter);

        // Pass 1 as staticItemCount because of the posts header.
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager,
                1 /* staticItemCount */) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                presenter.loadPosts(false /* reset */);
            }
        };

        recyclerViewPosts.addOnScrollListener(scrollListener);

        presenter = new PostsPresenter(subreddit);
        presenter.attachView(this);
        presenter.loadSubscriberCount();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        presenter.detachView();
        presenter.stop();
        dismissSnackbar();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_posts, menu);
        this.menu = menu;
        super.onCreateOptionsMenu(menu, inflater);

        setViewIcon(viewType);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                presenter.loadSubscriberCount();
                presenter.resetLoaderFromStartWithParams(sorting, timePeriod);
                presenter.loadPosts(true /* reset */);
                return true;
            case R.id.action_change_view:
                openViewPickerDialog();
                return true;
            case R.id.action_sort_hot:
                sorting = Sorting.HOT;
                presenter.resetLoaderFromStartWithParams(sorting, timePeriod);
                presenter.loadPosts(true /* reset */);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("HOT");
                return true;
            case R.id.action_sort_new:
                sorting = Sorting.NEW;
                presenter.resetLoaderFromStartWithParams(sorting, timePeriod);
                presenter.loadPosts(true /* reset */);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("NEW");
                return true;
            case R.id.action_sort_rising:
                sorting = Sorting.RISING;
                presenter.resetLoaderFromStartWithParams(sorting, timePeriod);
                presenter.loadPosts(true /* reset */);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("RISING");
                return true;
            case R.id.action_sort_controversial:
                sorting = Sorting.CONTROVERSIAL;
                presenter.resetLoaderFromStartWithParams(sorting, timePeriod);
                presenter.loadPosts(true /* reset */);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("CONTROVERSIAL");
                return true;
            case R.id.action_sort_top_hour:
                sorting = Sorting.TOP;
                timePeriod = TimePeriod.HOUR;
                presenter.resetLoaderFromStartWithParams(sorting, timePeriod);
                presenter.loadPosts(true /* reset */);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("TOP: HOUR");
                return true;
            case R.id.action_sort_top_day:
                sorting = Sorting.TOP;
                timePeriod = TimePeriod.DAY;
                presenter.resetLoaderFromStartWithParams(sorting, timePeriod);
                presenter.loadPosts(true /* reset */);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("TOP: DAY");
                return true;
            case R.id.action_sort_top_week:
                sorting = Sorting.TOP;
                timePeriod = TimePeriod.WEEK;
                presenter.resetLoaderFromStartWithParams(sorting, timePeriod);
                presenter.loadPosts(true /* reset */);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("TOP: WEEK");
                return true;
            case R.id.action_sort_top_month:
                sorting = Sorting.TOP;
                timePeriod = TimePeriod.MONTH;
                presenter.resetLoaderFromStartWithParams(sorting, timePeriod);
                presenter.loadPosts(true /* reset */);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("TOP: MONTH");
                return true;
            case R.id.action_sort_top_year:
                sorting = Sorting.TOP;
                timePeriod = TimePeriod.YEAR;
                presenter.resetLoaderFromStartWithParams(sorting, timePeriod);
                presenter.loadPosts(true /* reset */);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("TOP: YEAR");
                return true;
            case R.id.action_sort_top_all:
                sorting = Sorting.TOP;
                timePeriod = TimePeriod.ALL;
                presenter.resetLoaderFromStartWithParams(sorting, timePeriod);
                presenter.loadPosts(true /* reset */);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("TOP: ALL");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        presenter.loadSubscriberCount();
        presenter.resetLoaderFromStartWithParams(sorting, timePeriod);
        presenter.loadPosts(true /* reset */);
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        swipeRefreshLayout.setRefreshing(active);
    }

    @Override
    public void showPosts(List<CustomSubmission> submissions, boolean clear) {
        if (clear) {
            postsAdapter.setData(submissions);
        } else {
            postsAdapter.addData(submissions);
        }

        // We're coming from a config change, so the state needs to be restored.
        if (listState != null) {
            linearLayoutManager.onRestoreInstanceState(listState);
            listState = null;
        }
    }

    @Override
    public void showPostsLoadingFailedSnackbar(final boolean reset) {
        if (getView() != null) {
            snackbar = Snackbar.make(getView(), R.string.posts_loading_failed,
                    Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(R.string.retry, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (reset) {
                        presenter.resetLoaderFromStartWithParams(sorting, timePeriod);
                        presenter.loadPosts(true /* reset */);
                    } else {
                        presenter.loadPosts(false);
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
    public void changeViewType(int viewType) {
        postsAdapter.setContentViewType(viewType);
        setViewIcon(viewType);
    }

    @Override
    public void scrollToTop() {
        recyclerViewPosts.smoothScrollToPosition(0);
    }

    @Override
    public void resetScrollState() {
        scrollListener.resetState();
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
        bundle.putString(SubmissionActivity.KEY_SUBREDDIT, subreddit);

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

    private void openViewPickerDialog() {
        final MaterialDialog materialDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.change_view)
                .customView(R.layout.view_picker_layout, false)
                .build();

        View view = materialDialog.getCustomView();
        if (view == null) return;

        View viewTypeCard = view.findViewById(R.id.layout_type_card);
        View viewTypeList = view.findViewById(R.id.layout_type_list);

        viewTypeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onViewTypeSelected(Constants.VIEW_CARD);
                materialDialog.dismiss();
            }
        });

        viewTypeList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onViewTypeSelected(Constants.VIEW_LIST);
                materialDialog.dismiss();
            }
        });

        materialDialog.show();
    }

    private void setViewIcon(int viewType) {
        Drawable drawable;
        switch (viewType) {
            case Constants.VIEW_CARD:
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_image_white_24dp);
                break;
            case Constants.VIEW_LIST:
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_view_list_white_24dp);
                break;
            default:
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_image_white_24dp);
                break;
        }
        menu.findItem(R.id.action_change_view).setIcon(drawable);
    }
}
