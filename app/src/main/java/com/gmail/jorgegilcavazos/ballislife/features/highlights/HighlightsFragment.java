package com.gmail.jorgegilcavazos.ballislife.features.highlights;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.data.HighlightsRepository;
import com.gmail.jorgegilcavazos.ballislife.data.HighlightsRepositoryImpl;
import com.gmail.jorgegilcavazos.ballislife.features.model.Highlight;
import com.gmail.jorgegilcavazos.ballislife.features.shared.EndlessRecyclerViewScrollListener;
import com.gmail.jorgegilcavazos.ballislife.features.videoplayer.VideoPlayerActivity;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.SchedulerProvider;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HighlightsFragment extends Fragment implements HighlightsView,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "HighlightsFragment";

    public static final String VIEW_TYPE = "viewType";
    public static final int VIEW_CARD = 0;

    @BindView(R.id.swipeRefreshLayout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView_highlights) RecyclerView rvHighlights;

    private int viewType;
    private Unbinder unbinder;
    private HighlightAdapter highlightAdapter;
    private HighlightsPresenter presenter;
    private EndlessRecyclerViewScrollListener scrollListener;

    public HighlightsFragment() {
        // Required empty public constructor
    }

    public static HighlightsFragment newInstance(int viewType) {
        HighlightsFragment fragment = new HighlightsFragment();
        Bundle args = new Bundle();
        args.putInt(VIEW_TYPE, viewType);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            viewType = getArguments().getInt(VIEW_TYPE);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_highlights, container, false);
        unbinder = ButterKnife.bind(this, view);

        swipeRefreshLayout.setOnRefreshListener(this);

        highlightAdapter = new HighlightAdapter(getActivity(), new ArrayList<Highlight>(25));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        rvHighlights.setLayoutManager(linearLayoutManager);
        rvHighlights.setAdapter(highlightAdapter);

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                presenter.loadHighlights(false);
            }
        };

        rvHighlights.addOnScrollListener(scrollListener);

        HighlightsRepository highlightsRepository = new HighlightsRepositoryImpl(10);

        presenter = new HighlightsPresenter(highlightsRepository, SchedulerProvider.getInstance());
        presenter.attachView(this);
        presenter.subscribeToHighlightsClick(highlightAdapter.getViewClickObservable());
        presenter.subscribeToHighlightsShare(highlightAdapter.getShareClickObservable());
        presenter.loadHighlights(true);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        presenter.stop();
        presenter.detachView();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                presenter.loadHighlights(true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        presenter.loadHighlights(true);
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        swipeRefreshLayout.setRefreshing(active);
    }

    @Override
    public void showHighlights(List<Highlight> highlights, boolean clear) {
        if (clear) {
            highlightAdapter.setData(highlights);
        } else {
            highlightAdapter.addData(highlights);
        }
    }

    @Override
    public void showNoHighlightsAvailable() {
        Toast.makeText(getActivity(), R.string.no_highlights_to_show, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showErrorLoadingHighlights() {
        Toast.makeText(getActivity(), R.string.error_loading_highlights, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void openStreamable(String shortcode) {
        Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);
        intent.putExtra(VideoPlayerActivity.SHORTCODE, shortcode);
        startActivity(intent);
    }

    @Override
    public void showErrorOpeningStreamable() {
        Toast.makeText(getActivity(), R.string.error_loading_streamable, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void resetScrollState() {
        scrollListener.resetState();
    }

    @Override
    public void shareHighlight(Highlight highlight) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, highlight.getUrl());
        startActivity(Intent.createChooser(shareIntent,
                getResources().getString(R.string.share_video)));
    }

}
