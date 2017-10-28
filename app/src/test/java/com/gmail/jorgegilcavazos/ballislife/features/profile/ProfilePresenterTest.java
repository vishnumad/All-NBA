package com.gmail.jorgegilcavazos.ballislife.features.profile;

import com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.data.repository.profile.ProfileRepository;
import com.gmail.jorgegilcavazos.ballislife.util.exception.NotAuthenticatedException;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.TrampolineSchedulerProvider;
import com.google.common.collect.ImmutableList;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.PublicContribution;
import net.dean.jraw.models.Submission;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link ProfilePresenter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProfilePresenterTest {

    private static final List<Contribution> contributions =
            ImmutableList.of(
                    mock(Contribution.class),
                    mock(Contribution.class),
                    mock(Contribution.class));

    private static final List<Contribution> emptyContributions = Collections.emptyList();

    @Mock
    ProfileView mockView;

    @Mock
    ProfileRepository mockRepository;

    @Mock
    RedditAuthentication mockRedditAuthentication;

    private ProfilePresenter presenter;

    @Before
    public void setup() {
        presenter = new ProfilePresenter(
                mockRepository,
                mockRedditAuthentication,
                new TrampolineSchedulerProvider());
        presenter.attachView(mockView);
    }

    @After
    public void cleanup() {
        presenter.stop();
        presenter.detachView();
    }

    @Test
    public void testLoadFirstAvailable_cacheAvailable() {
        when(mockRepository.getCachedContributions()).thenReturn(contributions);

        presenter.loadFirstAvailable();

        verify(mockView).showContent(contributions, true);
    }

    @Test
    public void testLoadFirstAvailable_cacheNotAvailable() {
        when(mockRepository.getCachedContributions()).thenReturn(Collections.emptyList());
        when(mockRedditAuthentication.authenticate())
                .thenReturn(Completable.complete());
        when(mockRepository.next()).thenReturn(Single.just(contributions));

        presenter.loadFirstAvailable();

        verify(mockRepository).getCachedContributions();
        verify(mockView).setLoadingIndicator(true);
        verify(mockView).resetScrollingState();
        verify(mockRepository).reset();
        verify(mockView).dismissSnackbar();
        verify(mockRedditAuthentication).authenticate();
        verify(mockRepository).next();
        verify(mockView).showContent(contributions, true);
        verify(mockView).scrollToTop();
        verify(mockView).setLoadingIndicator(false);
        verifyNoMoreInteractions(mockView);
        verifyNoMoreInteractions(mockRepository);
        verifyNoMoreInteractions(mockRedditAuthentication);
    }

    @Test
    public void testLoadFirstAvailable_cacheNotAvailableAndEmptyNetworkList() {
        when(mockRepository.getCachedContributions()).thenReturn(Collections.emptyList());
        when(mockRedditAuthentication.authenticate())
                .thenReturn(Completable.complete());
        when(mockRepository.next()).thenReturn(Single.just(emptyContributions));

        presenter.loadFirstAvailable();

        verify(mockRepository).getCachedContributions();
        verify(mockView).setLoadingIndicator(true);
        verify(mockView).resetScrollingState();
        verify(mockRepository).reset();
        verify(mockView).dismissSnackbar();
        verify(mockRedditAuthentication).authenticate();
        verify(mockRepository).next();
        verify(mockView).showNothingToShowSnackbar();
        verify(mockView).hideContent();
        verify(mockView).scrollToTop();
        verify(mockView).setLoadingIndicator(false);
        verifyNoMoreInteractions(mockView);
        verifyNoMoreInteractions(mockRepository);
        verifyNoMoreInteractions(mockRedditAuthentication);
    }

    @Test
    public void testLoadFirstAvailable_cacheNotAvailableAndNetworkError() {
        when(mockRepository.getCachedContributions()).thenReturn(Collections.emptyList());
        when(mockRedditAuthentication.authenticate())
                .thenReturn(Completable.complete());
        Single<List<Contribution>> errorSingle = Single.error(new Exception());
        when(mockRepository.next()).thenReturn(errorSingle);

        presenter.loadFirstAvailable();

        verify(mockRepository).getCachedContributions();
        verify(mockView).setLoadingIndicator(true);
        verify(mockView).resetScrollingState();
        verify(mockRepository).reset();
        verify(mockView).dismissSnackbar();
        verify(mockRedditAuthentication).authenticate();
        verify(mockRepository).next();
        verify(mockView).showContributionsLoadingFailedSnackbar(true);
        verify(mockView).setLoadingIndicator(false);
        verify(mockView).hideContent();
        verifyNoMoreInteractions(mockView);
        verifyNoMoreInteractions(mockRepository);
        verifyNoMoreInteractions(mockRedditAuthentication);
    }

    @Test
    public void testLoadFirstAvailable_cacheNotAvailableAndUserNotLoggedIn() {
        when(mockRepository.getCachedContributions()).thenReturn(Collections.emptyList());
        when(mockRedditAuthentication.authenticate())
                .thenReturn(Completable.complete());
        Single<List<Contribution>> errorSingle = Single.error(new NotAuthenticatedException());
        when(mockRepository.next()).thenReturn(errorSingle);

        presenter.loadFirstAvailable();

        verify(mockRepository).getCachedContributions();
        verify(mockView).setLoadingIndicator(true);
        verify(mockView).resetScrollingState();
        verify(mockRepository).reset();
        verify(mockView).dismissSnackbar();
        verify(mockRedditAuthentication).authenticate();
        verify(mockRepository).next();
        verify(mockView).showNotAuthenticatedToast();
        verify(mockView).setLoadingIndicator(false);
        verify(mockView).hideContent();
        verifyNoMoreInteractions(mockView);
        verifyNoMoreInteractions(mockRepository);
        verifyNoMoreInteractions(mockRedditAuthentication);
    }

    @Test
    public void testObserveContributionsClicks() {
        Comment mockComment = mock(Comment.class);
        when(mockComment.getSubmissionId()).thenReturn("t3_sub_id_1");
        when(mockComment.getId()).thenReturn("t1_comment_id_1");
        Comment mockComment2 = mock(Comment.class);
        when(mockComment2.getSubmissionId()).thenReturn("t3_sub_id_2");
        when(mockComment.getId()).thenReturn("t1_comment_id_2");
        Submission mockSubmission = mock(Submission.class);
        when(mockSubmission.getId()).thenReturn("sub_id_3");


        Observable<PublicContribution> contributionObservable
                = Observable.just(mockComment, mockComment2, mockSubmission);

        presenter.observeContributionsClicks(contributionObservable);

        verify(mockView).openSubmissionAndScrollToComment("sub_id_1", mockComment.getId());
        verify(mockView).openSubmissionAndScrollToComment("sub_id_2", mockComment2.getId());
        verify(mockView).openSubmission("sub_id_3");
    }

    @Test
    public void testObserveContributionsClicks_unexpectedError() {
        Observable<PublicContribution> contributionObservable = Observable.error(new Exception());

        presenter.observeContributionsClicks(contributionObservable);

        verify(mockView).showUnknownErrorToast(any(Throwable.class));
    }
}
