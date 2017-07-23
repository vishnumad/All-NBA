package com.gmail.jorgegilcavazos.ballislife.data.service;

import com.gmail.jorgegilcavazos.ballislife.features.model.SubscriberCount;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.CommentSort;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dean.jraw.paginators.UserContributionPaginator;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Wrapper around the JRAW that provides easy access to reddit operations using RxJava.
 */
public interface RedditService {

    /**
     * Returns an Rx Single that emits a listing of the next page of contributions of a given
     * paginator.
     */
    Single<List<Contribution>> getUserContributions(UserContributionPaginator paginator);

    /**
     * Returns an Rx Single that emits a list all the comments in a submission.
     *
     * @param threadId the id of the submission to fetch comments of
     * @param type     either RedditUtils.LIVE_GT_TYPE or RedditUtils.POST_GT_TYPE
     */
    Single<List<CommentNode>> getComments(RedditClient redditClient, String threadId, String type);

    /**
     * Returns an Rx Single that emits the data of a full comment given its id and the id of the
     * submission it belongs to.
     */
    Single<CommentNode> getComment(RedditClient redditClient, String threadId, String commentId);

    /**
     * Returns an Rx Single that posts a reply to a given comment.
     *
     * @param parent comment that we are replying to
     * @param text   contents of the reply
     * @return the id of the posted comment
     */
    Single<String> replyToComment(RedditClient redditClient, Comment parent, String text);

    /**
     * Returns an Rx Completable that performs a vote on a comment.
     */
    Completable voteComment(RedditClient redditClient, Comment comment, VoteDirection direction);

    /**
     * Returns an Rx Completable that saves the given comment for the currently logged-in user.
     */
    Completable saveComment(RedditClient redditClient, Comment comment);

    /**
     * Returns an Rx Completable that un-saves the given comment for the currently logged-in user.
     */
    Completable unsaveComment(RedditClient redditClient, Comment comment);

    /**
     * Returns an Rx Single that posts a reply to a given submission.
     *
     * @param submission that we are replying to
     * @param text       contents of the reply
     * @return the id of the posted comment
     */
    Single<String> replyToThread(RedditClient redditClient, Submission submission, String text);

    /**
     * Returns an Rx Single that emits a full reddit submittion.
     *
     * @param threadId of the submission to fetch
     * @param sort     that the comments should be retrieved with
     */
    Single<Submission> getSubmission(RedditClient redditClient, String threadId, CommentSort sort);

    /**
     * Returns an Rx Single that emits a listing of the next page of Submission given a paginator.
     */
    Single<Listing<Submission>> getSubmissionListing(SubredditPaginator paginator);

    /**
     * Returns an Rx Completable that performs a vote on a fiven submission.
     */
    Completable voteSubmission(RedditClient redditClient, Submission submission,
                               VoteDirection vote);

    /**
     * Returns an Rx Completable that saves or un-saves the given submission for the currently
     * logged-in user.
     */
    Completable saveSubmission(RedditClient redditClient, Submission submission, boolean saved);

    /**
     * Returns an Rx Single that emits the subscriber count of the given subreddit.
     */
    Single<SubscriberCount> getSubscriberCount(RedditClient redditClient, String subreddit);

    /**
     * Returns an Rx Completable that authenticates to reddit without a user context.
     */
    Completable userlessAuthentication(RedditClient reddit, Credentials credentials);

    /**
     * Returns an Rx Completable that authenticates to reddit with a user context.
     */
    Completable userAuthentication(RedditClient reddit, Credentials credentials, String url);

    /**
     * Returns an Rx Completable that refreshes the token of the current reddit session.
     */
    Completable refreshToken(RedditClient reddit, Credentials credentials, String refreshToken);

    /**
     * Returns an Rx Completable that de-authenticates the current reddit session.
     */
    Completable deAuthenticate(RedditClient reddit, Credentials credentials);
}
