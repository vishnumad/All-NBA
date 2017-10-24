package com.gmail.jorgegilcavazos.ballislife.features.gamethread;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.gmail.jorgegilcavazos.ballislife.features.common.ThreadAdapter;
import com.gmail.jorgegilcavazos.ballislife.features.model.GameThreadType;
import com.gmail.jorgegilcavazos.ballislife.features.model.ThreadItem;
import com.gmail.jorgegilcavazos.ballislife.features.reply.ReplyActivity;
import com.gmail.jorgegilcavazos.ballislife.util.RedditUtils;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import static android.app.Activity.RESULT_OK;
import static com.gmail.jorgegilcavazos.ballislife.features.gamethread.CommentsActivity
        .AWAY_TEAM_KEY;
import static com.gmail.jorgegilcavazos.ballislife.features.gamethread.CommentsActivity
        .HOME_TEAM_KEY;

public class GameThreadFragment extends Fragment
        implements GameThreadView,
        SwipeRefreshLayout.OnRefreshListener,
        CompoundButton.OnCheckedChangeListener {

    public static final String THREAD_TYPE_KEY = "THREAD_TYPE";
    public static final String GAME_DATE_KEY = "GAME_DATE";

    @Inject GameThreadPresenterV2 presenter;
    @Inject RedditAuthentication redditAuthentication;

    @BindView(R.id.game_thread_swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.comment_thread_rv) RecyclerView rvComments;
    @BindView(R.id.noThreadText) TextView noThreadText;
    @BindView(R.id.noCommentsText) TextView noCommentsText;
    @BindView(R.id.errorLoadingText) TextView errorLoadingText;

    private PublishSubject<Object> fabClicks = PublishSubject.create();
    private PublishSubject<Boolean> streamChanges = PublishSubject.create();

    private RecyclerView.LayoutManager lmComments;
    private ThreadAdapter threadAdapter;
    private Unbinder unbinder;
    private String homeTeam, awayTeam;
    private GameThreadType threadType;
    private long gameDate;
    private Switch streamSwitch;

    public GameThreadFragment() {
        // Required empty public constructor.
    }

    public static GameThreadFragment newInstance() {
        return new GameThreadFragment();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ReplyActivity.POST_COMMENT_REPLY_REQUEST && resultCode == RESULT_OK) {
            String parentFullname = data.getStringExtra(ReplyActivity.KEY_COMMENT_FULLNAME);
            String response = data.getStringExtra(ReplyActivity.KEY_POSTED_COMMENT);
            presenter.replyToComment(parentFullname, response);
        } else if (requestCode == ReplyActivity.POST_SUBMISSION_REPLY_REQUEST && resultCode ==
                RESULT_OK) {
            String response = data.getStringExtra(ReplyActivity.KEY_POSTED_COMMENT);
            String submissionId = data.getStringExtra(ReplyActivity.KEY_SUBMISSION_ID);
            presenter.replyToSubmission(submissionId, response);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        BallIsLifeApplication.getAppComponent().inject(this);
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            homeTeam = getArguments().getString(HOME_TEAM_KEY);
            awayTeam = getArguments().getString(AWAY_TEAM_KEY);
            threadType = (GameThreadType) getArguments().getSerializable(THREAD_TYPE_KEY);
            gameDate = getArguments().getLong(GAME_DATE_KEY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_thread, container, false);
        unbinder = ButterKnife.bind(this, view);

        swipeRefreshLayout.setOnRefreshListener(this);

        threadAdapter = new ThreadAdapter(getActivity(), redditAuthentication, new ArrayList<>(),
                false);
        
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
        presenter.loadGameThread();

        return view;
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        presenter.detachView();
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (threadType == GameThreadType.LIVE) {
            inflater.inflate(R.menu.menu_game_thread, menu);
            streamSwitch = menu.findItem(R.id.action_stream).getActionView()
                    .findViewById(R.id.switch_stream);
            streamSwitch.setOnCheckedChangeListener(this);
            streamSwitch.setChecked(false);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                presenter.loadGameThread();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        presenter.loadGameThread();
    }

    @NonNull
    @Override
    public GameThreadType getThreadType() {
        return threadType;
    }

    @NonNull
    @Override
    public String getHome() {
        return homeTeam;
    }

    @NonNull
    @Override
    public String getVisitor() {
        return awayTeam;
    }

    @Override
    public long getGameTimeUtc() {
        return gameDate;
    }

    @Override
    public boolean isPremiumPurchased() {
        return ((CommentsActivity) getActivity()).billingProcessor.isPurchased("premium");
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
    public void showNoThreadText() {
        noThreadText.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideNoThreadText() {
        noThreadText.setVisibility(View.GONE);
    }

    @Override
    public void showNoCommentsText() {
        noCommentsText.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideNoCommentsText() {
        noCommentsText.setVisibility(View.GONE);
    }

    @Override
    public void showErrorLoadingText(int code) {
        errorLoadingText.setText(getString(R.string.error_loading_comments, code));
        errorLoadingText.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideErrorLoadingText() {
        errorLoadingText.setVisibility(View.GONE);
    }

    @NotNull
    @Override
    public Observable<Comment> commentSaves() {
        return threadAdapter.getCommentSaves();
    }

    @NotNull
    @Override
    public Observable<Comment> commentUnsaves() {
        return threadAdapter.getCommentUnsaves();
    }

    @NotNull
    @Override
    public Observable<Comment> upvotes() {
        return threadAdapter.getUpvotes();
    }

    @NotNull
    @Override
    public Observable<Comment> downvotes() {
        return threadAdapter.getDownvotes();
    }

    @NotNull
    @Override
    public Observable<Comment> novotes() {
        return threadAdapter.getNovotes();
    }

    @NotNull
    @Override
    public Observable<Comment> replies() {
        return threadAdapter.getReplies();
    }

    @NotNull
    @Override
    public Observable<Object> submissionReplies() {
        return fabClicks;
    }

    @Override
    public void openReplyToCommentActivity(@NonNull Comment parentComment) {
        Intent intent = new Intent(getActivity(), ReplyActivity.class);
        Bundle extras = new Bundle();
        extras.putString(ReplyActivity.KEY_COMMENT_FULLNAME, parentComment.getFullName());
        extras.putCharSequence(ReplyActivity.KEY_COMMENT,
                               RedditUtils.bindSnuDown(parentComment.data("body_html")));
        intent.putExtras(extras);
        startActivityForResult(intent, ReplyActivity.POST_COMMENT_REPLY_REQUEST);
    }

    @Override
    public void openReplyToSubmissionActivity(@NonNull String submissionId) {
        Intent intent = new Intent(getActivity(), ReplyActivity.class);
        Bundle extras = new Bundle();
        extras.putString(ReplyActivity.KEY_SUBMISSION_ID, submissionId);
        intent.putExtras(extras);
        startActivityForResult(intent, ReplyActivity.POST_SUBMISSION_REPLY_REQUEST);
    }

    @Override
    public void showSavingToast() {
        Toast.makeText(getActivity(), R.string.saving, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSavedToast() {
        Toast.makeText(getActivity(), R.string.saved, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showUnsavingToast() {
        Toast.makeText(getActivity(), R.string.unsaving, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showUnsavedToast() {
        Toast.makeText(getActivity(), R.string.unsaved, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSubmittingCommentToast() {
        Toast.makeText(getActivity(), "Saving comment", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSubmittedCommentToast() {
        Toast.makeText(getActivity(), "Reply saved", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showMissingParentToast() {
        Toast.makeText(getActivity(), "Couldn't save comment, missing parent", Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void showMissingSubmissionToast() {
        Toast.makeText(
                getActivity(),
                "Couldn't save comment, missing submission",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showErrorSavingCommentToast(int code) {
        Toast.makeText(getActivity(),
                       getString(R.string.something_went_wrong, code),
                       Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void showNotLoggedInToast() {
        Toast.makeText(getActivity(), R.string.not_logged_in, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showNoNetAvailable() {
        errorLoadingText.setText(R.string.your_device_is_offline);
        errorLoadingText.setVisibility(View.VISIBLE);
    }

    @Override
    public void showFab() {
        ((CommentsActivity) getActivity()).showFab();
    }

    @Override
    public void hideFab() {
        ((CommentsActivity) getActivity()).hideFab();
    }


    public void fabClicked() {
        fabClicks.onNext(new Object());
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        streamChanges.onNext(isChecked);
    }

    @NotNull
    @Override
    public Observable<Boolean> streamChanges() {
        return streamChanges;
    }

    @Override
    public void purchasePremium() {
        ((CommentsActivity) getActivity()).billingProcessor.purchase(getActivity(), "premium");
    }

    @Override
    public void setStreamSwitch(boolean isChecked) {
        if (streamSwitch != null) {
            streamSwitch.setChecked(isChecked);
        }
    }
}
