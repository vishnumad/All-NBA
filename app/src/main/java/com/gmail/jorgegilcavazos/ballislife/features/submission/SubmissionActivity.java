package com.gmail.jorgegilcavazos.ballislife.features.submission;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gmail.jorgegilcavazos.ballislife.features.model.Streamable;
import com.gmail.jorgegilcavazos.ballislife.features.model.wrapper.CustomSubmission;
import com.gmail.jorgegilcavazos.ballislife.features.shared.OnCommentClickListener;
import com.gmail.jorgegilcavazos.ballislife.features.shared.OnSubmissionClickListener;
import com.gmail.jorgegilcavazos.ballislife.features.shared.ThreadAdapter;
import com.gmail.jorgegilcavazos.ballislife.features.videoplayer.VideoPlayerActivity;
import com.gmail.jorgegilcavazos.ballislife.network.API.RedditService;
import com.gmail.jorgegilcavazos.ballislife.util.Constants;
import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.SchedulerProvider;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.gmail.jorgegilcavazos.ballislife.network.RedditAuthentication.REDDIT_AUTH_PREFS;

public class SubmissionActivity extends AppCompatActivity implements SubmissionView,
        SwipeRefreshLayout.OnRefreshListener, OnCommentClickListener, OnSubmissionClickListener,
        View.OnClickListener {
    private static final String TAG = "SubmissionActivity";

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.swipeRefreshLayout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView_submission) RecyclerView submissionRecyclerView;

    private String threadId;

    private CustomSubmission customSubmission;
    private ThreadAdapter threadAdapter;
    private SubmissionPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setTitle(getString(R.string.rnba));

        Bundle extras = getIntent().getExtras();
        if (getIntent() != null && getIntent().getExtras() != null) {
            threadId = extras.getString(Constants.THREAD_ID);
            customSubmission = (CustomSubmission) extras
                    .getSerializable(Constants.THREAD_SUBMISSION);
        }

        fab.setOnClickListener(this);
        swipeRefreshLayout.setOnRefreshListener(this);

        threadAdapter = new ThreadAdapter(this, new ArrayList<CommentNode>(), true);
        threadAdapter.setCommentClickListener(this);
        threadAdapter.setSubmissionClickListener(this);
        threadAdapter.setCustomSubmission(customSubmission);

        submissionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        submissionRecyclerView.setAdapter(threadAdapter);

        SharedPreferences preferences = getSharedPreferences(REDDIT_AUTH_PREFS, MODE_PRIVATE);

        presenter = new SubmissionPresenter(new RedditService(), preferences,
                SchedulerProvider.getInstance());
        presenter.attachView(this);
        presenter.loadComments(threadId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
        presenter.stop();
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        swipeRefreshLayout.setRefreshing(active);
    }

    @Override
    public void showComments(List<CommentNode> commentNodes, Submission submission) {
        threadAdapter.swap(commentNodes);
        threadAdapter.setSubmission(submission);
    }

    @Override
    public void addComment(CommentNode comment, int position) {
        threadAdapter.addComment(position, comment);
    }

    @Override
    public void showErrorAddingComment() {
        Toast.makeText(this, R.string.saving_failed, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showNotLoggedInError() {
        Toast.makeText(this, R.string.not_logged_in, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSavingToast() {
        Toast.makeText(this, R.string.saving, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSavedToast() {
        Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showErrorSavingToast() {
        Toast.makeText(this, R.string.saving_failed, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRefresh() {
        presenter.loadComments(threadId);
    }

    @Override
    public void onVoteComment(Comment comment, VoteDirection voteDirection) {
        presenter.onVoteComment(comment, voteDirection);
    }

    @Override
    public void onSaveComment(Comment comment) {
        presenter.onSaveComment(comment);
    }


    @Override
    public void onReplyToComment(int position, Comment parentComment) {
        presenter.onReplyToCommentBtnClick(position, parentComment);
    }

    @Override
    public void openReplyToCommentDialog(final int position, final Comment parentComment) {
        new MaterialDialog.Builder(this)
                .title(R.string.add_comment)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                .input(R.string.type_comment, R.string.empty, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        presenter.onReplyToComment(position, parentComment, input.toString());
                    }
                })
                .positiveText(R.string.reply)
                .negativeText(R.string.cancel)
                .show();
    }

    @Override
    public void onSubmissionClick(Submission submission) {
        // No action on submission click.
    }

    @Override
    public void onVoteSubmission(Submission submission, VoteDirection voteDirection) {
        if (submission != null) {
            presenter.onVoteSubmission(submission, voteDirection);
        }
    }

    @Override
    public void onSaveSubmission(Submission submission, boolean saved) {
        if (submission != null) {
            presenter.onSaveSubmission(submission, saved);
        }
    }

    @Override
    public void onContentClick(String url) {
        presenter.onContentClick(url);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                onReplyToThread();
                break;
        }
    }

    public void onReplyToThread() {
        presenter.onReplyToThreadBtnClick();
    }

    @Override
    public void openReplyToSubmissionDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.add_comment)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                .input(R.string.type_comment, R.string.empty, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (customSubmission.getSubmission() != null) {
                            presenter.onReplyToThread(input.toString(), customSubmission.getSubmission());
                        }
                    }
                })
                .positiveText(R.string.reply)
                .negativeText(R.string.cancel)
                .show();
    }

    @Override
    public void openContentTab(String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(this, Uri.parse(url));
    }

    @Override
    public void openStreamable(String shortcode) {
        Intent intent = new Intent(SubmissionActivity.this, VideoPlayerActivity.class);
        intent.putExtra(VideoPlayerActivity.SHORTCODE, shortcode);
        startActivity(intent);
    }

    @Override
    public void showContentUnavailableToast() {
        Toast.makeText(this, R.string.content_not_available, Toast.LENGTH_SHORT).show();
    }
}
