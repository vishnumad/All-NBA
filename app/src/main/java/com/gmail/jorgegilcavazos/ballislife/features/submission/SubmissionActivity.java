package com.gmail.jorgegilcavazos.ballislife.features.submission;

import android.content.Intent;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.features.application.BallIsLifeApplication;
import com.gmail.jorgegilcavazos.ballislife.features.model.wrapper.CustomSubmission;
import com.gmail.jorgegilcavazos.ballislife.features.shared.OnCommentClickListener;
import com.gmail.jorgegilcavazos.ballislife.features.shared.OnSubmissionClickListener;
import com.gmail.jorgegilcavazos.ballislife.features.shared.ThreadAdapter;
import com.gmail.jorgegilcavazos.ballislife.features.videoplayer.VideoPlayerActivity;
import com.gmail.jorgegilcavazos.ballislife.util.Constants;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.CommentSort;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SubmissionActivity extends AppCompatActivity implements SubmissionView,
        SwipeRefreshLayout.OnRefreshListener, OnCommentClickListener, OnSubmissionClickListener,
        View.OnClickListener {
    public static final String KEY_TITLE = "Title";
    public static final String KEY_COMMENT_TO_SCROLL = "CommentToScroll";
    private static final String TAG = "SubmissionActivity";
    @Inject
    RedditAuthentication redditAuthentication;

    @Inject
    SubmissionPresenter presenter;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.swipeRefreshLayout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView_submission) RecyclerView submissionRecyclerView;

    private String threadId;
    private String title;
    private String commentIdToScroll;
    private CustomSubmission customSubmission;

    private ThreadAdapter threadAdapter;

    private CommentSort sorting = CommentSort.TOP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BallIsLifeApplication.getAppComponent().inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setSubtitle("TOP");
        }

        Bundle extras = getIntent().getExtras();
        if (getIntent() != null && getIntent().getExtras() != null) {
            threadId = extras.getString(Constants.THREAD_ID);
            title = extras.getString(KEY_TITLE);
            commentIdToScroll = extras.getString(KEY_COMMENT_TO_SCROLL);
        }

        setTitle(title);

        fab.setOnClickListener(this);
        swipeRefreshLayout.setOnRefreshListener(this);

        threadAdapter = new ThreadAdapter(this, redditAuthentication, new ArrayList<CommentNode>(),
                true);
        threadAdapter.setCommentClickListener(this);
        threadAdapter.setSubmissionClickListener(this);

        submissionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        submissionRecyclerView.setAdapter(threadAdapter);
        submissionRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                if (dy > 0) {
                    fab.hide();
                } else if (dy < 0) {
                    fab.show();
                }
            }
        });

        presenter.attachView(this);
        presenter.loadComments(threadId, sorting, commentIdToScroll);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
        presenter.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_submission, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_sort_hot:
                sorting = CommentSort.HOT;
                presenter.loadComments(threadId, sorting);
                getSupportActionBar().setSubtitle("HOT");
                return true;
            case R.id.action_sort_new:
                sorting = CommentSort.NEW;
                presenter.loadComments(threadId, sorting);
                getSupportActionBar().setSubtitle("NEW");
                return true;
            case R.id.action_sort_old:
                sorting = CommentSort.OLD;
                presenter.loadComments(threadId, sorting);
                getSupportActionBar().setSubtitle("OLD");
                return true;
            case R.id.action_sort_controversial:
                sorting = CommentSort.CONTROVERSIAL;
                presenter.loadComments(threadId, sorting);
                getSupportActionBar().setSubtitle("CONTROVERSIAL");
                return true;
            case R.id.action_sort_top:
                sorting = CommentSort.TOP;
                presenter.loadComments(threadId, sorting);
                getSupportActionBar().setSubtitle("TOP");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        swipeRefreshLayout.setRefreshing(active);
    }

    @Override
    public void showComments(List<CommentNode> commentNodes, Submission submission) {
        threadAdapter.setData(commentNodes);
        threadAdapter.setSubmission(submission);
    }

    @Override
    public void addComment(CommentNode comment, int position) {
        threadAdapter.addComment(position, comment);
    }

    @Override
    public void setCustomSubmission(CustomSubmission customSubmission) {
        this.customSubmission = customSubmission;
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
    public void openReplyToSubmissionDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.add_comment)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                .input(R.string.type_comment, R.string.empty, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (customSubmission != null) {
                            presenter.onReplyToThread(input.toString(),
                                    customSubmission.getSubmission());
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

    @Override
    public void scrollToComment(int index) {
        // Comment i is actually at index i + 1 because of the post card view.
        submissionRecyclerView.scrollToPosition(index + 1);
    }

    @Override
    public void hideFab() {
        fab.hide();
    }

    @Override
    public void showFab() {
        fab.show();
    }

    @Override
    public void onRefresh() {
        presenter.loadComments(threadId, sorting);
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
    public void onUnsaveComment(Comment comment) {
        presenter.onUnsaveComment(comment);
    }

    @Override
    public void onReplyToComment(int position, Comment parentComment) {
        presenter.onReplyToCommentBtnClick(position, parentComment);
    }

    @Override
    public void onSubmissionClick(CustomSubmission customSubmission) {
        // No action on submission click.
    }

    @Override
    public void onVoteSubmission(CustomSubmission customSubmission, VoteDirection voteDirection) {
        if (customSubmission != null) {
            presenter.onVoteSubmission(customSubmission.getSubmission(), voteDirection);
        }
    }

    @Override
    public void onSaveSubmission(CustomSubmission customSubmission, boolean saved) {
        if (customSubmission != null) {
            presenter.onSaveSubmission(customSubmission.getSubmission(), saved);
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
}
