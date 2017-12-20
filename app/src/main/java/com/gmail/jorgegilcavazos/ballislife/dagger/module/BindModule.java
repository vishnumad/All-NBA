package com.gmail.jorgegilcavazos.ballislife.dagger.module;

import com.gmail.jorgegilcavazos.ballislife.data.actions.RedditActions;
import com.gmail.jorgegilcavazos.ballislife.data.actions.RedditActionsImpl;
import com.gmail.jorgegilcavazos.ballislife.data.firebase.remoteconfig.RemoteConfig;
import com.gmail.jorgegilcavazos.ballislife.data.firebase.remoteconfig.RemoteConfigImpl;
import com.gmail.jorgegilcavazos.ballislife.data.local.AppLocalRepository;
import com.gmail.jorgegilcavazos.ballislife.data.local.LocalRepository;
import com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthenticationImpl;
import com.gmail.jorgegilcavazos.ballislife.data.repository.boxscore.BoxScoreRepository;
import com.gmail.jorgegilcavazos.ballislife.data.repository.boxscore.BoxScoreRepositoryImpl;
import com.gmail.jorgegilcavazos.ballislife.data.repository.comments.ContributionRepository;
import com.gmail.jorgegilcavazos.ballislife.data.repository.comments.ContributionRepositoryImpl;
import com.gmail.jorgegilcavazos.ballislife.data.repository.games.GamesRepository;
import com.gmail.jorgegilcavazos.ballislife.data.repository.games.GamesRepositoryImpl;
import com.gmail.jorgegilcavazos.ballislife.data.repository.gamethreads.GameThreadsRepository;
import com.gmail.jorgegilcavazos.ballislife.data.repository.gamethreads.GameThreadsRepositoryImpl;
import com.gmail.jorgegilcavazos.ballislife.data.repository.highlights.FavoritesRepository;
import com.gmail.jorgegilcavazos.ballislife.data.repository.highlights.FavoritesRepositoryImpl;
import com.gmail.jorgegilcavazos.ballislife.data.repository.highlights.HighlightsRepository;
import com.gmail.jorgegilcavazos.ballislife.data.repository.highlights.HighlightsRepositoryImpl;
import com.gmail.jorgegilcavazos.ballislife.data.repository.posts.PostsRepository;
import com.gmail.jorgegilcavazos.ballislife.data.repository.posts.PostsRepositoryImpl;
import com.gmail.jorgegilcavazos.ballislife.data.repository.profile.ProfileRepository;
import com.gmail.jorgegilcavazos.ballislife.data.repository.profile.ProfileRepositoryImpl;
import com.gmail.jorgegilcavazos.ballislife.data.repository.submissions.SubmissionRepository;
import com.gmail.jorgegilcavazos.ballislife.data.repository.submissions.SubmissionRepositoryImpl;
import com.gmail.jorgegilcavazos.ballislife.data.service.RedditService;
import com.gmail.jorgegilcavazos.ballislife.data.service.RedditServiceImpl;
import com.gmail.jorgegilcavazos.ballislife.util.CrashReporter;
import com.gmail.jorgegilcavazos.ballislife.util.CrashReporterImpl;
import com.gmail.jorgegilcavazos.ballislife.util.ErrorHandler;
import com.gmail.jorgegilcavazos.ballislife.util.ErrorHandlerImpl;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.SchedulerProvider;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class BindModule {

    @Binds
    public abstract PostsRepository bindPostsRepository(PostsRepositoryImpl postsRepositoryImpl);

    @Binds
    public abstract RedditService bindRedditService(RedditServiceImpl redditServiceImpl);

    @Binds
    public abstract RedditAuthentication bindRedditAuthentication(
            RedditAuthenticationImpl redditAuthenticationImpl);

    @Binds
    public abstract ProfileRepository bindProfileRepository(
            ProfileRepositoryImpl profileRepositoryImpl);

    @Binds
    public abstract LocalRepository bindLocalRepository(AppLocalRepository appLocalRepository);

    @Binds
    public abstract BaseSchedulerProvider bindBaseSchedulerProvider(
            SchedulerProvider schedulerProvider);

    @Binds
    public abstract GamesRepository bindGamesRepository(GamesRepositoryImpl gamesRepositoryImpl);

    @Binds
    public abstract HighlightsRepository bindHighlightsRepository(
            HighlightsRepositoryImpl highlightsRepositoryImpl);

    @Binds
    public abstract FavoritesRepository bindFavoritesRepository(
            FavoritesRepositoryImpl favoritesRepositoryImpl);

    @Binds
    public abstract SubmissionRepository bindSubmissionRepository(
            SubmissionRepositoryImpl submissionRepositoryImpl);

    @Binds
    public abstract GameThreadsRepository bindGameThreadsRepository(
            GameThreadsRepositoryImpl gameThreadsRepositoryImpl);

    @Binds
    public abstract RedditActions bindRedditActions(RedditActionsImpl redditActionsImpl);

    @Binds
    public abstract ContributionRepository bindCommentsRepository(
            ContributionRepositoryImpl commentsRepositoryImpl);

    @Binds
    public abstract CrashReporter bindCrashReporter(CrashReporterImpl crashReporterImpl);

    @Binds
    public abstract ErrorHandler bindErrorHandler(ErrorHandlerImpl errorHandlerImpl);

    @Binds
    public abstract BoxScoreRepository bindBoxScoreRepository(
            BoxScoreRepositoryImpl boxScoreRepositoryImpl);

    @Binds
    public abstract RemoteConfig bindRemoteConfig(RemoteConfigImpl remoteConfigImpl);
}
