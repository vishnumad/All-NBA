package com.gmail.jorgegilcavazos.ballislife.features.gamethread;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.features.games.GamesFragment;

import butterknife.BindView;
import butterknife.ButterKnife;


public class CommentsActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {
    private static final String TAG = "CommentsActivity";

    private String homeTeam;
    private String awayTeam;
    private String gameId;
    private long date;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comments_activity);

        Intent intent = getIntent();
        homeTeam = intent.getStringExtra(GamesFragment.GAME_THREAD_HOME);
        awayTeam = intent.getStringExtra(GamesFragment.GAME_THREAD_AWAY);
        gameId = intent.getStringExtra(GamesFragment.GAME_ID);
        date = intent.getLongExtra(GamesFragment.GAME_DATE, -1);

        setTitle(awayTeam + " @ " + homeTeam);

        Bundle bundle = new Bundle();
        bundle.putString(GameThreadFragment.HOME_TEAM_KEY, homeTeam);
        bundle.putString(GameThreadFragment.AWAY_TEAM_KEY, awayTeam);
        bundle.putString(BoxScoreFragment.GAME_ID_KEY, gameId);
        bundle.putLong(GameThreadFragment.GAME_DATE_KEY, date);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(0);
        }

        // Initialize tab layout and add three tabs.
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.game_thread));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.box_score));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.post_game_thread));

        viewPager = (ViewPager) findViewById(R.id.pager);
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(),
                tabLayout.getTabCount(), bundle);
        viewPager.setAdapter(pagerAdapter);
        tabLayout.addOnTabSelectedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());

    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}
