package com.gmail.jorgegilcavazos.ballislife.features.submission;

import android.content.SharedPreferences;

import com.gmail.jorgegilcavazos.ballislife.base.BasePresenter;
import com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.data.service.RedditService;
import com.gmail.jorgegilcavazos.ballislife.features.model.wrapper.CustomSubmission;
import com.gmail.jorgegilcavazos.ballislife.util.Constants;
import com.gmail.jorgegilcavazos.ballislife.util.Utilities;
import com.gmail.jorgegilcavazos.ballislife.util.exception.NotLoggedInException;
import com.gmail.jorgegilcavazos.ballislife.util.exception.ReplyNotAvailableException;
import com.gmail.jorgegilcavazos.ballislife.util.exception.ReplyToCommentException;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider;

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

import io.reactivex.CompletableSource;
import io.reactivex.SingleSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;

public class SubmissionPresenter extends BasePresenter<SubmissionView> {

    private RedditAuthentication redditAuthentication;
    private RedditService redditService;
    private SharedPreferences redditPrefs;
    private CompositeDisposable disposables;
    private BaseSchedulerProvider schedulerProvider;

    @Inject
    public SubmissionPresenter(
            RedditAuthentication redditAuthentication,
            RedditService redditService,
            @Named("redditSharedPreferences") SharedPreferences redditPrefs,
            BaseSchedulerProvider schedulerProvider) {
        this.redditAuthentication = redditAuthentication;
        this.redditService = redditService;
        this.redditPrefs = redditPrefs;
        this.schedulerProvider = schedulerProvider;

        disposables = new CompositeDisposable();
    }

    public void loadComments(String threadId, CommentSort sorting) {
        loadComments(threadId, sorting, null);
    }

    public void loadComments(String threadId, CommentSort sorting, final String commentIdToScroll) {
        view.hideFab();
        view.setLoadingIndicator(true);
        disposables.add(redditAuthentication.authenticate(redditPrefs)
                .andThen(redditService.getSubmission(redditAuthentication.getRedditClient(),
                        threadId, sorting))
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeWith(new DisposableSingleObserver<Submission>() {
                    @Override
                    public void onSuccess(Submission submission) {
                        int i = 0;
                        int indexToScrollTo = 0;

                        Iterable<CommentNode> iterable = submission.getComments().walkTree();
                        List<CommentNode> commentNodes = new ArrayList<>();
                        for (CommentNode node : iterable) {
                            commentNodes.add(node);
                            if (commentIdToScroll != null
                                    && node.getComment().getId().equals(commentIdToScroll)) {
                                indexToScrollTo = i;
                            }
                            i++;
                        }

                        view.setCustomSubmission(new CustomSubmission(submission));
                        view.showComments(commentNodes, submission);
                        view.setLoadingIndicator(false);
                        view.showFab();

                        if (commentIdToScroll != null) {
                            view.scrollToComment(indexToScrollTo);
                        } else {
                            view.scrollToComment(0);
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
                .andThen(redditAuthentication.checkUserLoggedIn())
                .flatMapCompletable(new Function<Boolean, CompletableSource>() {
                    @Override
                    public CompletableSource apply(Boolean loggedIn) throws Exception {
                        if (loggedIn) {
                            return redditService.voteSubmission(
                                    redditAuthentication.getRedditClient(), submission, vote);
                        } else {
                            throw new NotLoggedInException();
                        }
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
                .andThen(redditAuthentication.checkUserLoggedIn())
                .flatMapCompletable(new Function<Boolean, CompletableSource>() {
                    @Override
                    public CompletableSource apply(Boolean loggedIn) throws Exception {
                        if (loggedIn) {
                            return redditService.saveSubmission(
                                    redditAuthentication.getRedditClient(), submission, saved);
                        } else {
                            throw new NotLoggedInException();
                        }
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
                .andThen(redditAuthentication.checkUserLoggedIn())
                .flatMapCompletable(new Function<Boolean, CompletableSource>() {
                    @Override
                    public CompletableSource apply(Boolean loggedIn) throws Exception {
                        if (loggedIn) {
                            return redditService.voteComment(redditAuthentication.getRedditClient(),
                                    comment, vote);
                        } else {
                            throw new NotLoggedInException();
                        }
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
                .andThen(redditAuthentication.checkUserLoggedIn())
                .flatMapCompletable(new Function<Boolean, CompletableSource>() {
                    @Override
                    public CompletableSource apply(Boolean loggedIn) throws Exception {
                        if (loggedIn) {
                            return redditService.saveComment(redditAuthentication.getRedditClient(),
                                    comment);
                        } else {
                            throw new NotLoggedInException();
                        }
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
                .andThen(redditAuthentication.checkUserLoggedIn())
                .flatMapCompletable(new Function<Boolean, CompletableSource>() {
                    @Override
                    public CompletableSource apply(Boolean loggedIn) throws Exception {
                        if (loggedIn) {
                            return redditService.unsaveComment(
                                    redditAuthentication.getRedditClient(), comment);
                        } else {
                            throw new NotLoggedInException();
                        }
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

    public void onReplyToComment(final int position, final Comment parent, final String text) {
        view.showSavingToast();
        disposables.add(redditAuthentication.authenticate(redditPrefs)
                .andThen(redditAuthentication.checkUserLoggedIn())
                .flatMap(new Function<Boolean, SingleSource<String>>() {
                    @Override
                    public SingleSource<String> apply(Boolean loggedIn) throws Exception {
                        if (loggedIn) {
                            return redditService.replyToComment(
                                    redditAuthentication.getRedditClient(), parent, text);
                        } else {
                            throw new NotLoggedInException();
                        }
                    }
                })
                // Comment is not immediately available after being posted in the next call
                // (probably a small delay from reddit's servers) so we need to wait for a bit
                // before fetching the posted comment.
                .delay(4, TimeUnit.SECONDS)
                .flatMap(new Function<String, SingleSource<CommentNode>>() {
                    @Override
                    public SingleSource<CommentNode> apply(String s) throws Exception {
                        return redditService.getComment(redditAuthentication.getRedditClient(),
                                parent.getSubmissionId().substring(3), s);
                    }
                })
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

    public void onReplyToThread(final String text, final Submission submission) {
        view.showSavingToast();
        disposables.add(redditAuthentication.authenticate(redditPrefs)
                .andThen(redditAuthentication.checkUserLoggedIn())
                .flatMap(new Function<Boolean, SingleSource<String>>() {
                    @Override
                    public SingleSource<String> apply(Boolean loggedIn) throws Exception {
                        if (loggedIn) {
                            return redditService.replyToThread(
                                    redditAuthentication.getRedditClient(), submission, text);
                        } else {
                            throw new NotLoggedInException();
                        }
                    }
                })
                // Comment is not immediately available after being posted in the next call
                // (probably a small delay from reddit's servers) so we need to wait for a bit
                // before fetching the posted comment.
                .delay(4, TimeUnit.SECONDS)
                .flatMap(new Function<String, SingleSource<CommentNode>>() {
                    @Override
                    public SingleSource<CommentNode> apply(String commentId) throws Exception {
                        return redditService.getComment(redditAuthentication.getRedditClient(),
                                submission.getId(), commentId);
                    }
                })
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
