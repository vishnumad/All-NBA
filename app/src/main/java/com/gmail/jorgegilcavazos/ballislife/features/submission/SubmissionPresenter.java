package com.gmail.jorgegilcavazos.ballislife.features.submission;

import android.content.SharedPreferences;

import com.gmail.jorgegilcavazos.ballislife.base.BasePresenter;
import com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.data.repository.submissions.SubmissionRepository;
import com.gmail.jorgegilcavazos.ballislife.data.service.RedditService;
import com.gmail.jorgegilcavazos.ballislife.features.model.SubmissionWrapper;
import com.gmail.jorgegilcavazos.ballislife.util.Constants;
import com.gmail.jorgegilcavazos.ballislife.util.Utilities;
import com.gmail.jorgegilcavazos.ballislife.util.exception.NotLoggedInException;
import com.gmail.jorgegilcavazos.ballislife.util.exception.ReplyNotAvailableException;
import com.gmail.jorgegilcavazos.ballislife.util.exception.ReplyToCommentException;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider;
import com.google.common.base.Optional;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.CommentSort;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;

public class SubmissionPresenter extends BasePresenter<SubmissionView> {

    private RedditAuthentication redditAuthentication;
    private RedditService redditService;
    private SubmissionRepository submissionRepository;
    private SharedPreferences redditPrefs;
    private CompositeDisposable disposables;
    private BaseSchedulerProvider schedulerProvider;

    @Inject
    public SubmissionPresenter(
            RedditAuthentication redditAuthentication,
            RedditService redditService, SubmissionRepository submissionRepository,
            @Named("redditSharedPreferences") SharedPreferences redditPrefs,
            BaseSchedulerProvider schedulerProvider) {
        this.redditAuthentication = redditAuthentication;
        this.redditService = redditService;
        this.submissionRepository = submissionRepository;
        this.redditPrefs = redditPrefs;
        this.schedulerProvider = schedulerProvider;

        disposables = new CompositeDisposable();
    }

    public void loadComments(String threadId, CommentSort sorting, boolean forceReload) {
        loadComments(threadId, sorting, null, forceReload);
    }

    public void loadComments(String threadId, CommentSort sorting, final String
            commentIdToScroll, boolean forceReload) {
        view.hideFab();
        view.setLoadingIndicator(true);
        disposables.add(redditAuthentication.authenticate(redditPrefs).andThen
                (submissionRepository.getSubmission(threadId, sorting, forceReload))
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui()).subscribeWith(new DisposableSingleObserver<SubmissionWrapper>() {
                    @Override
                    public void onSuccess(SubmissionWrapper submissionWrapper) {
                        int i = 0;
                        int indexToScrollTo = 0;

                        Iterable<CommentNode> iterable = submissionWrapper.submission.getComments
                                ().walkTree();
                        List<CommentNode> commentNodes = new ArrayList<>();
                        for (CommentNode node : iterable) {
                            commentNodes.add(node);
                            if (commentIdToScroll != null
                                    && node.getComment().getId().equals(commentIdToScroll)) {
                                indexToScrollTo = i;
                            }
                            i++;
                        }

                        view.showComments(commentNodes, submissionWrapper.submission);
                        view.setLoadingIndicator(false);
                        view.showFab();

                        if (commentIdToScroll != null) {
                            view.scrollToComment(indexToScrollTo);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.setLoadingIndicator(false);
                    }
                })
        );
    }

    public void onVoteSubmission(final Submission submission, final VoteDirection vote) {
        disposables.add(redditAuthentication.authenticate(redditPrefs)
                .andThen(redditAuthentication.checkUserLoggedIn()).flatMapCompletable((loggedIn)
                        -> {
                    if (loggedIn) {
                        return redditService.voteSubmission(redditAuthentication.getRedditClient
                                (), submission, vote);
                    } else {
                        throw new NotLoggedInException();
                    }
                })
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof NotLoggedInException) {
                            view.showNotLoggedInError();
                        }
                    }
                })
        );
    }

    public void onSaveSubmission(final Submission submission, final boolean saved) {
        disposables.add(redditAuthentication.authenticate(redditPrefs)
                .andThen(redditAuthentication.checkUserLoggedIn()).flatMapCompletable((loggedIn)
                        -> {
                    if (loggedIn) {
                        return redditService.saveSubmission(redditAuthentication.getRedditClient
                                (), submission, saved);
                    } else {
                        throw new NotLoggedInException();
                    }
                })
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof NotLoggedInException) {
                            view.showNotLoggedInError();
                        }
                    }
                })
        );
    }

    public void onVoteComment(final Comment comment, final VoteDirection vote) {
        disposables.add(redditAuthentication.authenticate(redditPrefs)
                .andThen(redditAuthentication.checkUserLoggedIn()).flatMapCompletable((loggedIn)
                        -> {
                    if (loggedIn) {
                        return redditService.voteComment(redditAuthentication.getRedditClient(),
                                comment, vote);
                    } else {
                        throw new NotLoggedInException();
                    }
                })
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof NotLoggedInException) {
                            view.showNotLoggedInError();
                        }
                    }
                })
        );
    }

    public void onSaveComment(final Comment comment) {
        disposables.add(redditAuthentication.authenticate(redditPrefs)
                .andThen(redditAuthentication.checkUserLoggedIn()).flatMapCompletable((loggedIn)
                        -> {
                    if (loggedIn) {
                        return redditService.saveComment(redditAuthentication.getRedditClient(),
                                comment);
                    } else {
                        throw new NotLoggedInException();
                    }
                })
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof NotLoggedInException) {
                            view.showNotLoggedInError();
                        }
                    }
                })
        );
    }

    public void onUnsaveComment(final Comment comment) {
        disposables.add(redditAuthentication.authenticate(redditPrefs)
                .andThen(redditAuthentication.checkUserLoggedIn()).flatMapCompletable((loggedIn)
                        -> {
                    if (loggedIn) {
                        return redditService.unsaveComment(redditAuthentication.getRedditClient()
                                , comment);
                    } else {
                        throw new NotLoggedInException();
                    }
                })
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof NotLoggedInException) {
                            view.showNotLoggedInError();
                        }
                    }
                })
        );
    }

    public void onReplyToCommentBtnClick(int position, Comment parent) {
        if (!redditAuthentication.isUserLoggedIn()) {
            view.showNotLoggedInError();
            return;
        }

        view.openReplyToCommentActivity(position, parent);
    }

    public void onReplyToComment(final int position, final String submissionId, final String
            commentFullName, final String text) {
        Optional<Submission> submission = submissionRepository.getCachedSubmission(submissionId);
        if (!submission.isPresent()) {
            throw new IllegalStateException("A cached submission should've been available.");
        }
        Optional<CommentNode> parent = submission.get().getComments().walkTree()
                .firstMatch(node -> node.getComment().getFullName().equals(commentFullName));
        if (!parent.isPresent()) {
            throw new IllegalStateException("Could not find comment to reply to.");
        }

        view.showSavingToast();
        disposables.add(redditAuthentication.authenticate(redditPrefs)
                .andThen(redditAuthentication.checkUserLoggedIn()).flatMap((loggedIn) -> {
                    if (loggedIn) {
                        return redditService.replyToComment(redditAuthentication.getRedditClient
                                (), parent.get().getComment(), text);
                    } else {
                        throw new NotLoggedInException();
                    }
                })
                // Comment is not immediately available after being posted in the next call
                // (probably a small delay from reddit's servers) so we need to wait for a bit
                // before fetching the posted comment.
                .delay(4, TimeUnit.SECONDS).flatMap(s -> redditService.getComment(redditAuthentication.getRedditClient(), submissionId, s))
                .observeOn(schedulerProvider.ui())
                .subscribeOn(schedulerProvider.io())
                .subscribeWith(new DisposableSingleObserver<CommentNode>() {
                    @Override
                    public void onSuccess(CommentNode comment) {
                        view.addComment(comment, position + 1);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof NotLoggedInException) {
                            view.showNotLoggedInError();
                        } else if (e instanceof ReplyToCommentException) {
                            view.showErrorAddingComment();
                        } else if (e instanceof ReplyNotAvailableException) {
                            // Reply was posted but could not be fetched to display in the UI.
                            view.showSavedToast();
                        } else {
                            view.showErrorSavingToast();
                        }
                    }
                })
        );
    }

    public void onReplyToThreadBtnClick() {
        if (!redditAuthentication.isUserLoggedIn()) {
            view.showNotLoggedInError();
            return;
        }

        view.openReplyToSubmissionActivity();
    }

    public void onReplyToThread(final String text, final String submissionId) {
        Optional<Submission> submission = submissionRepository.getCachedSubmission(submissionId);
        if (!submission.isPresent()) {
            throw new IllegalStateException("A cached submission should've been available.");
        }

        view.showSavingToast();
        disposables.add(redditAuthentication.authenticate(redditPrefs)
                .andThen(redditAuthentication.checkUserLoggedIn()).flatMap((loggedIn) -> {
                    if (loggedIn) {
                        return redditService.replyToThread(redditAuthentication.getRedditClient()
                                , submission.get(), text);
                    } else {
                        throw new NotLoggedInException();
                    }
                })
                // Comment is not immediately available after being posted in the next call
                // (probably a small delay from reddit's servers) so we need to wait for a bit
                // before fetching the posted comment.
                .delay(4, TimeUnit.SECONDS).flatMap(commentId -> redditService.getComment
                        (redditAuthentication.getRedditClient(), submissionId, commentId))
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeWith(new DisposableSingleObserver<CommentNode>() {
                    @Override
                    public void onSuccess(CommentNode comment) {
                        view.addComment(comment, 0);
                        view.scrollToComment(0);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof NotLoggedInException) {
                            view.showNotLoggedInError();
                        } else if (e instanceof ReplyToCommentException) {
                            view.showErrorAddingComment();
                        } else if (e instanceof ReplyNotAvailableException) {
                            // Reply was posted but could not be fetched to display in the UI.
                            view.showSavedToast();
                        } else {
                            view.showErrorSavingToast();
                        }
                    }
                })
        );
    }

    public void onContentClick(final String url) {
        if (url != null) {
            if (url.contains(Constants.STREAMABLE_DOMAIN)) {
                String shortCode = Utilities.getStreamableShortcodeFromUrl(url);
                if (shortCode != null) {
                    view.openStreamable(shortCode);
                } else {
                    view.openContentTab(url);
                }
            } else {
                view.openContentTab(url);
            }
        } else {
            view.showContentUnavailableToast();
        }
    }

    public void stop() {
        if (disposables != null) {
            disposables.clear();
        }
    }
}
