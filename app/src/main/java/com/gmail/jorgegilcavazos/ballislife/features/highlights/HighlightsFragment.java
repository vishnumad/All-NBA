package com.gmail.jorgegilcavazos.ballislife.features.highlights;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.gmail.jorgegilcavazos.ballislife.data.repository.highlights.HighlightsRepository;
import com.gmail.jorgegilcavazos.ballislife.features.application.BallIsLifeApplication;
import com.gmail.jorgegilcavazos.ballislife.features.model.Highlight;
import com.gmail.jorgegilcavazos.ballislife.features.shared.EndlessRecyclerViewScrollListener;
import com.gmail.jorgegilcavazos.ballislife.features.videoplayer.VideoPlayerActivity;
import com.gmail.jorgegilcavazos.ballislife.util.Constants;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HighlightsFragment extends Fragment implements HighlightsView,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "HighlightsFragment";

    private static final String LIST_STATE = "listState";

    @Inject
    LocalRepository localRepository;

    @Inject
    HighlightsRepository highlightsRepository;

    @Inject
    BaseSchedulerProvider schedulerProvider;

    @BindView(R.id.swipeRefreshLayout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView_highlights) RecyclerView rvHighlights;
    Parcelable listState;
    private int viewType;
    private Unbinder unbinder;
    private HighlightAdapter highlightAdapter;
    private HighlightsPresenter presenter;
    private LinearLayoutManager linearLayoutManager;
    private EndlessRecyclerViewScrollListener scrollListener;
    private Menu menu;

    public HighlightsFragment() {
        // Required empty public constructor.
    }

    public static HighlightsFragment newInstance() {
        HighlightsFragment fragment = new HighlightsFragment();
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BallIsLifeApplication.getAppComponent().inject(this);

        if (localRepository.getFavoriteHighlightViewType() != -1) {
            viewType = localRepository.getFavoriteHighlightViewType();
        } else {
            viewType = Constants.HIGHLIGHTS_VIEW_SMALL;
        }

        linearLayoutManager = new LinearLayoutManager(getActivity());
        highlightAdapter = new HighlightAdapter(getActivity(), new ArrayList<Highlight>(25), viewType);

        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load cached data if available, or from network if not.
        presenter.loadFirstAvailable();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_highlights, container, false);
        unbinder = ButterKnife.bind(this, view);

        swipeRefreshLayout.setOnRefreshListener(this);

        rvHighlights.setLayoutManager(linearLayoutManager);
        rvHighlights.setAdapter(highlightAdapter);

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                presenter.loadHighlights(false /* reset */);
            }
        };

        rvHighlights.addOnScrollListener(scrollListener);

        presenter = new HighlightsPresenter(highlightsRepository, localRepository,
                schedulerProvider);
        presenter.attachView(this);
        presenter.subscribeToHighlightsClick(highlightAdapter.getViewClickObservable());
        presenter.subscribeToHighlightsShare(highlightAdapter.getShareClickObservable());

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        presenter.stop();
        presenter.detachView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_highlights, menu);
        this.menu = menu;
        super.onCreateOptionsMenu(menu, inflater);

        setViewIcon(viewType);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                presenter.loadHighlights(true);
                return true;
            case R.id.action_change_view:
                openViewPickerDialog();
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

        // We're coming from a config change, so the state needs to be restored.
        if (listState != null) {
            linearLayoutManager.onRestoreInstanceState(listState);
            listState = null;
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

    @Override
    public void changeViewType(int viewType) {
        highlightAdapter.setContentViewType(viewType);
        setViewIcon(viewType);
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
                presenter.onViewTypeSelected(Constants.HIGHLIGHTS_VIEW_LARGE);
                materialDialog.dismiss();
            }
        });

        viewTypeList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onViewTypeSelected(Constants.HIGHLIGHTS_VIEW_SMALL);
                materialDialog.dismiss();
            }
        });

        materialDialog.show();
    }

    private void setViewIcon(int viewType) {
        Drawable drawable;
        switch (viewType) {
            case Constants.HIGHLIGHTS_VIEW_LARGE:
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_image_white_24dp);
                break;
            case Constants.HIGHLIGHTS_VIEW_SMALL:
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_view_list_white_24dp);
                break;
            default:
                throw new IllegalStateException("Highlight view icon neither small nor large");
        }
        menu.findItem(R.id.action_change_view).setIcon(drawable);
    }
}
