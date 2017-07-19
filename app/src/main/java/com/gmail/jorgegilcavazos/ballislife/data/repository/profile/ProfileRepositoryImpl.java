package com.gmail.jorgegilcavazos.ballislife.data.repository.profile;

import com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.data.service.RedditService;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;
import net.dean.jraw.paginators.UserContributionPaginator;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;

/**
 * Implementation of the {@link ProfileRepository}. Provides the reddit {@link Contribution}s of a
 * user from memory if available or from a network request if not.
 */
public class ProfileRepositoryImpl implements ProfileRepository {
    public static final String OVERVIEW = "overview";

    private RedditService redditService;

    private UserContributionPaginator contributionPaginator;
    private List<Contribution> cachedContributions;

    @Inject
    public ProfileRepositoryImpl(
            RedditService redditService,
            RedditAuthentication redditAuthentication,
            String where,
            int limit,
            Sorting sorting,
            TimePeriod timePeriod) {
        this.redditService = redditService;

        cachedContributions = new ArrayList<>();
        RedditClient redditClient = redditAuthentication.getRedditClient();
        contributionPaginator = new UserContributionPaginator(
                redditClient,
                where,
                redditClient.getAuthenticatedUser());
        contributionPaginator.setLimit(limit);
        contributionPaginator.setSorting(sorting);
        contributionPaginator.setTimePeriod(timePeriod);
    }

    @Override
    public void reset() {
        contributionPaginator.reset();
        cachedContributions.clear();
    }

    @Override
    public Single<List<Contribution>> next() {
        return redditService.getUserContributions(contributionPaginator)
                .flatMap(new Function<List<Contribution>, SingleSource<? extends List<Contribution>>>() {
                    @Override
                    public SingleSource<? extends List<Contribution>> apply(List<Contribution> contributions) throws Exception {
                        cachedContributions.addAll(contributions);
                        return Single.just(contributions);
                    }
                });
    }

    @Override
    public List<Contribution> getCachedContributions() {
        return cachedContributions;
    }

    @Override
    public void clearCache() {
        cachedContributions.clear();
    }
}
