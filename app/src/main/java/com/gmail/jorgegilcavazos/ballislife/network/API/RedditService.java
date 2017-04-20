package com.gmail.jorgegilcavazos.ballislife.network.API;

import android.util.Log;

import com.gmail.jorgegilcavazos.ballislife.features.model.SubscriberCount;
import com.gmail.jorgegilcavazos.ballislife.network.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.util.RedditUtils;
import com.gmail.jorgegilcavazos.ballislife.util.exception.NotAuthenticatedException;
import com.gmail.jorgegilcavazos.ballislife.util.exception.NotLoggedInException;
import com.gmail.jorgegilcavazos.ballislife.util.exception.ReplyNotAvailableException;
import com.gmail.jorgegilcavazos.ballislife.util.exception.ReplyToCommentException;
import com.gmail.jorgegilcavazos.ballislife.util.exception.ReplyToThreadException;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.SubmissionRequest;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthHelper;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.CommentSort;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.LoggedInAccount;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.models.VoteDirection;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dean.jraw.paginators.UserContributionPaginator;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;

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

    public Single<List<CommentNode>> getComments(final String threadId, final String type) {
        return Single.create(new SingleOnSubscribe<List<CommentNode>>() {
            @Override
            public void subscribe(SingleEmitter<List<CommentNode>> e) throws Exception {
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
                        e.onSuccess(commentNodes);
                    }
                } catch (Exception ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                    }
                }
            }
        });
    }

    public Single<List<CommentNode>> getComments(final String threadId, final CommentSort sorting) {
        return Single.create(new SingleOnSubscribe<List<CommentNode>>() {
            @Override
            public void subscribe(SingleEmitter<List<CommentNode>> e) throws Exception {
                RedditClient redditClient = RedditAuthentication.getInstance()
                        .getRedditClient();

                SubmissionRequest.Builder builder = new SubmissionRequest.Builder(threadId);
                builder.sort(sorting);

                SubmissionRequest submissionRequest = builder.build();
                Submission submission = null;
                try {
                    submission = redditClient.getSubmission(submissionRequest);

                    Iterable<CommentNode> iterable = submission.getComments().walkTree();
                    List<CommentNode> commentNodes = new ArrayList<>();
                    for (CommentNode node : iterable) {
                        commentNodes.add(node);
                    }

                    e.onSuccess(commentNodes);
                } catch (Exception ex) {
                    e.onError(ex);
                }
            }
        });
    }

    public Single<CommentNode> getComment(final String threadId, final String commentId) {
        return Single.create(new SingleOnSubscribe<CommentNode>() {
            @Override
            public void subscribe(SingleEmitter<CommentNode> e) throws Exception {
                RedditClient redditClient = RedditAuthentication.getInstance()
                        .getRedditClient();

                SubmissionRequest.Builder builder = new SubmissionRequest.Builder(threadId);
                builder.sort(CommentSort.NEW);

                SubmissionRequest submissionRequest = builder.build();
                Submission submission = null;
                try {
                    submission = redditClient.getSubmission(submissionRequest);

                    Iterable<CommentNode> iterable = submission.getComments().walkTree();
                    for (CommentNode node : iterable) {
                        if (node.getComment().getId().equals(commentId)) {
                            if (!e.isDisposed()) {
                                e.onSuccess(node);
                                return;
                            }
                        }
                    }

                    if (!e.isDisposed()) {
                        e.onError(new ReplyNotAvailableException());
                    }

                } catch (NetworkException ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                    }
                }
            }
        });
    }

    public Single<String> replyToComment(final Comment parent, final String text) {
        return Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(SingleEmitter<String> e) throws Exception {
                if (RedditAuthentication.getInstance().isUserLoggedIn()) {
                    AccountManager accountManger = new AccountManager(
                            RedditAuthentication.getInstance().getRedditClient());
                    try {
                        String id = accountManger.reply(parent, text);
                        e.onSuccess(id);
                    } catch (Exception ex) {
                        e.onError(new ReplyToCommentException());
                    }
                } else {
                    Single.error(new NotLoggedInException());
                }
            }
        });
    }

    public Completable voteComment(final Comment comment, final VoteDirection direction) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                if (RedditAuthentication.getInstance().isUserLoggedIn()) {
                    AccountManager accountManager = new AccountManager(
                            RedditAuthentication.getInstance().getRedditClient());
                    try {
                        accountManager.vote(comment, direction);
                        e.onComplete();
                    } catch (Exception ex) {
                        e.onError(ex);
                    }
                } else {
                    e.onError(new NotLoggedInException());
                }
            }
        });
    }

    public Completable saveComment(final Comment comment) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                if (RedditAuthentication.getInstance().isUserLoggedIn()) {
                    AccountManager accountManager = new AccountManager(
                            RedditAuthentication.getInstance().getRedditClient());
                    try {
                        accountManager.save(comment);
                        e.onComplete();
                    } catch (Exception ex) {
                        e.onError(ex);
                    }
                } else {
                    e.onError(new NotLoggedInException());
                }
            }
        });
    }

    public Single<String> replyToThread(final Submission submission, final String text) {
        return Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(SingleEmitter<String> e) throws Exception {
                if (RedditAuthentication.getInstance().isUserLoggedIn()) {
                    AccountManager accountManager = new AccountManager(
                            RedditAuthentication.getInstance().getRedditClient());

                    try {
                        e.onSuccess(accountManager.reply(submission, text));
                    } catch (Exception ex) {
                        e.onError(new ReplyToThreadException());
                    }
                } else {
                    e.onError(new NotLoggedInException());
                }
            }
        });
    }

    public Single<Submission> getSubmission(final String threadId, final CommentSort sort) {
        return Single.create(new SingleOnSubscribe<Submission>() {
            @Override
            public void subscribe(SingleEmitter<Submission> e) throws Exception {
                RedditClient redditClient = RedditAuthentication.getInstance()
                        .getRedditClient();

                SubmissionRequest.Builder builder = new SubmissionRequest.Builder(threadId);
                if (sort != null) {
                    builder.sort(sort);
                }

                SubmissionRequest submissionRequest = builder.build();
                Submission submission = null;

                try {
                    e.onSuccess(redditClient.getSubmission(submissionRequest));
                } catch (Exception ex) {
                    e.onError(ex);
                }
            }
        });
    }

    public Single<Listing<Submission>> getSubmissionListing(final String subreddit, final int limit,
                                                            final Sorting sorting) {
        return Single.create(new SingleOnSubscribe<Listing<Submission>>() {
            @Override
            public void subscribe(SingleEmitter<Listing<Submission>> e) throws Exception {
                RedditClient redditClient = RedditAuthentication.getInstance()
                        .getRedditClient();

                if (redditClient.isAuthenticated()) {

                    try {
                        SubredditPaginator paginator = new SubredditPaginator(redditClient, subreddit);
                        paginator.setLimit(limit);
                        paginator.setSorting(sorting);

                        e.onSuccess(paginator.next(false));
                    } catch (Exception ex) {
                        e.onError(ex);
                    }
                } else {
                    e.onError(new NotAuthenticatedException());
                }
            }
        });
    }

    public Single<Listing<Submission>> getSubmissionListing(final SubredditPaginator paginator) {
        return Single.create(new SingleOnSubscribe<Listing<Submission>>() {
            @Override
            public void subscribe(SingleEmitter<Listing<Submission>> e) throws Exception {
                Log.d("RedditService", "providing next page");
                e.onSuccess(paginator.next(false));
            }
        });
    }

    public Completable voteSubmission(final Submission submission, final VoteDirection vote) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                if (RedditAuthentication.getInstance().isUserLoggedIn()) {
                    AccountManager accountManager = new AccountManager(
                            RedditAuthentication.getInstance().getRedditClient());
                    try {
                        accountManager.vote(submission, vote);
                        e.onComplete();
                    } catch (Exception ex) {
                        e.onError(ex);
                    }
                } else {
                    e.onError(new NotLoggedInException());
                }
            }
        });
    }

    public Completable saveSubmission(final Submission submission, final boolean saved) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                if (RedditAuthentication.getInstance().isUserLoggedIn()) {
                    AccountManager accountManager = new AccountManager(
                            RedditAuthentication.getInstance().getRedditClient());
                    try {
                        if (saved) {
                            accountManager.save(submission);
                        } else {
                            accountManager.unsave(submission);
                        }
                        e.onComplete();
                    } catch (Exception ex) {
                        e.onError(ex);
                    }
                } else {
                    e.onError(new NotLoggedInException());
                }
            }
        });
    }

    public Single<SubscriberCount> getSubscriberCount(final String subreddit) {
        return Single.create(new SingleOnSubscribe<SubscriberCount>() {
            @Override
            public void subscribe(SingleEmitter<SubscriberCount> e) throws Exception {
                RedditClient client = RedditAuthentication.getInstance().getRedditClient();

                try {
                    Subreddit rnba = client.getSubreddit(subreddit);
                    Long subscribers = rnba.getSubscriberCount();
                    int activeUsers = rnba.getAccountsActive();

                    e.onSuccess(new SubscriberCount(subscribers, activeUsers));
                } catch (Exception ex) {
                    e.onError(ex);
                }
            }
        });
    }

    public Completable userlessAuthentication(final RedditClient reddit,
                                              final Credentials credentials) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                try {
                    OAuthData oAuthData = reddit.getOAuthHelper().easyAuth(credentials);
                    reddit.authenticate(oAuthData);
                    e.onComplete();
                } catch (Exception ex) {
                    e.onError(ex);
                }
            }
        });
    }

    public Completable userAuthentication(final RedditClient reddit, final Credentials credentials,
                                          final String url) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                OAuthHelper oAuthHelper = reddit.getOAuthHelper();

                try {
                    OAuthData oAuthData = oAuthHelper.onUserChallenge(url, credentials);
                    reddit.authenticate(oAuthData);
                    e.onComplete();
                } catch (Exception ex) {
                    e.onError(ex);
                }
            }
        });
    }

    public Completable refreshToken(final RedditClient reddit, final Credentials credentials,
                                          final String refreshToken) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                OAuthHelper helper = reddit.getOAuthHelper();
                helper.setRefreshToken(refreshToken);

                try {
                    OAuthData oAuthData = helper.refreshToken(credentials);
                    reddit.authenticate(oAuthData);
                    e.onComplete();
                } catch (Exception ex) {
                    e.onError(ex);
                }
            }
        });
    }

    public Completable deAuthenticate(final RedditClient reddit, final Credentials credentials) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                OAuthHelper helper = reddit.getOAuthHelper();
                try {
                    helper.revokeAccessToken(credentials);
                    reddit.deauthenticate();
                    e.onComplete();
                } catch (Exception ex) {
                    e.onError(ex);
                }
            }
        });
    }
}
