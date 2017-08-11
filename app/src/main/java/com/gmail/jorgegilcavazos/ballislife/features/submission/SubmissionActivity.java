package com.gmail.jorgegilcavazos.ballislife.features.submission;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.features.application.BallIsLifeApplication;
import com.gmail.jorgegilcavazos.ballislife.features.common.OnCommentClickListener;
import com.gmail.jorgegilcavazos.ballislife.features.common.OnSubmissionClickListener;
import com.gmail.jorgegilcavazos.ballislife.features.common.ThreadAdapter;
import com.gmail.jorgegilcavazos.ballislife.features.model.SubmissionWrapper;
import com.gmail.jorgegilcavazos.ballislife.features.reply.ReplyActivity;
import com.gmail.jorgegilcavazos.ballislife.features.videoplayer.VideoPlayerActivity;
import com.gmail.jorgegilcavazos.ballislife.util.Constants;
import com.gmail.jorgegilcavazos.ballislife.util.RedditUtils;

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
    private static final String TAG = "SubmissionActivity";
    private static final String KEY_COMMENT_TO_REPLY_POS = "CommentToReplyPos";
    private static final String KEY_COMMENT_TO_REPLY_FULL_NAME = "CommentToReplyFullName";
    public static final String KEY_TITLE = "Title";
    public static final String KEY_COMMENT_TO_SCROLL = "CommentToScroll";
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

    private int commentToReplyToPos = -1;
    private String commentToReplyToFullName;

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

        threadAdapter = new ThreadAdapter(this, redditAuthentication, new ArrayList<>(), true);
        threadAdapter.setCommentClickListener(this);
        threadAdapter.setSubmissionClickListener(this);

        submissionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        submissionRecyclerView.setAdapter(threadAdapter);
        submissionRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        if (savedInstanceState != null) {
            commentToReplyToPos = savedInstanceState.getInt(KEY_COMMENT_TO_REPLY_POS);
            commentToReplyToFullName = savedInstanceState.getString(KEY_COMMENT_TO_REPLY_FULL_NAME);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
        presenter.stop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_COMMENT_TO_REPLY_POS, commentToReplyToPos);
        outState.putString(KEY_COMMENT_TO_REPLY_FULL_NAME, commentToReplyToFullName);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ReplyActivity.POST_COMMENT_REPLY_REQUEST && resultCode == RESULT_OK) {
            if (commentToReplyToPos == -1 || commentToReplyToFullName == null) {
                throw new IllegalStateException("Invalid reply pos or fullname");
            }
            presenter.onReplyToComment(
                    commentToReplyToPos, threadId, commentToReplyToFullName,
                    data.getStringExtra(ReplyActivity.KEY_POSTED_COMMENT));
        } else if (requestCode == ReplyActivity.POST_SUBMISSION_REPLY_REQUEST
                && resultCode == RESULT_OK) {
            presenter.onReplyToThread(data.getStringExtra(ReplyActivity.KEY_POSTED_COMMENT),
                    threadId);
        }
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
    public void openReplyToCommentActivity(final int position, final Comment parentComment) {
        commentToReplyToFullName = parentComment.getFullName();
        commentToReplyToPos = position;

        Intent intent = new Intent(SubmissionActivity.this, ReplyActivity.class);
        Bundle extras = new Bundle();
        extras.putCharSequence(ReplyActivity.KEY_COMMENT,
                RedditUtils.bindSnuDown(parentComment.data("body_html")));
        intent.putExtras(extras);
        startActivityForResult(intent, ReplyActivity.POST_COMMENT_REPLY_REQUEST);
    }

    @Override
    public void openReplyToSubmissionActivity() {
        Intent intent = new Intent(SubmissionActivity.this, ReplyActivity.class);
        startActivityForResult(intent, ReplyActivity.POST_SUBMISSION_REPLY_REQUEST);
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
    public void onSubmissionClick(SubmissionWrapper submissionWrapper) {
        // No action on submission click.
    }

    @Override
    public void onVoteSubmission(SubmissionWrapper submissionWrapper, VoteDirection voteDirection) {
        if (submissionWrapper != null) {
            presenter.onVoteSubmission(submissionWrapper.getSubmission(), voteDirection);
        }
    }

    @Override
    public void onSaveSubmission(SubmissionWrapper submissionWrapper, boolean saved) {
        if (submissionWrapper != null) {
            presenter.onSaveSubmission(submissionWrapper.getSubmission(), saved);
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
                presenter.onReplyToThreadBtnClick();
                break;
        }
    }
}
