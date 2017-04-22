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
import com.gmail.jorgegilcavazos.ballislife.features.model.Highlight;
import com.gmail.jorgegilcavazos.ballislife.features.videoplayer.VideoPlayerActivity;
import com.gmail.jorgegilcavazos.ballislife.network.API.HighlightsService;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.SchedulerProvider;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HighlightsFragment extends Fragment implements HighlightsView,
        SwipeRefreshLayout.OnRefreshListener{

    private static final String TAG = "HighlightsFragment";

    public static final String VIEW_TYPE = "viewType";
    public static final int VIEW_CARD = 0;

    @BindView(R.id.swipeRefreshLayout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView_highlights) RecyclerView rvHighlights;

    private int viewType;
    private Unbinder unbinder;
    private HighlightAdapter highlightAdapter;
    private HighlightsPresenter presenter;

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

        rvHighlights.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvHighlights.setAdapter(highlightAdapter);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://nba-app-ca681.firebaseio.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        HighlightsService highlightsService = retrofit.create(HighlightsService.class);

        presenter = new HighlightsPresenter(highlightsService, SchedulerProvider.getInstance());
        presenter.attachView(this);
        presenter.subscribeToHighlightsClick(highlightAdapter.getViewClickObservable());
        presenter.loadHighlights();

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
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        presenter.loadHighlights();
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        swipeRefreshLayout.setRefreshing(active);
    }

    @Override
    public void showHighlights(List<Highlight> highlights) {
        highlightAdapter.setData(highlights);
    }

    @Override
    public void showNoHighlightsAvailable() {
        Toast.makeText(getActivity(), "No highlights to show", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showErrorLoadingHighlights() {
        Toast.makeText(getActivity(), "Error loading highlights", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void openStreamable(String shortcode) {
        Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);
        intent.putExtra(VideoPlayerActivity.SHORTCODE, shortcode);
        startActivity(intent);
    }

    @Override
    public void showErrorOpeningStreamable() {
        Toast.makeText(getActivity(), "Error loading streamable", Toast.LENGTH_SHORT).show();
    }

}
