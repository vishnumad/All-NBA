package com.gmail.jorgegilcavazos.ballislife.features.profile;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.data.API.RedditService;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.SchedulerProvider;

import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Listing;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;

import static com.gmail.jorgegilcavazos.ballislife.data.RedditAuthentication.REDDIT_AUTH_PREFS;

public class ProfileActivity extends AppCompatActivity
        implements ProfileView, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "ProfileActivity";

    @BindView(R.id.profile_coordinator_layout) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.profile_toolbar) Toolbar toolbar;
    @BindView(R.id.profile_swipe_refresh) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.profile_recycler_view) RecyclerView recyclerView;

    private RecyclerView.LayoutManager layoutManager;
    private ContributionsAdapter contributionsAdapter;
    private Snackbar snackbar;

    private ProfilePresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setTitle("Profile");

        swipeRefreshLayout.setOnRefreshListener(this);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        contributionsAdapter = new ContributionsAdapter(this, new ArrayList<Contribution>());
        recyclerView.setAdapter(contributionsAdapter);

        presenter = new ProfilePresenter(new RedditService(),
                getSharedPreferences(REDDIT_AUTH_PREFS, MODE_PRIVATE),
                new CompositeDisposable(),
                SchedulerProvider.getInstance());
        presenter.attachView(this);
        presenter.loadUserDetails();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.stop();
        presenter.detachView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_refresh:
                presenter.loadUserDetails();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        presenter.loadUserDetails();
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        swipeRefreshLayout.setRefreshing(active);
    }

    @Override
    public void showContent(Listing<Contribution> contributions) {
        contributionsAdapter.addData(contributions);
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideContent() {
        recyclerView.setVisibility(View.GONE);
    }

    @Override
    public void setToolbarTitle(String title) {
        toolbar.setTitle(title);
    }

    @Override
    public void showSnackbar(boolean canReload) {
        snackbar = Snackbar.make(coordinatorLayout, R.string.failed_profile_data,
                Snackbar.LENGTH_INDEFINITE);
        if (canReload) {
            snackbar.setAction(R.string.retry, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    presenter.loadUserDetails();
                }
            });
        }
        snackbar.show();
    }

    @Override
    public void dismissSnackbar() {
        if (snackbar != null) {
            snackbar.dismiss();
        }
    }
}
