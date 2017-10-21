package com.gmail.jorgegilcavazos.ballislife.features.boxscore;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.data.service.NbaGamesService;
import com.gmail.jorgegilcavazos.ballislife.features.application.BallIsLifeApplication;
import com.gmail.jorgegilcavazos.ballislife.features.gamethread.CommentsActivity;
import com.gmail.jorgegilcavazos.ballislife.features.model.BoxScoreValues;
import com.gmail.jorgegilcavazos.ballislife.features.model.StatLine;
import com.gmail.jorgegilcavazos.ballislife.util.UnitUtils;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider;
import com.google.common.base.Optional;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.gmail.jorgegilcavazos.ballislife.features.gamethread.CommentsActivity
        .AWAY_TEAM_KEY;
import static com.gmail.jorgegilcavazos.ballislife.features.gamethread.CommentsActivity
        .HOME_TEAM_KEY;


public class BoxScoreFragment extends Fragment implements BoxScoreView {
    private static final String TAG = "BoxScoreFragment";
    public static final int LOAD_AWAY = 0;
    public static final int LOAD_HOME = 2;
    @Inject
    BaseSchedulerProvider schedulerProvider;

    @BindView(R.id.button_home) Button btnHome;
    @BindView(R.id.button_away) Button btnAway;
    @BindView(R.id.progressBar) ProgressBar progressBar;
    @BindView(R.id.text_load_message) TextView tvLoadMessage;
    @BindView(R.id.playersTable) TableLayout playersTable;
    @BindView(R.id.statsTable) TableLayout statsTable;
    @BindView(R.id.scrollView) ScrollView scrollView;

    private BoxScorePresenter presenter;
    private Unbinder unbinder;

    private String homeTeam;
    private String awayTeam;
    private String gameId;
    private int teamSelected;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        BallIsLifeApplication.getAppComponent().inject(this);

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

        presenter = new BoxScorePresenter(this, gamesService, schedulerProvider);
        presenter.start();
        presenter.loadBoxScore(gameId, teamSelected);

        return view;
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        presenter.stop();
        super.onDestroyView();
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
        playersTable.removeAllViews();
        statsTable.removeAllViews();

        List<String> players = new ArrayList<>();

        for (StatLine statLine : values.getVls().getPstsg()) {
            // Some players don't have a first name, like Nene.
            if (statLine.getFn() != null && statLine.getFn().length() >= 1) {
                players.add(statLine.getFn().substring(0, 1) + ". " + statLine.getLn());
            } else {
                players.add(statLine.getLn());
            }
        }

        addRowToPlayersTable2("PLAYER");
        for (String player : players) {
            addRowToPlayersTable2(player);
        }

        addRowToStatsTable2(Optional.absent());
        for (StatLine statLine : values.getVls().getPstsg()) {
            addRowToStatsTable2(Optional.of(statLine));
        }

        scrollView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showHomeBoxScore(BoxScoreValues values) {
        playersTable.removeAllViews();
        statsTable.removeAllViews();

        List<String> players = new ArrayList<>();

        for (StatLine statLine : values.getHls().getPstsg()) {
            // Some players don't have a first name, like Nene.
            if (statLine.getFn() != null && statLine.getFn().length() >= 1) {
                players.add(statLine.getFn().substring(0, 1) + ". " + statLine.getLn());
            } else {
                players.add(statLine.getLn());
            }
        }

        addRowToPlayersTable2("PLAYER");
        for (String player : players) {
            addRowToPlayersTable2(player);
        }

        addRowToStatsTable2(Optional.absent());
        for (StatLine statLine : values.getHls().getPstsg()) {
            addRowToStatsTable2(Optional.of(statLine));
        }

        scrollView.setVisibility(View.VISIBLE);
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
        scrollView.setVisibility(View.GONE);
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

    public void addRowToPlayersTable2(String content) {
        TableRow row = new TableRow(getActivity());
        row.setBackgroundColor(Color.WHITE);
        row.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        int width = (int) UnitUtils.convertDpToPixel(100, getActivity());
        row.setMinimumWidth(width);

        if (content.equals("PLAYER")) {
            TextView tv = addHeaderItem(row, content);
            tv.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            row.addView(tv);
        } else {
            TextView tv = addNormalItem(row, content);
            tv.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            row.addView(tv);
        }

        playersTable.addView(row, new TableLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
    }

    public void addRowToStatsTable2(Optional<StatLine> statLineOptional) {
        TableRow row = new TableRow(getActivity());
        row.setBackgroundColor(Color.WHITE);
        row.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        if (statLineOptional.isPresent()) {
            StatLine statLine = statLineOptional.get();

            row.addView(addNormalItem(row, String.valueOf(statLine.getMin())));
            row.addView(addNormalItem(row, String.valueOf(statLine.getPts())));
            row.addView(addNormalItem(row, String.valueOf(statLine.getReb())));
            row.addView(addNormalItem(row, String.valueOf(statLine.getAst())));
            row.addView(addNormalItem(row, String.valueOf(statLine.getStl())));
            row.addView(addNormalItem(row, String.valueOf(statLine.getBlk())));
            row.addView(addNormalItem(row, String.valueOf(statLine.getPf())));
            row.addView(addNormalItem(row, String.valueOf(statLine.getTov())));
        } else {
            row.addView(addHeaderItem(row, "MIN"));
            row.addView(addHeaderItem(row, "PTS"));
            row.addView(addHeaderItem(row, "REB"));
            row.addView(addHeaderItem(row, "AST"));
            row.addView(addHeaderItem(row, "STL"));
            row.addView(addHeaderItem(row, "BLK"));
            row.addView(addHeaderItem(row, "PF"));
            row.addView(addHeaderItem(row, "TO"));
        }

        statsTable.addView(row, new TableLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
    }

    private TextView addHeaderItem(TableRow row, String text) {
        TextView view = (TextView) LayoutInflater.from(getActivity())
                .inflate(R.layout.boxscore_item, row, false);
        view.setText(text);
        view.setTypeface(null, Typeface.BOLD);
        view.setMinWidth((int) UnitUtils.convertDpToPixel(30, getActivity()));
        return view;
    }

    private TextView addNormalItem(TableRow row, String text) {
        TextView view = (TextView) LayoutInflater.from(getActivity())
                .inflate(R.layout.boxscore_item, row, false);
        view.setText(text);
        view.setTypeface(null, Typeface.NORMAL);
        view.setMinWidth((int) UnitUtils.convertDpToPixel(30, getActivity()));
        return view;
    }
}
