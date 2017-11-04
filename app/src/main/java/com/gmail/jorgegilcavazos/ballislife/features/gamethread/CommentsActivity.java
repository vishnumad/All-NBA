package com.gmail.jorgegilcavazos.ballislife.features.gamethread;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.data.local.LocalRepository;
import com.gmail.jorgegilcavazos.ballislife.features.application.BallIsLifeApplication;
import com.gmail.jorgegilcavazos.ballislife.features.games.GamesFragment;
import com.gmail.jorgegilcavazos.ballislife.features.main.BaseNoActionBarActivity;
import com.gmail.jorgegilcavazos.ballislife.features.model.NbaGame;
import com.google.firebase.crash.FirebaseCrash;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.gmail.jorgegilcavazos.ballislife.features.gamethread.PagerAdapter.BOX_SCORE_TAB;
import static com.gmail.jorgegilcavazos.ballislife.features.gamethread.PagerAdapter.POST_GAME_TAB;


public class CommentsActivity extends BaseNoActionBarActivity implements TabLayout
        .OnTabSelectedListener, ViewPager.OnPageChangeListener, View.OnClickListener,
        BillingProcessor.IBillingHandler {
    public static final String GAME_ID_KEY = "gameId";
    public static final String HOME_TEAM_KEY = "homeTeamKey";
    public static final String AWAY_TEAM_KEY = "awayTeamKey";

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tabLayout) TabLayout tabLayout;
    @BindView(R.id.pager) ViewPager viewPager;
    @BindView(R.id.fab) FloatingActionButton fab;

    @Inject
    LocalRepository localRepository;

    private PagerAdapter pagerAdapter;
    public BillingProcessor billingProcessor;

    @Override
    public void injectAppComponent() {
        BallIsLifeApplication.getAppComponent().inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comments_activity);
        ButterKnife.bind(this);

        String billingLicense = getString(R.string.play_billing_license_key);
        billingProcessor = new BillingProcessor(this, billingLicense, this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        String homeTeam = intent.getStringExtra(GamesFragment.GAME_THREAD_HOME);
        String awayTeam = intent.getStringExtra(GamesFragment.GAME_THREAD_AWAY);
        String gameId = intent.getStringExtra(GamesFragment.GAME_ID);
        long date = intent.getLongExtra(GamesFragment.GAME_DATE, -1);

        setTitle(awayTeam + " @ " + homeTeam);

        Bundle bundle = new Bundle();
        bundle.putString(HOME_TEAM_KEY, homeTeam);
        bundle.putString(AWAY_TEAM_KEY, awayTeam);
        bundle.putString(GAME_ID_KEY, gameId);
        bundle.putLong(GameThreadFragment.GAME_DATE_KEY, date);

        // Initialize tab layout and add three tabs.
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.game_thread));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.box_score));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.post_game_thread));
        fab.hide();

        pagerAdapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount(), bundle);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        viewPager.addOnPageChangeListener(this);
        tabLayout.addOnTabSelectedListener(this);

        setSelectedTab(intent.getStringExtra(GamesFragment.GAME_STATUS));

        fab.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
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
        switch (tab.getPosition()) {
            case 1:
                fab.hide();
                expandToolbar();
                break;
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                addComment();
                break;
        }
    }

    private void addComment() {
        int pos = viewPager.getCurrentItem();
        Fragment fragment = pagerAdapter.getItem(pos);

        if (pos == 0 || pos == 2) {
            GameThreadFragment gameThreadFragment = ((GameThreadFragment) fragment);
            gameThreadFragment.fabClicked();
        }
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        Toast.makeText(this, R.string.purchase_complete, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        FirebaseCrash.log("Billing error code: " + errorCode);
        FirebaseCrash.report(error);
    }

    @Override
    public void onBillingInitialized() {

    }

    private void expandToolbar() {
        if (toolbar.getParent() instanceof AppBarLayout) {
            ((AppBarLayout) toolbar.getParent()).setExpanded(true, true);
        }
    }

    public void showFab() {
        if (viewPager.getCurrentItem() != 1) {
            fab.show();
        }
    }

    public void hideFab() {
        fab.hide();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        tabLayout.setScrollPosition(position, 0f, true);
        switch (position) {
            case 1:
                fab.hide();
                expandToolbar();
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void setSelectedTab(String gameStatus) {
        if (localRepository.getOpenBoxScoreByDefault()) {
            TabLayout.Tab boxScoreTab = tabLayout.getTabAt(BOX_SCORE_TAB);
            if (boxScoreTab != null) {
                boxScoreTab.select();
            } else {
                viewPager.setCurrentItem(BOX_SCORE_TAB);
            }
        } else if (gameStatus.equals(NbaGame.POST_GAME)) {
            TabLayout.Tab postGameTab = tabLayout.getTabAt(POST_GAME_TAB);
            if (postGameTab != null) {
                postGameTab.select();
            } else {
                viewPager.setCurrentItem(POST_GAME_TAB);
            }
        }
    }
}
