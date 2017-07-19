package com.gmail.jorgegilcavazos.ballislife.data.repository;

import com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.data.repository.profile.ProfileRepository;
import com.gmail.jorgegilcavazos.ballislife.data.repository.profile.ProfileRepositoryImpl;
import com.gmail.jorgegilcavazos.ballislife.data.service.RedditService;
import com.google.common.collect.ImmutableList;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;
import net.dean.jraw.paginators.UserContributionPaginator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link ProfileRepositoryImpl}
 */
@RunWith(MockitoJUnitRunner.class)
public class ProfileRepositoryImplTest {

    private static final List<Contribution> contributions = ImmutableList.of(
            mock(Contribution.class),
            mock(Contribution.class),
            mock(Contribution.class),
            mock(Contribution.class),
            mock(Contribution.class));

    @Mock
    RedditService mockRedditService;

    @Mock
    RedditAuthentication mockRedditAuthentication;

    @Mock
    RedditClient redditClient;

    private ProfileRepository profileRepository;

    @Before
    public void setup() {
        when(redditClient.getAuthenticatedUser()).thenReturn("Juanito");
        when(mockRedditAuthentication.getRedditClient()).thenReturn(redditClient);
        profileRepository = new ProfileRepositoryImpl(
                mockRedditService,
                mockRedditAuthentication,
                ProfileRepositoryImpl.OVERVIEW,
                5,
                Sorting.NEW,
                TimePeriod.ALL);
    }

    @Test
    public void testNext() {
        when(mockRedditService.getUserContributions(any(UserContributionPaginator.class)))
                .thenReturn(Single.just(contributions));

        List<Contribution> actualContributions = profileRepository.next().blockingGet();

        verify(mockRedditService).getUserContributions(any(UserContributionPaginator.class));
        assertEquals(contributions.size(), actualContributions.size());
        assertEquals(contributions, actualContributions);
        assertEquals(contributions.size(), profileRepository.getCachedContributions().size());
        assertEquals(contributions, profileRepository.getCachedContributions());
    }

    @Test
    public void testNext_calledTwice_cachedContributionsHaveAll() {
        when(mockRedditService.getUserContributions(any(UserContributionPaginator.class)))
                .thenReturn(Single.just(contributions));
        List<Contribution> expectedCachedContributions = new ArrayList<>();
        expectedCachedContributions.addAll(contributions);
        expectedCachedContributions.addAll(contributions);

        List<Contribution> actualContributions = profileRepository.next().blockingGet();
        List<Contribution> actualContributions2 = profileRepository.next().blockingGet();

        verify(mockRedditService, times(2))
                .getUserContributions(any(UserContributionPaginator.class));
        assertEquals(contributions.size(), actualContributions.size());
        assertEquals(contributions, actualContributions);
        assertEquals(contributions.size(), actualContributions2.size());
        assertEquals(contributions, actualContributions2);
        assertEquals(expectedCachedContributions.size(),
                profileRepository.getCachedContributions().size());
        assertEquals(expectedCachedContributions, profileRepository.getCachedContributions());
    }

    @Test
    public void testClearCache() {
        when(mockRedditService.getUserContributions(any(UserContributionPaginator.class)))
                .thenReturn(Single.just(contributions));

        profileRepository.next().blockingGet();

        assertEquals(contributions.size(), profileRepository.getCachedContributions().size());

        profileRepository.clearCache();

        assertTrue(profileRepository.getCachedContributions().isEmpty());
    }

    @Test
    public void testReset() {
        when(mockRedditService.getUserContributions(any(UserContributionPaginator.class)))
                .thenReturn(Single.just(contributions));

        profileRepository.next().blockingGet();

        assertEquals(contributions.size(), profileRepository.getCachedContributions().size());

        profileRepository.reset();

        assertTrue(profileRepository.getCachedContributions().isEmpty());
    }
}
