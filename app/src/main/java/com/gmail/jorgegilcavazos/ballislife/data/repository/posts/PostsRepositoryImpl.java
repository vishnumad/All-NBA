package com.gmail.jorgegilcavazos.ballislife.data.repository.posts;

import com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthenticationImpl;
import com.gmail.jorgegilcavazos.ballislife.data.service.RedditService;
import com.gmail.jorgegilcavazos.ballislife.features.application.BallIsLifeApplication;
import com.gmail.jorgegilcavazos.ballislife.features.model.wrapper.CustomSubmission;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dean.jraw.paginators.TimePeriod;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;

public class PostsRepositoryImpl implements PostsRepository {

    @Inject
    RedditService redditService;

    private SubredditPaginator paginator;
    private List<CustomSubmission> cachedCustomSubmissions;

    public PostsRepositoryImpl() {
        BallIsLifeApplication.getAppComponent().inject(this);
        cachedCustomSubmissions = new ArrayList<>();
    }

    @Override
    public void reset(Sorting sorting, TimePeriod timePeriod, String subreddit) {
        SubredditPaginator paginator = new SubredditPaginator(
                RedditAuthenticationImpl.getInstance().getRedditClient(),
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
    public Single<List<CustomSubmission>> next() {
        return redditService.getSubmissionListing(paginator)
                .flatMap(new Function<Listing<Submission>, SingleSource<? extends
                        List<CustomSubmission>>>() {
                    @Override
                    public SingleSource<? extends List<CustomSubmission>>
                    apply(Listing<Submission> submissions) throws Exception {
                        // Convert immutable listing to mutable list of custom submissions.
                        List<CustomSubmission> customSubmissions = new ArrayList<>();
                        for (Submission submission : submissions) {
                            customSubmissions.add(new CustomSubmission(submission));
                        }

                        cachedCustomSubmissions.addAll(customSubmissions);
                        return Single.just(customSubmissions);
                    }
                });
    }

    @Override
    public List<CustomSubmission> getCachedSubmissions() {
        return cachedCustomSubmissions;
    }

    @Override
    public void clearCache() {
        cachedCustomSubmissions.clear();
    }
}
