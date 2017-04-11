package com.gmail.jorgegilcavazos.ballislife.features.gamethread;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.features.model.BoxScoreValues;
import com.gmail.jorgegilcavazos.ballislife.features.model.StatLine;
import com.gmail.jorgegilcavazos.ballislife.network.API.NbaGamesService;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.SchedulerProvider;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.gmail.jorgegilcavazos.ballislife.features.gamethread.CommentsActivity.AWAY_TEAM_KEY;
import static com.gmail.jorgegilcavazos.ballislife.features.gamethread.CommentsActivity.HOME_TEAM_KEY;


public class BoxScoreFragment extends Fragment implements BoxScoreView {

    private static final String TAG = "BoxScoreFragment";

    public static final int LOAD_AWAY = 0;
    public static final int LOAD_HOME = 2;

    @BindView(R.id.button_home) Button btnHome;
    @BindView(R.id.button_away) Button btnAway;
    @BindView(R.id.rv_players) RecyclerView recyclerViewPlayers;
    @BindView(R.id.rv_stats) RecyclerView recyclerViewStats;
    @BindView(R.id.progressBar) ProgressBar progressBar;
    @BindView(R.id.layout_boxscore) LinearLayout layoutBoxScore;
    @BindView(R.id.text_load_message) TextView tvLoadMessage;

    private BoxScorePresenter presenter;
    private PlayerAdapter playerAdapter;
    private StatLineAdapter statLineAdapter;
    private Unbinder unbinder;

    private String homeTeam;
    private String awayTeam;
    private String gameId;
    private int teamSelected;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            homeTeam = getArguments().getString(HOME_TEAM_KEY);
            awayTeam = getArguments().getString(AWAY_TEAM_KEY);
            gameId = getArguments().getString(CommentsActivity.GAME_ID_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_box_score, container, false);
        unbinder = ButterKnife.bind(this, view);

        teamSelected = LOAD_HOME;

        btnAway.setText(awayTeam);
        btnHome.setText(homeTeam);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://nba-app-ca681.firebaseio.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        NbaGamesService gamesService = retrofit.create(NbaGamesService.class);

        playerAdapter = new PlayerAdapter(new ArrayList<String>());
        recyclerViewPlayers.setLayoutManager(new CustomLayoutMaganer(getActivity()));
        recyclerViewPlayers.setAdapter(playerAdapter);
        recyclerViewPlayers.setNestedScrollingEnabled(false);

        statLineAdapter = new StatLineAdapter(new ArrayList<StatLine>());
        recyclerViewStats.setLayoutManager(new CustomLayoutMaganer(getActivity()));
        recyclerViewStats.setAdapter(statLineAdapter);

        presenter = new BoxScorePresenter(this, gamesService, SchedulerProvider.getInstance());
        presenter.start();
        presenter.loadBoxScore(gameId, teamSelected);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        presenter.stop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                presenter.loadBoxScore(gameId, teamSelected);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.button_away)
    public void onButtonAwayClick() {
        btnAway.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.square_black));
        btnAway.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
        btnHome.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.square_white));
        btnHome.setTextColor(ContextCompat.getColor(getActivity(), R.color.black));

        teamSelected = LOAD_AWAY;
        presenter.loadBoxScore(gameId, teamSelected);
    }

    @OnClick(R.id.button_home)
    public void onButtonHomeClick() {
        btnHome.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.square_black));
        btnHome.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
        btnAway.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.square_white));
        btnAway.setTextColor(ContextCompat.getColor(getActivity(), R.color.black));

        teamSelected = LOAD_HOME;
        presenter.loadBoxScore(gameId, teamSelected);
    }

    @Override
    public void showVisitorBoxScore(BoxScoreValues values) {
        List<String> players = new ArrayList<>();

        for (StatLine statLine : values.getVls().getPstsg()) {
            players.add(statLine.getFn().substring(0, 1) + ". " + statLine.getLn());
        }

        playerAdapter.setData(players);
        statLineAdapter.setData(values.getHls().getPstsg());

        layoutBoxScore.setVisibility(View.VISIBLE);
    }

    @Override
    public void showHomeBoxScore(BoxScoreValues values) {
        List<String> players = new ArrayList<>();

        for (StatLine statLine : values.getHls().getPstsg()) {
            players.add(statLine.getFn().substring(0, 1) + ". " + statLine.getLn());
        }

        playerAdapter.setData(players);
        statLineAdapter.setData(values.getHls().getPstsg());

        layoutBoxScore.setVisibility(View.VISIBLE);
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        if (active) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void hideBoxScore() {
        layoutBoxScore.setVisibility(View.GONE);
    }

    @Override
    public void showLoadingBoxScoreErrorMessage() {
        tvLoadMessage.setText(R.string.failed_to_load_box_score);
        tvLoadMessage.setVisibility(View.VISIBLE);
    }

    @Override
    public void showBoxScoreNotAvailableMessage() {
        tvLoadMessage.setText(R.string.box_score_not_available);
        tvLoadMessage.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoadMessage() {
        tvLoadMessage.setVisibility(View.GONE);
    }

    public class CustomLayoutMaganer extends LinearLayoutManager {

        private boolean isScrollEnabled = false;

        public CustomLayoutMaganer(Context context) {
            super(context);
        }

        @Override
        public boolean canScrollVertically() {
            return isScrollEnabled && super.canScrollVertically();
        }
    }
}
