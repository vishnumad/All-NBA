package com.gmail.jorgegilcavazos.ballislife.features.gamethread;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
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

import com.afollestad.materialdialogs.MaterialDialog;
import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.data.API.RedditService;
import com.gmail.jorgegilcavazos.ballislife.features.shared.OnCommentClickListener;
import com.gmail.jorgegilcavazos.ballislife.features.shared.ThreadAdapter;
import com.gmail.jorgegilcavazos.ballislife.util.RedditUtils;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.VoteDirection;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.content.Context.MODE_PRIVATE;
import static com.gmail.jorgegilcavazos.ballislife.data.RedditAuthentication.REDDIT_AUTH_PREFS;
import static com.gmail.jorgegilcavazos.ballislife.features.gamethread.CommentsActivity.AWAY_TEAM_KEY;
import static com.gmail.jorgegilcavazos.ballislife.features.gamethread.CommentsActivity.HOME_TEAM_KEY;

public class GameThreadFragment extends Fragment implements GameThreadView,
        SwipeRefreshLayout.OnRefreshListener, OnCommentClickListener, CompoundButton.OnCheckedChangeListener {
    public static final String THREAD_TYPE_KEY = "THREAD_TYPE";
    public static final String GAME_DATE_KEY = "GAME_DATE";
    private static final String TAG = "GameThreadFragment";
    public boolean isPremium = false;
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
    private GameThreadPresenter presenter;

    public GameThreadFragment() {
        // Required empty public constructor.
    }

    public static GameThreadFragment newInstance() {
        return new GameThreadFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
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

        threadAdapter = new ThreadAdapter(getActivity(), new ArrayList<CommentNode>(), false);
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

        SharedPreferences preferences = getActivity().getSharedPreferences(REDDIT_AUTH_PREFS,
                MODE_PRIVATE);

        presenter = new GameThreadPresenter(this, new RedditService(), gameDate, preferences);
        presenter.start();
        presenter.loadComments(threadType, homeTeam, awayTeam, stream);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (threadType.equals(RedditUtils.LIVE_GT_TYPE)) {
            inflater.inflate(R.menu.menu_game_thread, menu);
            streamSwitch = (Switch) menu.findItem(R.id.action_stream).getActionView()
                    .findViewById(R.id.switch_stream);
            streamSwitch.setOnCheckedChangeListener(this);
            streamSwitch.setChecked(stream);
        }
        super.onCreateOptionsMenu(menu, inflater);
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
                presenter.loadComments(threadType, homeTeam, awayTeam, stream);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        presenter.loadComments(threadType, homeTeam, awayTeam, stream);
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        swipeRefreshLayout.setRefreshing(active);
    }

    @Override
    public void showComments(List<CommentNode> comments) {
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
    public void showFailedToSaveToast() {
        Toast.makeText(getActivity(), R.string.failed_to_save_comment, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showNotLoggedInToast() {
        Toast.makeText(getActivity(), R.string.not_logged_in, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showReplyToSubmissionSavedToast() {
        Toast.makeText(getActivity(), R.string.reply_to_sub_saved, Toast.LENGTH_SHORT).show();
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
    public void openReplyToCommentDialog(final int position, final Comment parentComment) {
        new MaterialDialog.Builder(getContext())
                .title(R.string.add_comment)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                .input(R.string.type_comment, R.string.empty, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        presenter.reply(position, parentComment, input.toString());
                    }
                })
                .positiveText(R.string.reply)
                .negativeText(R.string.cancel)
                .show();
    }

    @Override
    public void openReplyToThreadDialog() {
        new MaterialDialog.Builder(getContext())
                .title(R.string.add_comment)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                .input(R.string.type_comment, R.string.empty, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        presenter.replyToThread(input.toString(), threadType, homeTeam, awayTeam);
                    }
                })
                .positiveText(R.string.reply)
                .negativeText(R.string.cancel)
                .show();
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
                presenter.loadComments(threadType, homeTeam, awayTeam, stream);
            }
        } else {
            stream = false;
            presenter.loadComments(threadType, homeTeam, awayTeam, stream);
        }
    }
}
