package com.gmail.jorgegilcavazos.ballislife.data.repository.posts;

import com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.data.service.RedditService;
import com.gmail.jorgegilcavazos.ballislife.features.model.wrapper.SubmissionWrapper;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dean.jraw.paginators.TimePeriod;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;

@Singleton
public class PostsRepositoryImpl implements PostsRepository {

    private RedditAuthentication redditAuthentication;
    private RedditService redditService;

    private SubredditPaginator paginator;
    private List<SubmissionWrapper> cachedSubmissionWrappers;


    @Inject
    public PostsRepositoryImpl(
            RedditAuthentication redditAuthentication,
            RedditService redditService) {
        this.redditAuthentication = redditAuthentication;
        this.redditService = redditService;
        cachedSubmissionWrappers = new ArrayList<>();
    }

    @Override
    public void reset(Sorting sorting, TimePeriod timePeriod, String subreddit) {
        SubredditPaginator paginator = new SubredditPaginator(
                redditAuthentication.getRedditClient(),
                subreddit);
        paginator.setLimit(20);
        paginator.setSorting(sorting);
        paginator.setTimePeriod(timePeriod);
        if (sorting == Sorting.TOP) {
            paginator.setTimePeriod(timePeriod);
        }
        this.paginator = paginator;
        clearCache();
    }

    @Override
    public Single<List<SubmissionWrapper>> next() {
        return redditService.getSubmissionListing(paginator)
                .flatMap(new Function<Listing<Submission>, SingleSource<? extends List
                        <SubmissionWrapper>>>() {
                    @Override
                    public SingleSource<? extends List<SubmissionWrapper>>
                    apply(Listing<Submission> submissions) throws Exception {
                        // Convert immutable listing to mutable list of custom submissions.
                        List<SubmissionWrapper> submissionWrappers = new ArrayList<>();
                        for (Submission submission : submissions) {
                            submissionWrappers.add(new SubmissionWrapper(submission));
                        }

                        cachedSubmissionWrappers.addAll(submissionWrappers);
                        return Single.just(submissionWrappers);
                    }
                });
    }

    @Override
    public List<SubmissionWrapper> getCachedSubmissions() {
        return cachedSubmissionWrappers;
    }

    @Override
    public void clearCache() {
        cachedSubmissionWrappers.clear();
    }
}
