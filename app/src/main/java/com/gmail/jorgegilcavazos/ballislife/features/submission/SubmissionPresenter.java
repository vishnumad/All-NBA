package com.gmail.jorgegilcavazos.ballislife.features.submission;

import com.gmail.jorgegilcavazos.ballislife.base.BasePresenter;
import com.gmail.jorgegilcavazos.ballislife.network.API.RedditService;
import com.gmail.jorgegilcavazos.ballislife.util.exception.NotLoggedInException;
import com.gmail.jorgegilcavazos.ballislife.util.exception.ReplyNotAvailableException;
import com.gmail.jorgegilcavazos.ballislife.util.exception.ReplyToCommentException;
import com.gmail.jorgegilcavazos.ballislife.util.exception.ReplyToThreadException;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.CommentSort;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.SingleSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;

public class SubmissionPresenter extends BasePresenter<SubmissionView> {

    private RedditService service;
    private CompositeDisposable disposables;
    private BaseSchedulerProvider schedulerProvider;

    public SubmissionPresenter(RedditService service, BaseSchedulerProvider schedulerProvider) {
        this.service = service;
        this.schedulerProvider = schedulerProvider;

        disposables = new CompositeDisposable();
    }

    public void loadComments(String threadId) {
        view.setLoadingIndicator(true);
        disposables.add(service.getSubmission(threadId, CommentSort.HOT)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeWith(new DisposableSingleObserver<Submission>() {
                    @Override
                    public void onSuccess(Submission submission) {
                        Iterable<CommentNode> iterable = submission.getComments().walkTree();
                        List<CommentNode> commentNodes = new ArrayList<>();
                        for (CommentNode node : iterable) {
                            commentNodes.add(node);
                        }

                        if (isViewAttached()) {
                            view.showComments(commentNodes, submission);
                            view.setLoadingIndicator(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (isViewAttached()) {
                            view.setLoadingIndicator(false);
                        }
                    }
                })
        );
    }

    public void onVoteSubmission(Submission submission, VoteDirection vote) {
        disposables.add(service.voteSubmission(submission, vote)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                })
        );
    }

    public void onSaveSubmission(Submission submission, boolean saved) {
        view.showSavingToast();
        disposables.add(service.saveSubmission(submission, saved)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        if (isViewAttached()) {
                            view.showSavedToast();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (isViewAttached()) {
                            view.showErrorSavingToast();
                        }
                    }
                })
        );
    }

    public void onVoteComment(Comment comment, VoteDirection vote) {
        disposables.add(service.voteComment(comment, vote)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                })
        );
    }

    public void onSaveComment(Comment comment) {
        view.showSavingToast();
        disposables.add(service.saveComment(comment)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        if (isViewAttached()) {
                            view.showSavedToast();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (isViewAttached()) {
                            view.showErrorSavingToast();
                        }
                    }
                })
        );
    }

    public void onReplyToComment(final int position, final Comment parent, String text) {
        view.showSavingToast();
        disposables.add(service.replyToComment(parent, text)
                .flatMap(new Function<String, SingleSource<CommentNode>>() {
                    @Override
                    public SingleSource<CommentNode> apply(String s) throws Exception {
                        return service.getComment(parent.getSubmissionId().substring(3), s);
                    }
                })
                .observeOn(schedulerProvider.ui())
                .subscribeOn(schedulerProvider.io())
                .subscribeWith(new DisposableSingleObserver<CommentNode>() {
                    @Override
                    public void onSuccess(CommentNode comment) {
                        if (isViewAttached()) {
                            view.showSavedToast();
                            view.addComment(comment, position + 1);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (isViewAttached()) {
                            if (e instanceof NotLoggedInException) {
                                view.showNotLoggedInError();
                            } else if (e instanceof ReplyToCommentException) {
                                view.showErrorAddingComment();
                            } else {
                                view.showSavedToast();
                            }
                        }
                    }
                })
        );
    }

    public void onReplyToThread(String text, final Submission submission) {
        view.showSavingToast();
        disposables.add(service.replyToThread(submission, text)
                .flatMap(new Function<String, SingleSource<CommentNode>>() {
                    @Override
                    public SingleSource<CommentNode> apply(String commentId) throws Exception {
                        return service.getComment(submission.getId(), commentId);
                    }
                })
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeWith(new DisposableSingleObserver<CommentNode>() {
                    @Override
                    public void onSuccess(CommentNode comment) {
                        if (isViewAttached()) {
                            view.showSavedToast();
                            view.addComment(comment, 0);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (isViewAttached()) {
                            if (e instanceof NotLoggedInException) {
                                view.showNotLoggedInError();
                            } else if (e instanceof ReplyToThreadException){
                                view.showErrorAddingComment();
                            } else if (e instanceof ReplyNotAvailableException) {
                                view.showSavedToast();
                            }
                        }
                    }
                })
        );
    }

    public void stop() {
        if (disposables != null) {
            disposables.clear();
        }
    }
}
