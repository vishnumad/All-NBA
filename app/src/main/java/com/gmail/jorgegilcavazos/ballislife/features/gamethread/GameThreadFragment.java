package com.gmail.jorgegilcavazos.ballislife.features.gamethread;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.features.application.BallIsLifeApplication;
import com.gmail.jorgegilcavazos.ballislife.features.common.OnCommentClickListener;
import com.gmail.jorgegilcavazos.ballislife.features.common.ThreadAdapter;
import com.gmail.jorgegilcavazos.ballislife.features.model.ThreadItem;
import com.gmail.jorgegilcavazos.ballislife.features.reply.ReplyActivity;
import com.gmail.jorgegilcavazos.ballislife.util.RedditUtils;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.VoteDirection;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;
import static com.gmail.jorgegilcavazos.ballislife.features.gamethread.CommentsActivity
        .AWAY_TEAM_KEY;
import static com.gmail.jorgegilcavazos.ballislife.features.gamethread.CommentsActivity
        .HOME_TEAM_KEY;

public class GameThreadFragment extends Fragment
        implements GameThreadView,
        SwipeRefreshLayout.OnRefreshListener,
        OnCommentClickListener,
        CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "GameThreadFragment";
    private static final String KEY_COMMENT_TO_REPLY_POS = "CommentToReplyPos";
    private static final String KEY_COMMENT_TO_REPLY_FULL_NAME = "CommentToReplyFullName";
    private static final String KEY_SUBMISSION_ID = "SubmissionId";
    public static final String THREAD_TYPE_KEY = "THREAD_TYPE";
    public static final String GAME_DATE_KEY = "GAME_DATE";
    public boolean isPremium = false;

    @Inject GameThreadPresenter presenter;
    @Inject RedditAuthentication redditAuthentication;

    @BindView(R.id.game_thread_swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.comment_thread_rv) RecyclerView rvComments;
    @BindView(R.id.text_message) TextView tvMessage;

    private RecyclerView.LayoutManager lmComments;
    private ThreadAdapter threadAdapter;
    private Unbinder unbinder;
    private String homeTeam, awayTeam, threadType;
    private long gameDate;
    private boolean stream = false;
    private Switch streamSwitch;

    private int commentToReplyToPos = -1;
    private String commentToReplyToFullName;
    private String submissionId;

    public GameThreadFragment() {
        // Required empty public constructor.
    }

    public static GameThreadFragment newInstance() {
        return new GameThreadFragment();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ReplyActivity.POST_COMMENT_REPLY_REQUEST && resultCode == RESULT_OK) {
            if (commentToReplyToPos == -1 || commentToReplyToFullName == null) {
                throw new IllegalStateException("Invalid reply pos or fullname");
            }
            presenter.reply(commentToReplyToPos, submissionId, commentToReplyToFullName, data
                    .getStringExtra(ReplyActivity.KEY_POSTED_COMMENT));
        } else if (requestCode == ReplyActivity.POST_SUBMISSION_REPLY_REQUEST && resultCode ==
                RESULT_OK) {
            presenter.replyToThread(data.getStringExtra(ReplyActivity.KEY_POSTED_COMMENT),
                    submissionId);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        BallIsLifeApplication.getAppComponent().inject(this);
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        if (getArguments() != null) {
            homeTeam = getArguments().getString(HOME_TEAM_KEY);
            awayTeam = getArguments().getString(AWAY_TEAM_KEY);
            threadType = getArguments().getString(THREAD_TYPE_KEY);
            gameDate = getArguments().getLong(GAME_DATE_KEY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_thread, container, false);
        unbinder = ButterKnife.bind(this, view);

        isPremium = ((CommentsActivity) getActivity()).billingProcessor.isPurchased("premium");

        swipeRefreshLayout.setOnRefreshListener(this);

        threadAdapter = new ThreadAdapter(getActivity(), redditAuthentication, new ArrayList<>(),
                false);
        threadAdapter.setCommentClickListener(this);
        
        lmComments = new LinearLayoutManager(getActivity());
        rvComments.setLayoutManager(lmComments);
        rvComments.setAdapter(threadAdapter);
        rvComments.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                if (dy > 0) {
                    ((CommentsActivity) getActivity()).fab.hide();
                } else if (dy < 0) {
                    ((CommentsActivity) getActivity()).fab.show();
                }
            }
        });

        presenter.attachView(this);
        presenter.loadComments(
                threadType,
                homeTeam,
                awayTeam,
                stream,
                false /* forceReload */,
                gameDate);

        if (savedInstanceState != null) {
            commentToReplyToPos = savedInstanceState.getInt(KEY_COMMENT_TO_REPLY_POS);
            commentToReplyToFullName = savedInstanceState.getString(KEY_COMMENT_TO_REPLY_FULL_NAME);
            submissionId = savedInstanceState.getString(KEY_SUBMISSION_ID);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_COMMENT_TO_REPLY_POS, commentToReplyToPos);
        outState.putString(KEY_COMMENT_TO_REPLY_FULL_NAME, commentToReplyToFullName);
        outState.putString(KEY_SUBMISSION_ID, submissionId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        presenter.detachView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (threadType.equals(RedditUtils.LIVE_GT_TYPE)) {
            inflater.inflate(R.menu.menu_game_thread, menu);
            streamSwitch = menu.findItem(R.id.action_stream).getActionView()
                    .findViewById(R.id.switch_stream);
            streamSwitch.setOnCheckedChangeListener(this);
            streamSwitch.setChecked(stream);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                presenter.loadComments(threadType, homeTeam, awayTeam, stream, true /*
                forceReload */, gameDate);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        presenter.loadComments(
                threadType,
                homeTeam,
                awayTeam,
                stream,
                true  /* forceReload */,
                gameDate);
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        swipeRefreshLayout.setRefreshing(active);
    }

    @Override
    public void showComments(List<ThreadItem> comments) {
        threadAdapter.setData(comments);
        rvComments.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideComments() {
        rvComments.setVisibility(View.GONE);
    }

    @Override
    public void addComment(int position, CommentNode comment) {
        threadAdapter.addComment(position, comment);
    }

    @Override
    public void hideText() {
        tvMessage.setVisibility(View.GONE);
    }

    @Override
    public void showNoThreadText() {
        tvMessage.setVisibility(View.VISIBLE);
        tvMessage.setText(R.string.no_thread_made);
    }

    @Override
    public void showNoCommentsText() {
        tvMessage.setVisibility(View.VISIBLE);
        tvMessage.setText(R.string.no_comments_available);
    }

    @Override
    public void showFailedToLoadCommentsText() {
        tvMessage.setVisibility(View.VISIBLE);
        tvMessage.setText(R.string.failed_load_comments);
    }

    @Override
    public void showReplySavedToast() {
        Toast.makeText(getActivity(), R.string.reply_saved, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showReplyErrorToast() {
        Toast.makeText(getActivity(), R.string.reply_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSavedToast() {
        Toast.makeText(getActivity(), R.string.saved, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showNotLoggedInToast() {
        Toast.makeText(getActivity(), R.string.not_logged_in, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showReplyToSubmissionFailedToast() {
        Toast.makeText(getActivity(), R.string.reply_to_sub_failed, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSavingToast() {
        Toast.makeText(getActivity(), R.string.saving, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void openReplyToCommentActivity(final int position, final Comment parentComment) {
        commentToReplyToFullName = parentComment.getFullName();
        commentToReplyToPos = position;

        Intent intent = new Intent(getActivity(), ReplyActivity.class);
        Bundle extras = new Bundle();
        extras.putCharSequence(ReplyActivity.KEY_COMMENT,
                RedditUtils.bindSnuDown(parentComment.data("body_html")));
        intent.putExtras(extras);
        startActivityForResult(intent, ReplyActivity.POST_COMMENT_REPLY_REQUEST);
    }

    @Override
    public void openReplyToSubmissionActivity() {
        Intent intent = new Intent(getActivity(), ReplyActivity.class);
        startActivityForResult(intent, ReplyActivity.POST_SUBMISSION_REPLY_REQUEST);
    }

    @Override
    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }

    @Override
    public void showFab() {
        ((CommentsActivity) getActivity()).showFab();
    }

    @Override
    public void hideFab() {
        ((CommentsActivity) getActivity()).hideFab();
    }

    @Override
    public void onVoteComment(Comment comment, VoteDirection voteDirection) {
        presenter.vote(comment, voteDirection);
    }

    @Override
    public void onSaveComment(Comment comment) {
        presenter.save(comment);
    }

    @Override
    public void onUnsaveComment(Comment comment) {
        presenter.unsave(comment);
    }

    @Override
    public void onReplyToComment(final int position, final Comment parentComment) {
        presenter.replyToCommentBtnClick(position, parentComment);
    }

    public void replyToThread() {
        if (presenter != null) {
            presenter.replyToThreadBtnClick();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            if (!isPremium) {
                ((CommentsActivity) getActivity()).billingProcessor.purchase(getActivity(),
                        "premium");
                streamSwitch.setChecked(false);
            } else {
                stream = true;
                presenter.loadComments(threadType, homeTeam, awayTeam, stream, true /*
                forceReload */, gameDate);
            }
        } else {
            stream = false;
            presenter.loadComments(
                    threadType,
                    homeTeam,
                    awayTeam,
                    stream,
                    true /* forceReload */,
                    gameDate);
        }
    }
}
