package com.gmail.jorgegilcavazos.ballislife.network.API;

import android.os.AsyncTask;
import android.util.Log;

import com.gmail.jorgegilcavazos.ballislife.network.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.util.RedditUtils;

import net.dean.jraw.ApiException;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.SubmissionRequest;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.CommentSort;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.LoggedInAccount;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.UserContributionPaginator;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class RedditService {

    public RedditService() {

    }

    public Observable<LoggedInAccount> getLoggedInAccount() {
        Observable<LoggedInAccount> observable = Observable.create(
                new ObservableOnSubscribe<LoggedInAccount>() {
            @Override
            public void subscribe(ObservableEmitter<LoggedInAccount> e) throws Exception {
                try {
                    if (!e.isDisposed()) {
                        e.onNext(RedditAuthentication.getInstance().getRedditClient().me());
                        e.onComplete();
                    }
                } catch (Exception ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                    }
                }
            }
        });

        return observable;
    }

    public Observable<Listing<Contribution>> getUserContributions() {
        RedditClient redditClient = RedditAuthentication.getInstance().getRedditClient();
        String where = "overview";

        final UserContributionPaginator paginator = new UserContributionPaginator(redditClient,
                where, redditClient.getAuthenticatedUser());
        paginator.setLimit(50);
        paginator.setSorting(Sorting.NEW);

        Observable<Listing<Contribution>> observable = Observable.create(
                new ObservableOnSubscribe<Listing<Contribution>>() {
                    @Override
                    public void subscribe(ObservableEmitter<Listing<Contribution>> e) throws Exception {
                        try {
                            Listing<Contribution> contributions = paginator.next(true);
                            if (!e.isDisposed()) {
                                e.onNext(contributions);
                                e.onComplete();
                            }
                        } catch (Exception ex) {
                            if (!e.isDisposed()) {
                                e.onError(ex);
                            }
                        }
                    }
                }
        );

        return observable;
    }

    public Observable<List<CommentNode>> getComments(final String threadId, final String type) {
        Observable<List<CommentNode>> observable = Observable.create(
                new ObservableOnSubscribe<List<CommentNode>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<CommentNode>> e) throws Exception {
                        RedditClient redditClient = RedditAuthentication.getInstance()
                                .getRedditClient();

                        SubmissionRequest.Builder builder = new SubmissionRequest.Builder(threadId);
                        switch (type) {
                            case RedditUtils.LIVE_GT_TYPE:
                                builder.sort(CommentSort.NEW);
                                break;
                            case RedditUtils.POST_GT_TYPE:
                                builder.sort(CommentSort.TOP);
                                break;
                            default:
                                builder.sort(CommentSort.TOP);
                                break;
                        }

                        SubmissionRequest submissionRequest = builder.build();
                        Submission submission = null;
                        try {
                            submission = redditClient.getSubmission(submissionRequest);

                            Iterable<CommentNode> iterable = submission.getComments().walkTree();
                            List<CommentNode> commentNodes = new ArrayList<>();
                            for (CommentNode node : iterable) {
                                commentNodes.add(node);
                            }

                            if (!e.isDisposed()) {
                                e.onNext(commentNodes);
                                e.onComplete();
                            }
                        } catch (NetworkException ex) {
                            if (!e.isDisposed()) {
                                e.onError(ex);
                            }
                        }
                    }
                }
        );

        return observable;
    }

    public void voteComment(Comment comment, VoteDirection direction) {
        new VoteCommentTask(comment, direction).execute();
    }

    public void saveComment(Comment comment) {
        new SaveCommentTask(comment).execute();
    }

    public void replyToComment(Comment parent, String text) {
        new ReplyToCommentTask(parent, text).execute();
    }

    private static class VoteCommentTask extends AsyncTask<Void, Void, Void> {

        private Comment comment;
        private VoteDirection direction;

        VoteCommentTask(Comment comment, VoteDirection direction) {
            this.comment = comment;
            this.direction = direction;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (RedditAuthentication.getInstance().isUserLoggedIn()) {
                AccountManager accountManager = new AccountManager(
                        RedditAuthentication.getInstance().getRedditClient());
                try {
                    accountManager.vote(comment, direction);
                } catch (Exception e) {
                    // Non-successful request.
                }
            }
            return null;
        }
    }

    private static class SaveCommentTask extends AsyncTask<Void, Void, Void> {

        private Comment comment;

        SaveCommentTask(Comment comment) {
            this.comment = comment;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (RedditAuthentication.getInstance().isUserLoggedIn()) {
                AccountManager accountManager = new AccountManager(
                        RedditAuthentication.getInstance().getRedditClient());
                try {
                    accountManager.save(comment);
                } catch (Exception e) {
                    // Non successful request.
                }
            }
            return null;
        }
    }

    private static class ReplyToCommentTask extends AsyncTask<Void, Void, Void> {

        Comment parent;
        String text;

        ReplyToCommentTask(Comment parent, String text) {
            this.parent = parent;
            this.text = text;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (RedditAuthentication.getInstance().isUserLoggedIn()) {
                AccountManager accountManger = new AccountManager(
                        RedditAuthentication.getInstance().getRedditClient());
                try {
                    accountManger.reply(parent, text);
                } catch (Exception e) {
                    // Non successful request.
                }
            }
            return null;
        }
    }
}
